package com.roadstudy.automotive.session

/** 개념 하나를 진행하는 단계: 설명 → 질문 → 떠올리기(무음) → 정답 */
enum class Step { EXPLAIN, QUESTION, THINK, ANSWER }

data class SessionUiState(
    val mode: LearningMode,
    val reason: ModeReason,
    val conceptIndex: Int,
    val conceptTotal: Int,
    val currentTitle: String?,
    val remainingMeters: Int?,
    val remainingSec: Int?,
    val finished: Boolean,
    /** 현재 개념의 진행 단계. 화면이 "질문 · 떠올려보세요" 같은 문구를 고를 때 쓴다 */
    val step: Step = Step.EXPLAIN,
    /** 사용자가 일시정지했는지 (운전대 재생/일시정지 버튼) */
    val userPaused: Boolean = false
)

/**
 * 주행 상황(모드)에 맞춰 학습을 진행·일시정지·가벼운 복습으로 전환하는 상태 머신.
 *
 * - NORMAL : 설명 → 질문 → 떠올리기 → 정답
 * - CAUTION: 새 개념은 건너뛰고, 다시 듣기 개념의 설명만 가볍게 재생
 * - FOCUS  : 즉시 일시정지, 조건이 풀리면 이어서 재생
 * - PARKED : 정지. UI 는 주차 후 정리 화면으로 전환
 *
 * 핸들 버튼: 다음(▶) = 알아요, 이전(◀) = 다시 들려줘, 재생/일시정지 = [pauseByUser] / [resumeByUser]
 * 다음·이전은 기록만 하지 않고 바로 반응한다(눌렸는지 귀로 알 수 있게): 알아요 → 다음 개념, 다시 들려줘 → 설명부터 다시.
 */
