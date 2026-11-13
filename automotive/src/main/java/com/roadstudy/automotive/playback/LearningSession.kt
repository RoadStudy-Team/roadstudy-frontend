package com.roadstudy.automotive.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Observer
import com.roadstudy.automotive.navi.NaviSignalHub
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.LearningMode
import com.roadstudy.automotive.session.LiveDrivingContext
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.ProgressTracker
import com.roadstudy.automotive.session.RouteEndTracker
import com.roadstudy.automotive.session.SessionController
import com.roadstudy.automotive.session.SessionPlan
import com.roadstudy.automotive.session.SessionResult
import com.roadstudy.automotive.session.TripType
import com.roadstudy.automotive.session.SessionUiState
import com.roadstudy.automotive.session.VehicleSignals
import com.roadstudy.automotive.vehicle.VehicleSignalHub

/** 세션을 시작할 때 필요한 값. 홈에서 고른 섹션과 경로로 세운 계획이다 */
data class SessionRequest(
    val sectionId: String?,
    val sectionTitle: String,
    val destinationName: String,
    val plan: SessionPlan,
    val progress: ProgressTracker,
    /** 내비 안내가 없을 때 쓰는 남은 거리(m)·시간(초) */
    val fallbackMeters: Int?,
    val fallbackSec: Int?,
    /** 개발·시연용: 내비의 회전·주의 구간·재탐색으로 멈추지 않는다 ([LiveDrivingContext] 참고) */
    val ignoreNaviFocus: Boolean = false,
    /** 출근·퇴근·기타. 주차 후 복습 화면의 제목에 쓴다 */
    val tripType: TripType = TripType.TO_WORK
)

/**
 * 실제 차량·내비 신호로 한 번의 주행 학습을 처음부터 끝까지 진행한다.
 *
 * - 하나의 [SessionController] 를 끝까지 유지해서 "몇 번째 개념"이 이어진다 (화면이 다시 만들어져도 그대로다).
 * - 신호 허브를 직접 구독하고, 0.5초마다 진행기를 깨워 떠올리기 시간·재탐색 후 집중 모드 해제를 처리한다.
 * - 소리는 [PleosTtsAudioPort] 로 읽는다. 운전대 버튼을 받기 위해 세션 동안 무음도 재생한다([SilentKeepAlive]).
 *   오디오 포커스를 잃으면(다른 앱 재생) 멈추고, 잠깐 잃으면(내비 음성 안내) 일시정지했다가 이어 간다.
 *   실제 목소리는 TTS 서비스(다른 프로세스)가 내므로 시스템의 "소리 줄이기"가 적용되지 않는다. 그래서 줄이는 대신 멈춘다.
 * - 바뀐 상태는 [onState] 로 알린다. 메인 스레드에서만 쓴다.
 */
