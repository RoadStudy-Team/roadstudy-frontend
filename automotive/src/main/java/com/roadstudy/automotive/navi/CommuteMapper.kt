package com.roadstudy.automotive.navi

import ai.pleos.playground.navi.constants.BookmarkType
import ai.pleos.playground.navi.constants.TrafficCongestionType
import ai.pleos.playground.navi.data.BookmarkInfo
import ai.pleos.playground.navi.data.TrafficInfo
import com.roadstudy.automotive.session.CommutePlace
import com.roadstudy.automotive.session.CommutePlaces
import com.roadstudy.automotive.session.CommuteTarget
import com.roadstudy.automotive.session.GeoPoint
import com.roadstudy.automotive.session.RoutePreview
import com.roadstudy.automotive.session.countCongestionZones

/** NaviHelper SDK 의 즐겨찾기·경로 미리보기 값을 앱의 값으로 바꾼다 */
object CommuteMapper {

    /**
     * 즐겨찾기 목록에서 집(HOME)과 회사(OFFICE)를 고른다.
     * 에뮬레이터 실측: 등록하지 않은 집·회사도 이름이 비고 좌표가 (0, 0)인 항목으로 온다. 이것은 등록되지 않은 것으로 본다.
     */
    fun toPlaces(bookmarks: List<BookmarkInfo>): CommutePlaces = CommutePlaces(
        home = bookmarks.firstRegistered(BookmarkType.HOME),
        work = bookmarks.firstRegistered(BookmarkType.OFFICE)
    )

    /** 좌표가 (0, 0)이면 내비가 값을 비워 보낸 것으로 보고 null 로 바꾼다 */
    fun toPoint(latitude: Double, longitude: Double): GeoPoint? =
        if (latitude == 0.0 && longitude == 0.0) null else GeoPoint(latitude, longitude)

    /**
     * requestTrip 결과를 경로 미리보기로 바꾼다.
     * 에뮬레이터 실측: distance 는 m, eta 는 걸리는 시간(초)이다 (25212, 1721 -> 내비 화면의 25km, 29분).
     * 혼잡도는 경로를 같은 길이로 나눈 구간마다 오므로, 서행·정체가 이어진 구간을 한 곳으로 센다.
     */
    fun toPreview(
        target: CommuteTarget,
        place: CommutePlace,
        distanceMeters: Int,
        etaSec: Int,
        traffic: List<TrafficInfo>
    ): RoutePreview = RoutePreview(
        target = target,
        place = place,
        remainingMeters = distanceMeters,
        etaSec = etaSec,
        congestionCount = countCongestionZones(traffic.sortedBy { it.startRatio }.map { it.congestionType.isCongested() })
    )

    private fun TrafficCongestionType.isCongested(): Boolean =
        this == TrafficCongestionType.SLOW || this == TrafficCongestionType.HEAVY

    private fun List<BookmarkInfo>.firstRegistered(type: BookmarkType): CommutePlace? =
        filter { it.category == type && it.name.isNotBlank() }
            .firstNotNullOfOrNull { bookmark ->
                toPoint(bookmark.latitude, bookmark.longitude)?.let { point ->
                    CommutePlace(bookmark.name, point, bookmark.poiId, bookmark.poiSubId)
                }
            }
}
