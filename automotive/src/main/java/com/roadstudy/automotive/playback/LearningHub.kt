package com.roadstudy.automotive.playback

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.roadstudy.automotive.session.SessionResult
import com.roadstudy.automotive.session.SessionUiState

/**
 * 지금 진행 중인 주행 학습 세션(하나뿐)을 들고 있는 곳. 주행 화면과 미디어 서비스([LearningPlaybackService])가 함께 쓴다.
 *
 * - [start] 하면 세션을 만들고, 신호 수신을 붙잡고, 미디어 서비스를 켠다. 같은 섹션으로 다시 부르면 이어서 쓴다.
 * - 세션이 끝나면(도착·마지막 개념) [stop] 으로 정리한다. 화면이 닫혀도 세션은 이어진다(내비가 앞에 있어도 소리로 진행).
 * - 메인 스레드에서만 쓴다.
 */
object LearningHub {

    private val _state = MutableLiveData<SessionUiState?>(null)

    /** 진행 중인 세션 상태. 세션이 없으면 null */
    val state: LiveData<SessionUiState?> = _state

    private val _level = MutableLiveData(0.0)

    /** 읽고 있는 소리의 크기(RMS). 파형에 쓴다. 조용하면 0 */
    val level: LiveData<Double> = _level

    var session: LearningSession? = null
        private set

    /** 마지막으로 도착해서 끝난 세션의 결과. 주차 후 복습 화면이 쓴다 */
    var lastResult: SessionResult? = null
        private set

    /** 상태가 바뀌면 미디어 서비스가 플레이어 상태를 다시 계산하도록 부른다 */
    var onChanged: (() -> Unit)? = null

    /** 같은 섹션의 세션이 진행 중이면 그대로 두고, 아니면 새로 시작한다 */
    fun start(context: Context, request: SessionRequest) {
        val current = session
        if (current != null && current.request.sectionId == request.sectionId && !current.state.finished) return
        stop()
        lastResult = null
        val app = context.applicationContext
        SignalSources.acquire(app, USER)
        val created = LearningSession(
            app,
            request,
            onState = { state ->
                _state.value = state
                onChanged?.invoke()
            },
            onLevel = { _level.value = it },
            onFocusLost = ::stop,
            // 도착하면 내비 안내도 끝낸다 (MVP: 주행한 뒤 P단 = 도착)
            onArrived = SignalSources::cancelRouteIfGuiding
        )
        session = created
        created.start()
        _state.value = created.state
        app.startService(Intent(app, LearningPlaybackService::class.java))
    }

    fun stop() {
        val current = session ?: return
        session = null
        current.result()?.let { lastResult = it }
        current.stop()
        SignalSources.release(USER)
        _state.value = null
        _level.value = 0.0
        onChanged?.invoke()
    }

    fun setPaused(paused: Boolean) = session?.setPaused(paused)

    fun next() = session?.onNext()

    fun previous() = session?.onPrevious()

    private const val USER = "session"
}
