package com.roadstudy.automotive.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.roadstudy.automotive.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * 주행 학습을 차량 미디어 시스템에 연결하는 서비스 (Pleos Music/Audio 가이드의 필수 요건: MediaBrowserService + MediaSession).
 *
 * - 세션은 [LearningHub] 가 만들고, 이 서비스는 그 상태를 [LearningPlayer] 로 시스템에 보여 주며 운전대 버튼 명령을 받는다.
 * - 세션 동안 포그라운드 서비스로 둔다. 에뮬레이터 실측: 포그라운드가 아니면 내비가 앞에 나온 뒤 안드로이드가
 *   우리 앱을 얼렸다가(freeze) 종료해서 학습이 문장 중간에 멈췄다. Media3 의 자동 알림·포그라운드 처리는 일시정지 때
 *   포그라운드를 내려서 같은 일이 생길 수 있어 끄고([onUpdateNotification]), 시작할 때 직접 올린다(Pleos 가이드와 같음).
 * - 가이드에 따라 연결만으로는 자동 재생하지 않는다. 세션은 사용자가 시작 버튼을 누르거나 출발(D단)했을 때만 시작한다.
 * - 세션이 끝나면 스스로 멈춘다.
 */
@UnstableApi
class LearningPlaybackService : MediaLibraryService() {

    private var player: LearningPlayer? = null
    private var librarySession: MediaLibrarySession? = null

    override fun onCreate() {
        super.onCreate()
        val created = LearningPlayer()
        player = created
        librarySession = MediaLibrarySession.Builder(this, created, LibraryCallback()).build()
        LearningHub.onChanged = {
            created.refresh()
            if (LearningHub.session == null) {
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        Log.d(TAG, "미디어 서비스 시작")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = librarySession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        startInForeground()
        return result
    }

    /** Media3 가 재생 상태에 따라 알림·포그라운드를 바꾸지 않게 한다 (세션 동안 계속 포그라운드로 둔다) */
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) = Unit

    private fun startInForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(LearningHub.session?.request?.sectionTitle.orEmpty())
            .setOngoing(true)
            .build()
        runCatching {
            ServiceCompat.startForeground(
                this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        }.onFailure { Log.w(TAG, "포그라운드 전환 실패: ${it.message}") }
    }

    /** 앱을 최근 목록에서 지우면 세션도 끝낸다 (Pleos 가이드: 정리 후 stopSelf) */
    override fun onTaskRemoved(rootIntent: Intent?) {
        LearningHub.stop()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        LearningHub.onChanged = null
        librarySession?.release()
        librarySession = null
        player?.release()
        player = null
        Log.d(TAG, "미디어 서비스 끝")
        super.onDestroy()
    }

    /**
     * 미디어 목록: 루트 아래에 "오늘의 학습" 하나만 둔다. 학습은 앱에서 시작하고, 목록에서 고르는 기능은 아직 없다.
     * 에뮬레이터 실측: 루트 아래 목록이 비어 있으면 시스템 미디어 앱(ai.umos.mediaapp)이 우리 앱에 연결하면서 반복해서 죽는다.
     */
    private class LibraryCallback : MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val root = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("로드스터디")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(root, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val children = if (parentId == ROOT_ID) ImmutableList.of(todayItem()) else ImmutableList.of()
            return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> =
            Futures.immediateFuture(LibraryResult.ofItem(todayItem(), null))

        private fun todayItem() = MediaItem.Builder()
            .setMediaId(TODAY_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(LearningHub.session?.request?.sectionTitle ?: "오늘의 학습")
                    .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST_EPISODE)
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .build()
            )
            .build()
    }

    private companion object {
        const val TAG = "RoadStudyMedia"
        const val CHANNEL_ID = "learning"
        const val NOTIFICATION_ID = 28
        const val ROOT_ID = "root"
        const val TODAY_ID = "today"
    }
}
