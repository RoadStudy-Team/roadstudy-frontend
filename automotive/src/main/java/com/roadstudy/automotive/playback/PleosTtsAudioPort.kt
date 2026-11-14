package com.roadstudy.automotive.playback

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.roadstudy.automotive.session.AudioClip
import com.roadstudy.automotive.session.AudioPort
import java.util.Locale

/**
 * Pleos TextToSpeech(온디바이스)로 개념의 설명·질문·정답 문장을 읽는다.
 *
 * - 온디바이스 모드는 앱 등록(클라이언트 ID·시크릿)이 필요 없고 네트워크 없이 동작한다. 에뮬레이터에서 동작을 확인했다.
 * - SDK 가 초기화되기 전에 [play] 가 오면 기억해 두었다가 초기화가 끝나면 읽는다.
 * - TTS 에는 일시정지가 없어서 [pause] 는 멈추고, [resume] 은 그 문장을 처음부터 다시 읽는다 (문장이 짧아 괜찮다).
 * - 끝까지 읽으면 [onFinished] 를, 읽는 동안 소리 크기(RMS)가 바뀌면 [onLevel] 을 메인 스레드에서 부른다.
 * - 읽기에 실패하면 세션이 멈추지 않도록 끝난 것으로 보고 넘어간다(원인은 로그로 남긴다).
 * - 우리가 멈추지 않았는데 TTS 가 멈추면(에뮬레이터 실측: 내비 음성 안내가 끼어든 뒤 "완료"가 오지 않았다) 그 문장을 다시 읽는다.
 * - 그래도 "완료"가 오지 않으면 문장 길이로 잡은 제한 시간이 지난 뒤 끝난 것으로 보고 넘어간다. 세션이 멈춘 채로 남지 않게 한다.
 */
class PleosTtsAudioPort(
    context: Context,
    private val onFinished: () -> Unit,
    private val onLevel: (Double) -> Unit = {}
) : AudioPort {

    private val main = Handler(Looper.getMainLooper())
    private val tts = TextToSpeech(context.applicationContext, Mode.ON_DEVICE)
    private var ready = false
    private var released = false

    /** 지금 읽고 있거나 일시정지된 문장 */
    private var current: AudioClip? = null

    /** 일부러 멈춘 경우의 onStop 은 "다 읽음"으로 보지 않는다 */
    private var speaking = false

    /**
     * 우리가 tts.stop() 을 부른 시각. 그 직후의 onStop 은 우리가 멈춘 것이고, 아니면 다른 소리(내비 안내 등)에 끊긴 것이다.
     * 운전대 버튼처럼 멈추자마자 다음 문장을 읽으면 onStop 이 새 문장 시작 뒤에 늦게 올 수 있어서, 표시 대신 시각으로 판단한다.
     */
    private var stopRequestedAt = NEVER

    /** 지금 문장을 다시 읽은 횟수 (끊길 때마다 다시 읽되 무한 반복하지 않는다) */
    private var retries = 0

    private val watchdog = Runnable {
        if (!speaking) return@Runnable
        Log.w(TAG, "읽기 완료 신호가 오지 않아 다음으로 넘어감: ${current?.text}")
        onSpeechEnded("시간 초과")
    }

    private val listener = object : EventListener {
        override fun onReady() = Unit
        override fun onStart() = Unit
        override fun onStop() {
            main.post { onSpeechStopped() }
        }

        override fun onDone() {
            main.post { onSpeechEnded("완료") }
        }

        override fun onError(errorType: ErrorType) {
            main.post {
                Log.w(TAG, "TTS 오류 $errorType")
                onSpeechEnded("오류")
            }
        }

        override fun onUpdatedRms(rms: Double) {
            main.post { if (speaking) onLevel(rms) }
        }
    }

    init {
        tts.initialize(object : OnInitializeListener {
            override fun onSuccess() {
                main.post {
                    if (released) return@post
                    // 리스너는 초기화가 끝난 뒤에 등록해야 한다 (먼저 등록하면 SDK 내부에서 예외가 난다)
                    tts.addEventListener(listener)
                    tts.setLocale(Locale.KOREA)
                    ready = true
                    Log.d(TAG, "TTS 준비됨")
                    if (speaking) current?.let { speak(it) }
                }
            }

            override fun onFailure(errorType: ErrorType) {
                Log.w(TAG, "TTS 초기화 실패 $errorType")
            }
        })
    }

    override fun play(clip: AudioClip) {
        current = clip
        speaking = true
        retries = 0
        if (ready) speak(clip)
    }

    override fun pause() {
        if (!speaking) return
        speaking = false
        main.removeCallbacks(watchdog)
        requestStop()
    }

    override fun resume() {
        val clip = current ?: return
        speaking = true
        if (ready) speak(clip)
    }

    override fun stop() {
        current = null
        speaking = false
        main.removeCallbacks(watchdog)
        requestStop()
    }

    fun release() {
        released = true
        stop()
        if (ready) {
            tts.removeEventListener(listener)
            tts.release()
        }
        ready = false
    }

    private fun speak(clip: AudioClip) {
        Log.d(TAG, "읽기: ${clip.text}")
        main.removeCallbacks(watchdog)
        main.postDelayed(watchdog, timeoutMillis(clip.text))
        tts.speak(clip.text, true)
    }

    private fun requestStop() {
        if (!ready) return
        stopRequestedAt = SystemClock.uptimeMillis()
        tts.stop()
    }

    /** 우리가 멈추지 않았는데 멈췄으면 다른 소리에 끊긴 것으로 보고 그 문장을 다시 읽는다 */
    private fun onSpeechStopped() {
        if (SystemClock.uptimeMillis() - stopRequestedAt < OWN_STOP_WINDOW_MILLIS) return
        val clip = current ?: return
        if (!speaking || released) return
        if (retries >= MAX_RETRIES) {
            onSpeechEnded("끊김")
            return
        }
        retries++
        Log.d(TAG, "읽는 중 끊겨서 다시 읽음 ($retries)")
        main.removeCallbacks(watchdog)
        main.postDelayed({ if (speaking && current == clip) speak(clip) }, RETRY_DELAY_MILLIS)
    }

    /** 한국어를 느긋하게 읽는 속도(글자당 0.3초)에 여유를 더한 제한 시간 */
    private fun timeoutMillis(text: String): Long = BASE_TIMEOUT_MILLIS + text.length * MILLIS_PER_CHAR

    private fun onSpeechEnded(reason: String) {
        if (!speaking || released) return
        speaking = false
        current = null
        main.removeCallbacks(watchdog)
        Log.d(TAG, "읽기 끝($reason)")
        onLevel(0.0)
        onFinished()
    }

    private companion object {
        const val TAG = "RoadStudyAudio"
        const val MAX_RETRIES = 2
        const val RETRY_DELAY_MILLIS = 1_000L
        const val BASE_TIMEOUT_MILLIS = 8_000L
        const val MILLIS_PER_CHAR = 300L
        const val OWN_STOP_WINDOW_MILLIS = 1_500L
        const val NEVER = -100_000L
    }
}
