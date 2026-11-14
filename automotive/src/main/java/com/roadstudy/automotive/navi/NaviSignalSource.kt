package com.roadstudy.automotive.navi

import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.data.CautionPoint
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.DrivingInfo
import ai.pleos.playground.navi.data.TBTInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.roadstudy.automotive.session.NaviSignals

/**
 * Pleos NaviHelper SDK 에서 내비 이벤트를 받아 [NaviSignals] 로 알려 준다.
 *
 * - [start] 로 SDK 에 연결하고 리스너를 등록하며, [stop] 으로 해제한다 (화면이 보이는 동안만 쓴다).
 * - 이벤트는 내비 앱이 보낼 때만 온다. 경로 안내 중이 아니면 아무 값도 오지 않고, 그때는 기본값(경로 없음)이다.
 * - SDK 콜백은 다른 스레드에서 올 수 있어 메인 스레드로 옮겨서 처리한다.
 * - 연결되면 현재 목적지와 회전 정보를 한 번 요청해서, 앱을 늦게 켠 경우에도 값을 채운다.
 */
class NaviSignalSource(
    context: Context,
    private val onChanged: (NaviSignals) -> Unit,
    private val navi: NaviHelper = NaviHelper(context),
    private val aggregator: NaviSignalAggregator = NaviSignalAggregator()
) {
    private val main = Handler(Looper.getMainLooper())
    private var running = false
    private var delivered = false

    private val listener = object : NaviHelperEventListener {
        override fun onDestinationInfo(destinationInfo: DestinationInfo) = update("목적지 $destinationInfo") {
            onDestination(destinationInfo.name, destinationInfo.remainDistance, destinationInfo.remainTime)
        }

        override fun onDrivingInfo(drivingInfo: DrivingInfo) = update("주행 정보") {
            onDrivingEta(drivingInfo.destination.duration)
        }

        override fun onTBTInfo(tbtInfoList: List<TBTInfo>) = update(
            "회전 정보 ${tbtInfoList.size}개 " + tbtInfoList.take(4).joinToString { "${it.type}:${it.distance}m" }
        ) {
            // 에뮬레이터 실측: 목록은 앞 회전부터 순서대로이고 각 거리는 바로 앞 회전부터의 거리다 (700m, 50m, 110m...).
            // 그래서 다음 회전까지의 거리는 첫 항목이다
            onNextTurn(tbtInfoList.firstOrNull()?.distance?.toInt())
        }

        override fun onCautionPointInfo(cautionPointList: List<CautionPoint>) =
            update("주의 구간 ${cautionPointList.size}개") {
                onCautionPoints(cautionPointList.map { it.remainDistance })
            }

        override fun onReRouteEnded() = update("재탐색 완료") { onRerouted() }

        override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) =
            update("목적지 도착 ${destinationArrivedInfo.name}") { onArrived() }

        override fun onRouteCancelled() = update("경로 취소") { onRouteCancelled() }

        override fun onNavigationStatus(status: NavigationStatus) = update("내비 상태 $status") { onStatus(status) }
    }

    fun start() {
        if (running) return
        running = true
        delivered = false
        Log.d(TAG, "NaviHelper SDK 연결 시작")
        guarded("연결") {
            navi.addListener(listener)
            navi.initialize(
                onConnected = { main.post { if (running) requestCurrent() } },
                onFailed = { Log.w(TAG, "NaviHelper 연결 실패: ${it.message}") }
            )
        }
    }

    fun stop() {
        if (!running) return
        running = false
        guarded("해제") {
            navi.removeListener(listener)
            navi.release()
        }
        Log.d(TAG, "NaviHelper SDK 해제")
    }

    /** 이미 안내 중인 경로가 있으면 현재 값을 한 번 받아 온다 (결과는 리스너로 온다) */
    private fun requestCurrent() = guarded("현재 값 요청") {
        Log.d(TAG, "NaviHelper 연결됨, 현재 값 요청")
        navi.getDestinationInfo()
        navi.getTBTInfo()
    }

    /** 받은 값을 반영하고, 합쳐진 신호가 바뀌었을 때만 알린다 (메인 스레드에서 처리) */
    private fun update(description: String, apply: NaviSignalAggregator.() -> Unit) {
        main.post {
            if (!running) return@post
            val before = aggregator.signals
            aggregator.apply()
            val after = aggregator.signals
            Log.d(TAG, "$description -> $after")
            // 시작 후 첫 이벤트는 바뀐 게 없어도 전달한다. 앱이 다시 켜졌을 때 허브에 남은 지난 경로를 지우기 위해서다
            if (after != before || !delivered) onChanged(after)
            delivered = true
        }
    }

    /** 권한이 빠졌거나 서비스가 없을 때 앱이 죽지 않도록 막고 원인을 남긴다 */
    private inline fun guarded(what: String, block: () -> Unit) {
        try {
            block()
        } catch (e: SecurityException) {
            Log.w(TAG, "$what 실패(권한): ${e.message}")
        } catch (e: RuntimeException) {
            Log.w(TAG, "$what 실패: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "RoadStudyNavi"
    }
}
