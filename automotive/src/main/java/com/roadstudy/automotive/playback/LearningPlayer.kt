package com.roadstudy.automotive.playback

import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.roadstudy.automotive.session.LearningMode
import com.roadstudy.automotive.session.SessionUiState
import com.roadstudy.automotive.session.Step

/**
 * 진행 중인 학습 세션을 Media3 플레이어로 보여 준다. 시스템(운전대 버튼, 미디어 컨트롤)은 이 플레이어로 세션을 조작한다.
 *
 * - 재생/일시정지 → 세션 일시정지·이어 듣기, 다음(▶) → 알아요, 이전(◀) → 다시 들려줘, 정지 → 세션 끝
 * - 실제 소리는 세션(음성 파일 또는 TTS)이 내므로, 이 플레이어는 상태와 명령만 전달한다.
 * - 다음/이전 버튼이 항상 눌리도록 목록을 [이전, 현재, 다음] 세 칸으로 두고, 어느 칸으로 이동하든 가운데로 되돌린다.
 */
@UnstableApi
class LearningPlayer : SimpleBasePlayer(Looper.getMainLooper()) {

    override fun getState(): State {
        val session = LearningHub.session
        val state = LearningHub.state.value
        val builder = State.Builder()
            .setAvailableCommands(
                Player.Commands.Builder()
                    .addAll(
                        Player.COMMAND_PLAY_PAUSE,
                        Player.COMMAND_STOP,
                        Player.COMMAND_SEEK_TO_NEXT,
                        Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                        Player.COMMAND_SEEK_TO_PREVIOUS,
                        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                        Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                        Player.COMMAND_GET_TIMELINE,
                        Player.COMMAND_GET_METADATA
                    )
                    .build()
            )
        if (session == null || state == null) {
            return builder.setPlaybackState(Player.STATE_IDLE).build()
        }
        val title = state.currentTitle ?: session.request.sectionTitle
        val subtitle = "${session.request.sectionTitle} · 개념 ${state.conceptIndex}/${state.conceptTotal} · ${statusText(state)}"
        return builder
            .setPlaylist(
                listOf(
                    slot(SLOT_PREVIOUS, "다시 들려줘", subtitle),
                    slot(SLOT_CURRENT, title, subtitle),
                    slot(SLOT_NEXT, "알아요", subtitle)
                )
            )
            .setCurrentMediaItemIndex(CURRENT_INDEX)
            .setPlaybackState(if (state.finished) Player.STATE_ENDED else Player.STATE_READY)
            .setPlayWhenReady(!state.userPaused, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .build()
    }

    /** 바뀐 세션 상태를 시스템에 알린다 */
    fun refresh() = invalidateState()

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        LearningHub.setPaused(!playWhenReady)
        return Futures.immediateVoidFuture()
    }

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: Int): ListenableFuture<*> {
        when {
            mediaItemIndex > CURRENT_INDEX -> LearningHub.next()
            mediaItemIndex < CURRENT_INDEX -> LearningHub.previous()
        }
        return Futures.immediateVoidFuture()
    }

    override fun handleStop(): ListenableFuture<*> {
        LearningHub.stop()
        return Futures.immediateVoidFuture()
    }

    private fun slot(id: String, title: String, subtitle: String) =
        MediaItemData.Builder(id)
            .setMediaItem(MediaItem.Builder().setMediaId(id).build())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(subtitle)
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
            )
            .build()

    private fun statusText(state: SessionUiState): String = when {
        state.finished -> "학습 완료"
        state.userPaused -> "일시정지"
        state.mode == LearningMode.FOCUS -> "잠시 멈춤"
        state.mode == LearningMode.CAUTION -> "가볍게 복습"
        state.step == Step.THINK -> "떠올려 보세요"
        else -> "듣는 중"
    }

    private companion object {
        const val SLOT_PREVIOUS = "previous"
        const val SLOT_CURRENT = "current"
        const val SLOT_NEXT = "next"
        const val CURRENT_INDEX = 1
    }
}
