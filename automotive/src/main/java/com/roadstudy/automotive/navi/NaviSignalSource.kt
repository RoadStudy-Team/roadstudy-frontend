package com.roadstudy.automotive.navi

import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.data.BookmarkInfo
import ai.pleos.playground.navi.data.CautionPoint
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.DrivingInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.TBTInfo
import ai.pleos.playground.navi.data.TripInfo
import ai.pleos.playground.navi.data.TripRequest
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.roadstudy.automotive.session.CommuteInfo
import com.roadstudy.automotive.session.CommutePlace
import com.roadstudy.automotive.session.CommutePlaces
import com.roadstudy.automotive.session.CommuteTarget
import com.roadstudy.automotive.session.CommuteTargetPicker
import com.roadstudy.automotive.session.NaviSignals
import java.time.LocalTime

/**
 * Pleos NaviHelper SDK 에서 내비 이벤트를 받아 [NaviSignals] 로 알려 준다.
 *
 * - [start] 로 SDK 에 연결하고 리스너를 등록하며, [stop] 으로 해제한다 (화면이 보이는 동안만 쓴다).
 * - 이벤트는 내비 앱이 보낼 때만 온다. 경로 안내 중이 아니면 아무 값도 오지 않고, 그때는 기본값(경로 없음)이다.
 * - SDK 콜백은 다른 스레드에서 올 수 있어 메인 스레드로 옮겨서 처리한다.
 * - 연결되면 현재 목적지와 회전 정보를 한 번 요청해서, 앱을 늦게 켠 경우에도 값을 채운다.
 * - 연결되면 즐겨찾기(집·회사)도 요청하고, 등록된 집·회사까지의 경로를 안내 없이 하나씩 물어 봐서([NaviHelper.requestTrip])
 *   받는 대로 [onCommute] 로 알린다. 경로 안내를 시작하지 않으므로 내비 화면은 바뀌지 않는다.
 */
