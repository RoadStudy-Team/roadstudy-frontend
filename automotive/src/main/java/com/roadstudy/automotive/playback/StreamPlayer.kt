package com.roadstudy.automotive.playback

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * 백엔드의 학습 음성 한 파일을 받으면서 재생한다 (ExoPlayer).
 *
 * - 재생·멈춤은 세션이 [setPlaying] 으로 정한다. 멈췄다가 다시 재생하면 멈춘 지점부터 이어진다.
 * - 오디오 포커스는 세션이 직접 관리하므로 ExoPlayer 의 자동 포커스 처리는 끈다.
 * - 받지 못하거나 재생에 실패하면 [onError] 를 한 번 부른다 (세션이 TTS 로 바꾼다). 메인 스레드에서만 쓴다.
 */
class StreamPlayer(
    context: Context,
    url: String,
    private val onError: () -> Unit
) {
    private var failed = false

    private val player = ExoPlayer.Builder(context.applicationContext).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            /* handleAudioFocus = */ false
        )
        addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.w(TAG, "음성 파일 재생 실패: ${error.errorCodeName}")
                if (failed) return
                failed = true
                onError()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) Log.d(TAG, "음성 파일 준비됨 (${duration / 1_000}초)")
                if (state == Player.STATE_ENDED) Log.d(TAG, "음성 파일 끝까지 재생함")
            }
        })
        setMediaItem(MediaItem.fromUri(url))
        prepare()
    }

    init {
        Log.d(TAG, "음성 파일 받기: $url")
    }

    fun setPlaying(playing: Boolean) {
        if (player.playWhenReady == playing) return
        player.playWhenReady = playing
        Log.d(TAG, if (playing) "음성 파일 재생 (${player.currentPosition / 1_000}초부터)" else "음성 파일 멈춤")
    }

    /** 앞(+)·뒤(-)로 [deltaMillis] 만큼 옮긴다 */
    fun seekBy(deltaMillis: Long) {
        val duration = player.duration.takeIf { it != C.TIME_UNSET } ?: return
        val target = (player.currentPosition + deltaMillis).coerceIn(0L, duration)
        player.seekTo(target)
        Log.d(TAG, "음성 파일 ${target / 1_000}초로 이동")
    }

    fun release() = player.release()

    private companion object {
        const val TAG = "RoadStudyAudio"
    }
}