class LearningSession(
    context: Context,
    val request: SessionRequest,
    private val onState: (SessionUiState) -> Unit,
    private val onLevel: (Double) -> Unit,
    private val onFocusLost: () -> Unit,
    /** 도착(주행한 뒤 P단)으로 세션이 끝났을 때 한 번 부른다 */
    private val onArrived: () -> Unit = {},
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val main = Handler(Looper.getMainLooper())
    private val audio = PleosTtsAudioPort(context, onFinished = ::onAudioFinished, onLevel = onLevel)
    private val controller = SessionController(request.plan, request.progress, audio)
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val keepAlive = SilentKeepAlive()

    private var vehicle = VehicleSignalHub.signals.value ?: VehicleSignals()
    private var navi = NaviSignalHub.signals.value ?: NaviSignals()
    private var hasDriven = vehicle.gear == Gear.DRIVE

    /** 세션 중에 내비 안내가 끝났는지 (P단에서 끝나면 도착) */
    private val routeEnd = RouteEndTracker()
    private var lastState: SessionUiState? = null
    private var running = false
    private var startedAtMillis = 0L
    private var arrivalNotified = false

    /** 내비 음성 안내 등으로 잠깐 멈춘 경우. 포커스를 되찾으면 이어서 재생한다 */
    private var pausedByTransientLoss = false

    private val vehicleObserver = Observer<VehicleSignals> { signals ->
        vehicle = signals
        if (signals.gear == Gear.DRIVE) hasDriven = true
        update()
    }
    private val naviObserver = Observer<NaviSignals> { signals ->
        navi = signals
        update()
    }
    private val ticker = object : Runnable {
        override fun run() {
            if (!running) return
            controller.tick(clock())
            update()
            main.postDelayed(this, TICK_MILLIS)
        }
    }

    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        // 차량용 안드로이드는 내비 안내가 미디어와 "함께 재생(작게 줄이기)"이 기본이라 알림 없이 겹칠 수 있다.
        // 이 값을 켜야 내비 안내 때 포커스 변화(잠깐 잃음)를 받아 학습을 멈추고, 안내가 끝나면 이어 간다.
        .setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener(::onFocusChange, main)
        .build()

    val state: SessionUiState get() = controller.uiState

    fun start() {
        if (running) return
        running = true
        startedAtMillis = clock()
        val focus = audioManager.requestAudioFocus(focusRequest)
        keepAlive.start()
        Log.d(TAG, "세션 시작: ${request.sectionTitle}, 개념 ${request.plan.items.size}개, 오디오 포커스 $focus" +
            if (request.ignoreNaviFocus) " (개발용: 내비 회전·주의 구간 무시)" else "")
        VehicleSignalHub.signals.observeForever(vehicleObserver)
        NaviSignalHub.signals.observeForever(naviObserver)
        update()
        main.postDelayed(ticker, TICK_MILLIS)
    }

    fun stop() {
        if (!running) return
        running = false
        main.removeCallbacks(ticker)
        VehicleSignalHub.signals.removeObserver(vehicleObserver)
        NaviSignalHub.signals.removeObserver(naviObserver)
        audioManager.abandonAudioFocusRequest(focusRequest)
        keepAlive.stop()
        audio.release()
        Log.d(TAG, "세션 끝")
    }

    /** 도착으로 끝났으면 실제 결과를 돌려준다. 아직 진행 중이거나 중간에 멈췄으면 null */
    fun result(): SessionResult? {
        val state = controller.uiState
        if (!state.finished || state.mode != LearningMode.PARKED) return null
        return SessionResult.of(
            request.plan, state, request.progress, request.sectionId, request.tripType, clock() - startedAtMillis
        )
    }

    /** 운전대 재생/일시정지 */
    fun setPaused(paused: Boolean) {
        if (paused) controller.pauseByUser() else controller.resumeByUser(clock())
        pausedByTransientLoss = false
        publish()
    }

    /** 운전대 다음(▶): 알아요 → 다음 개념 */
    fun onNext() {
        Log.d(TAG, "운전대 다음(알아요): ${controller.uiState.step}")
        controller.onSteeringNext(clock())
        publish()
    }

    /** 운전대 이전(◀): 다시 들려줘 → 설명부터 다시 */
    fun onPrevious() {
        Log.d(TAG, "운전대 이전(다시 들려줘): ${controller.uiState.step}")
        controller.onSteeringPrevious(clock())
        publish()
    }

    private fun onAudioFinished() {
        controller.onAudioFinished(clock())
        publish()
    }

    private fun update() {
        if (!running) return
        controller.onContextChanged(
            LiveDrivingContext.build(
                vehicle, navi, hasDriven, request.fallbackMeters, request.fallbackSec, clock(), request.ignoreNaviFocus,
                routeEnded = routeEnd.update(navi)
            )
        )
        publish()
    }

    private fun publish() {
        val state = controller.uiState
        if (state == lastState) return
        if (state.mode != lastState?.mode || state.conceptIndex != lastState?.conceptIndex || state.step != lastState?.step) {
            Log.d(TAG, "상태 ${state.mode}(${state.reason}) 개념 ${state.conceptIndex}/${state.conceptTotal} ${state.step}")
        }
        lastState = state
        // 화면이 상태를 받고 세션을 정리(stop)할 수 있어서, 도착 처리를 먼저 한다
        if (state.finished && state.mode == LearningMode.PARKED && !arrivalNotified) {
            arrivalNotified = true
            Log.d(TAG, "도착")
            onArrived()
        }
        onState(state)
    }

    private fun onFocusChange(change: Int) {
        Log.d(TAG, "오디오 포커스 변화 $change")
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> onFocusLost()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (!controller.uiState.userPaused) {
                    pausedByTransientLoss = true
                    controller.pauseByUser()
                    publish()
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> if (pausedByTransientLoss) {
                pausedByTransientLoss = false
                controller.resumeByUser(clock())
                publish()
            }
        }
    }

    private companion object {
        const val TAG = "RoadStudySession"
        const val TICK_MILLIS = 500L
    }
}