class NaviSignalSource(
    context: Context,
    private val onChanged: (NaviSignals) -> Unit,
    private val onCommute: (CommuteInfo) -> Unit,
    private val navi: NaviHelper = NaviHelper(context),
    private val aggregator: NaviSignalAggregator = NaviSignalAggregator(),
    private val clock: () -> LocalTime = { LocalTime.now() }
) {
    private val main = Handler(Looper.getMainLooper())
    private var running = false
    private var delivered = false

    /** 지금까지 받은 즐겨찾기와 경로 미리보기 */
    private var commute = CommuteInfo()

    /**
     * 아직 미리보기를 요청하지 않은 집·회사와 응답을 기다리는 쪽.
     * requestTrip 결과에는 어느 목적지에 대한 것인지 표시가 없어서, 한 번에 하나씩 요청하고 답이 오면 다음을 요청한다.
     */
    private val previewQueue = ArrayDeque<CommuteTarget>()
    private var awaitingTarget: CommuteTarget? = null

    /** 미리보기 응답이 오지 않으면 그쪽은 건너뛰고 다음을 요청한다 (내비가 requestTrip 에 응답하지 않는 경우) */
    private val previewTimeout = Runnable {
        Log.w(TAG, "경로 미리보기 응답 없음: $awaitingTarget")
        awaitingTarget = null
        requestNextPreview()
    }

    private val listener = object : NaviHelperEventListener {
        override fun onDestinationInfo(destinationInfo: DestinationInfo) = update("목적지 $destinationInfo") {
            onDestination(
                destinationInfo.name,
                destinationInfo.remainDistance,
                destinationInfo.remainTime,
                CommuteMapper.toPoint(destinationInfo.latitude, destinationInfo.longitude)
            )
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

        override fun onBookmarkInfo(bookmarkInfoList: List<BookmarkInfo>) {
            main.post { if (running) onPlaces(CommuteMapper.toPlaces(bookmarkInfoList)) }
        }

        override fun onBookMarkChanged() {
            main.post { if (running) guarded("즐겨찾기 다시 요청") { navi.getBookmarkInfo() } }
        }

        override fun onTripInfo(tripInfo: TripInfo) {
            main.post { if (running) onPreview(tripInfo) }
        }
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
        main.removeCallbacks(previewTimeout)
        previewQueue.clear()
        awaitingTarget = null
        guarded("해제") {
            navi.removeListener(listener)
            navi.release()
        }
        Log.d(TAG, "NaviHelper SDK 해제")
    }

    /**
     * [place] 로 경로 안내를 시작한다 (권한 NAVI_ROUTE_SEARCH).
     * 에뮬레이터 실측: 호출하면 내비 앱이 앞으로 나오고 바로 안내를 시작한다. 결과(남은 거리 등)는 기존 리스너로 온다.
     */
    fun requestRoute(place: CommutePlace) = guarded("경로 안내 요청") {
        Log.d(TAG, "경로 안내 요청 -> ${place.name}")
        navi.requestRoute(
            RouteInfo(
                latitude = place.point.latitude,
                longitude = place.point.longitude,
                poiName = place.name,
                poiId = place.poiId,
                address = "",
                poiSubId = place.poiSubId.orEmpty()
            )
        )
    }

    /** 이미 안내 중인 경로가 있으면 현재 값을 한 번 받아 온다 (결과는 리스너로 온다) */
    private fun requestCurrent() = guarded("현재 값 요청") {
        Log.d(TAG, "NaviHelper 연결됨, 현재 값 요청")
        navi.getDestinationInfo()
        navi.getTBTInfo()
        navi.getBookmarkInfo()
    }

    /** 즐겨찾기를 받으면 바로 알리고(이름이 먼저 보이게), 등록된 집·회사까지의 경로를 시간대에 맞는 쪽부터 물어 본다 */
    private fun onPlaces(places: CommutePlaces) {
        val order = CommuteTargetPicker.requestOrder(places, clock())
        Log.d(TAG, "즐겨찾기 집=${places.home?.name} 회사=${places.work?.name} -> 미리보기 순서 $order")
        // 즐겨찾기가 그대로면 이미 받은 미리보기는 그대로 두고 다시 받는다
        commute = CommuteInfo(places, commute.previews.filterKeys { it in order && commute.places == places })
        onCommute(commute)
        main.removeCallbacks(previewTimeout)
        awaitingTarget = null
        previewQueue.clear()
        previewQueue.addAll(order)
        requestNextPreview()
    }

    private fun requestNextPreview() {
        val places = commute.places ?: return
        val target = previewQueue.removeFirstOrNull() ?: return
        val place = CommuteTargetPicker.placeOf(places, target) ?: return requestNextPreview()
        awaitingTarget = target
        main.postDelayed(previewTimeout, PREVIEW_TIMEOUT_MS)
        guarded("경로 미리보기 요청") { navi.requestTrip(place.toTripRequest()) }
    }

    private fun onPreview(trip: TripInfo) {
        val places = commute.places ?: return
        val target = awaitingTarget ?: return
        val place = CommuteTargetPicker.placeOf(places, target) ?: return
        main.removeCallbacks(previewTimeout)
        awaitingTarget = null
        val preview = CommuteMapper.toPreview(target, place, trip.distance, trip.eta, trip.summary.trafficConditions)
        Log.d(TAG, "경로 미리보기 $preview")
        commute = commute.copy(previews = commute.previews + (target to preview))
        onCommute(commute)
        requestNextPreview()
    }

    private fun CommutePlace.toTripRequest() =
        TripRequest(point.latitude, point.longitude, poiId, name, poiSubId)

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

        /** 에뮬레이터에서는 1초 안에 응답이 왔다. 이 시간 안에 오지 않으면 그쪽 미리보기 없이 진행한다 */
        const val PREVIEW_TIMEOUT_MS = 5_000L
    }
}
