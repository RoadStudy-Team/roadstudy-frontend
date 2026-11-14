package com.roadstudy.automotive.playback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

/**
 * 세션 동안 우리 앱에서 무음을 재생해 둔다.
 *
 * 이유: 실제 목소리는 Pleos TextToSpeech 서비스(다른 프로세스)가 내기 때문에, 시스템은 우리 앱이 소리를 내고 있다고 보지 않는다.
 * 그러면 운전대·미디어 버튼을 받을 세션(media button session)이 정해지지 않아 버튼이 우리 앱으로 오지 않는다(에뮬레이터 실측).
 * 우리 앱이 무음을 재생하고 있으면 시스템이 우리 세션을 재생 중인 앱으로 보고 버튼을 보낸다.
 */
class SilentKeepAlive {

    private var track: AudioTrack? = null

    fun start() {
        if (track != null) return
        val frames = SAMPLE_RATE // 1초 분량의 무음을 반복한다
        track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(frames * BYTES_PER_FRAME)
                .build()
                .also {
                    it.write(ShortArray(frames), 0, frames)
                    it.setLoopPoints(0, frames, LOOP_FOREVER)
                    it.play()
                }
        }.onFailure { Log.w(TAG, "무음 재생 실패: ${it.message}") }.getOrNull()
    }

    fun stop() {
        track?.run {
            runCatching { stop() }
            release()
        }
        track = null
    }

    private companion object {
        const val TAG = "RoadStudyAudio"
        const val SAMPLE_RATE = 8_000
        const val BYTES_PER_FRAME = 2
        const val LOOP_FOREVER = -1
    }
}