class SessionController(
    private val plan: SessionPlan,
    private val progress: ProgressTracker,
    private val audio: AudioPort,
    private val decider: LearningModeDecider = LearningModeDecider(),
    private val thinkMillis: Long = THINK_MILLIS
) {
    private var decision = ModeDecision(LearningMode.PARKED, ModeReason.PARKED)
    private var index = -1
    private var step: Step = Step.EXPLAIN
    private var playing = false
    private var pausedByFocus = false
    private var thinkStartedAt: Long? = null
    private var lastNavi = NaviSignals()
    private var finished = false
    private var userPaused = false

    /** 멈춰 있는 동안 버튼으로 위치를 옮겼다. 다시 재생할 때 이어 듣기가 아니라 새 위치에서 처음부터 읽는다 */
    private var restartStep = false

    val uiState: SessionUiState
        get() = SessionUiState(
            mode = decision.mode,
            reason = decision.reason,
            conceptIndex = (index + 1).coerceIn(0, plan.items.size),
            conceptTotal = plan.items.size,
            currentTitle = plan.items.getOrNull(index)?.concept?.title,
            remainingMeters = lastNavi.remainingMeters,
            remainingSec = lastNavi.remainingSec,
            finished = finished,
            step = step,
            userPaused = userPaused
        )

    /** 차량·내비 상태가 바뀔 때마다 호출 (vehicle / navi 패키지에서) */
    fun onContextChanged(context: DrivingContext) {
        lastNavi = context.navi
        val previous = decision.mode
        decision = decider.decide(context)
        if (finished) return

        when (decision.mode) {
            LearningMode.PARKED -> {
                audio.stop()
                playing = false
                if (index >= 0) finished = true
            }
            LearningMode.FOCUS -> {
                if (playing) audio.pause()
                if (index >= 0) pausedByFocus = true
                playing = false
            }
            LearningMode.NORMAL, LearningMode.CAUTION -> {
                when {
                    userPaused -> Unit
                    index < 0 -> moveToNextItem(context.nowMillis)
                    restartStep || pausedByFocus -> continuePlayback(context.nowMillis)
                    previous == LearningMode.PARKED -> playCurrentStep(context.nowMillis)
                }
            }
        }
    }

    /** 오디오 한 조각 재생이 끝났을 때 (audio 패키지에서) */
    fun onAudioFinished(nowMillis: Long) {
        if (!playing || finished) return
        playing = false
        val item = plan.items[index]
        when (step) {
            Step.EXPLAIN -> {
                progress.markListened(item.concept.id)
                if (decision.mode == LearningMode.CAUTION) moveToNextItem(nowMillis)
                else { step = Step.QUESTION; playCurrentStep(nowMillis) }
            }
            Step.QUESTION -> { step = Step.THINK; thinkStartedAt = nowMillis }
            Step.THINK -> Unit
            Step.ANSWER -> moveToNextItem(nowMillis)
        }
    }

    /** 주기적으로 호출 (예: 0.5초마다). 떠올리기 시간이 지나면 정답을 들려준다 */
    fun tick(nowMillis: Long) {
        val startedAt = thinkStartedAt ?: return
        if (finished || userPaused || decision.mode == LearningMode.FOCUS || decision.mode == LearningMode.PARKED) return
        if (step == Step.THINK && nowMillis - startedAt >= thinkMillis) {
            thinkStartedAt = null
            step = Step.ANSWER
            playCurrentStep(nowMillis)
        }
    }

    /** 사용자가 일시정지했을 때. 주행 상황이 안전해져도 [resumeByUser] 전까지는 이어서 재생하지 않는다 */
    fun pauseByUser() {
        if (finished || userPaused) return
        userPaused = true
        if (playing) audio.pause()
        playing = false
        if (index >= 0) pausedByFocus = true
    }

    /** 사용자가 다시 재생했을 때. 집중 모드처럼 아직 멈춰야 하는 상황이면 그 상황이 풀릴 때 이어진다 */
    fun resumeByUser(nowMillis: Long) {
        if (!userPaused) return
        userPaused = false
        if (finished) return
        val canPlay = decision.mode == LearningMode.NORMAL || decision.mode == LearningMode.CAUTION
        when {
            !canPlay -> Unit
            index < 0 -> moveToNextItem(nowMillis)
            else -> continuePlayback(nowMillis)
        }
    }

    /** 운전대 다음(▶): 알아요로 기록하고 바로 다음 개념으로 넘어간다 */
    fun onSteeringNext(nowMillis: Long) = jumpBySelfCheck(SelfCheck.KNOW, nowMillis)

    /** 운전대 이전(◀): 다시 들려줘로 기록하고 지금 개념을 설명부터 다시 읽는다 */
    fun onSteeringPrevious(nowMillis: Long) = jumpBySelfCheck(SelfCheck.RELISTEN, nowMillis)

    /**
     * 집중 모드·주차 중에는 무시한다(운전에 집중해야 하는 순간).
     * 사용자가 멈춰 둔 상태라면 기록과 위치 이동만 하고, 소리는 다시 재생할 때 새 위치에서 나온다.
     */
    private fun jumpBySelfCheck(check: SelfCheck, nowMillis: Long) {
        val item = plan.items.getOrNull(index) ?: return
        if (finished) return
        if (decision.mode == LearningMode.FOCUS || decision.mode == LearningMode.PARKED) return
        progress.record(item.concept.id, check)
        if (playing) audio.stop()
        playing = false
        thinkStartedAt = null
        pausedByFocus = false
        when (check) {
            SelfCheck.KNOW -> if (!advance()) return
            SelfCheck.RELISTEN -> step = Step.EXPLAIN
        }
        if (userPaused) {
            restartStep = true
            return
        }
        playCurrentStep(nowMillis)
    }

    /** 멈췄던 학습을 다시 시작한다. 버튼으로 위치를 옮겼다면 그 위치에서 처음부터, 아니면 멈춘 곳에서 이어서 */
    private fun continuePlayback(nowMillis: Long) {
        if (restartStep) {
            restartStep = false
            pausedByFocus = false
            playCurrentStep(nowMillis)
        } else if (pausedByFocus) {
            resumeAfterFocus(nowMillis)
        }
    }

    private fun resumeAfterFocus(nowMillis: Long) {
        pausedByFocus = false
        when (step) {
            // 떠올리기 중에 멈췄다면 떠올리기 시간을 처음부터 다시 준다
            Step.THINK -> thinkStartedAt = nowMillis
            else -> { audio.resume(); playing = true }
        }
    }

    private fun moveToNextItem(nowMillis: Long) {
        if (advance()) playCurrentStep(nowMillis)
    }

    /** 다음 개념의 설명 단계로 옮긴다(주의 모드면 새 개념은 건너뜀). 더 없으면 끝내고 false */
    private fun advance(): Boolean {
        var next = index + 1
        if (decision.mode == LearningMode.CAUTION) {
            while (next < plan.items.size && plan.items[next].kind == PlanItemKind.NEW) next++
        }
        if (next >= plan.items.size) {
            index = plan.items.size
            finished = true
            audio.stop()
            return false
        }
        index = next
        step = Step.EXPLAIN
        return true
    }

    private fun playCurrentStep(nowMillis: Long) {
        val concept = plan.items.getOrNull(index)?.concept ?: return
        when (step) {
            Step.EXPLAIN -> audio.play(AudioClip(concept.explanation, concept.explanationAudioUrl))
            Step.QUESTION -> audio.play(AudioClip(concept.question, concept.questionAudioUrl))
            Step.ANSWER -> audio.play(AudioClip(concept.answer, concept.answerAudioUrl))
            Step.THINK -> { thinkStartedAt = nowMillis; return }
        }
        playing = true
    }

    companion object {
        /** 질문 뒤 속으로 떠올리는 시간 */
        const val THINK_MILLIS = 4_000L
    }
}
