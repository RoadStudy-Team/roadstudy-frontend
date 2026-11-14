package com.roadstudy.automotive.navi

import ai.pleos.playground.navi.constants.BookmarkType
import ai.pleos.playground.navi.constants.TrafficCongestionType
import ai.pleos.playground.navi.data.BookmarkInfo
import ai.pleos.playground.navi.data.TrafficInfo
import com.roadstudy.automotive.session.CommuteTarget
import com.roadstudy.automotive.session.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommuteMapperTest {

    private fun bookmark(type: BookmarkType, name: String, lat: Double, lon: Double) = BookmarkInfo(
        latitude = lat, longitude = lon, name = name, poiId = if (name.isBlank()) "" else "poi-$name",
        poiSubId = null, address = "", category = type
    )

    @Test
    fun `에뮬레이터처럼 집은 빈 항목, 회사만 등록돼 있으면 회사만 받는다`() {
        val places = CommuteMapper.toPlaces(
            listOf(
                bookmark(BookmarkType.HOME, "", 0.0, 0.0),
                bookmark(BookmarkType.OFFICE, "이노그리드 본사", 37.56581944, 126.98872222)
            )
        )

        assertNull(places.home)
        assertEquals("이노그리드 본사", places.work?.name)
        assertEquals("poi-이노그리드 본사", places.work?.poiId)
        assertEquals(GeoPoint(37.56581944, 126.98872222), places.work?.point)
    }

    @Test
    fun `이름이 있어도 좌표가 비어 있으면 등록되지 않은 것으로 본다`() {
        val places = CommuteMapper.toPlaces(listOf(bookmark(BookmarkType.HOME, "집", 0.0, 0.0)))

        assertNull(places.home)
    }

    @Test
    fun `집·회사가 아닌 즐겨찾기는 쓰지 않는다`() {
        val places = CommuteMapper.toPlaces(
            listOf(
                bookmark(BookmarkType.DEFAULT, "카페", 37.5, 127.0),
                bookmark(BookmarkType.EV_CHARGING_STATION, "충전소", 37.5, 127.0)
            )
        )

        assertEquals(true, places.isEmpty)
    }

    @Test
    fun `경로 미리보기의 정체 구간은 서행과 정체가 이어진 구간을 한 곳으로 센다`() {
        val work = CommuteMapper.toPlaces(listOf(bookmark(BookmarkType.OFFICE, "회사", 37.5, 127.0))).work!!
        val traffic = listOf(
            TrafficInfo(TrafficCongestionType.SLOW, 0, 4),
            TrafficInfo(TrafficCongestionType.HEAVY, 4, 8),
            TrafficInfo(TrafficCongestionType.FAST, 8, 12),
            TrafficInfo(TrafficCongestionType.MODERATE, 12, 16),
            TrafficInfo(TrafficCongestionType.SLOW, 16, 20),
            TrafficInfo(TrafficCongestionType.UNKNOWN, 20, 24)
        )

        val preview = CommuteMapper.toPreview(CommuteTarget.WORK, work, 25_212, 1_721, traffic.reversed())

        assertEquals(2, preview.congestionCount)
        assertEquals(25_212, preview.remainingMeters)
        assertEquals(1_721, preview.etaSec)
    }

    @Test
    fun `좌표가 0, 0 이면 모르는 좌표다`() {
        assertNull(CommuteMapper.toPoint(0.0, 0.0))
        assertEquals(GeoPoint(37.5, 127.0), CommuteMapper.toPoint(37.5, 127.0))
    }
}
