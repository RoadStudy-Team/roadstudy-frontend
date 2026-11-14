package com.roadstudy.automotive.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class CommutePlacesTest {

    private val home = CommutePlace("집", GeoPoint(37.40, 127.10), "home-poi")
    private val work = CommutePlace("이노그리드 본사", GeoPoint(37.5658, 126.9887), "work-poi")
    private val both = CommutePlaces(home = home, work = work)

    @Test
    fun `오후 2시 전에는 회사, 그 뒤에는 집을 미리 보여 준다`() {
        assertEquals(CommuteTarget.WORK, CommuteTargetPicker.pick(both, LocalTime.of(8, 0)))
        assertEquals(CommuteTarget.WORK, CommuteTargetPicker.pick(both, LocalTime.of(13, 59)))
        assertEquals(CommuteTarget.HOME, CommuteTargetPicker.pick(both, LocalTime.of(14, 0)))
        assertEquals(CommuteTarget.HOME, CommuteTargetPicker.pick(both, LocalTime.of(19, 0)))
    }

    @Test
    fun `시간대에 맞는 쪽이 없으면 등록된 다른 쪽을 고른다`() {
        assertEquals(CommuteTarget.WORK, CommuteTargetPicker.pick(CommutePlaces(work = work), LocalTime.of(19, 0)))
        assertEquals(CommuteTarget.HOME, CommuteTargetPicker.pick(CommutePlaces(home = home), LocalTime.of(8, 0)))
    }

    @Test
    fun `집과 회사가 모두 없으면 고르지 않는다`() {
        assertNull(CommuteTargetPicker.pick(CommutePlaces(), LocalTime.of(8, 0)))
    }

    @Test
    fun `사용자가 고른 쪽이 등록돼 있으면 시간대보다 먼저다`() {
        assertEquals(CommuteTarget.HOME, CommuteTargetPicker.resolve(both, CommuteTarget.HOME, LocalTime.of(8, 0)))
        assertEquals(CommuteTarget.WORK, CommuteTargetPicker.resolve(both, null, LocalTime.of(8, 0)))
        assertEquals(
            CommuteTarget.WORK,
            CommuteTargetPicker.resolve(CommutePlaces(work = work), CommuteTarget.HOME, LocalTime.of(19, 0))
        )
    }

    @Test
    fun `미리보기는 시간대에 맞는 쪽부터 등록된 쪽만 요청한다`() {
        assertEquals(listOf(CommuteTarget.HOME, CommuteTarget.WORK), CommuteTargetPicker.requestOrder(both, LocalTime.of(19, 0)))
        assertEquals(listOf(CommuteTarget.WORK), CommuteTargetPicker.requestOrder(CommutePlaces(work = work), LocalTime.of(19, 0)))
        assertEquals(emptyList<CommuteTarget>(), CommuteTargetPicker.requestOrder(CommutePlaces(), LocalTime.of(8, 0)))
    }

    @Test
    fun `이어진 혼잡 구간은 한 곳으로 센다`() {
        assertEquals(0, countCongestionZones(emptyList()))
        assertEquals(0, countCongestionZones(listOf(false, false)))
        assertEquals(1, countCongestionZones(listOf(true, true, false)))
        assertEquals(2, countCongestionZones(listOf(true, false, true, true)))
        assertEquals(2, countCongestionZones(listOf(false, true, false, true)))
    }

    @Test
    fun `목적지가 회사 근처면 출근, 집 근처면 퇴근, 아니면 기타 이동이다`() {
        val detector = TripTypeDetector()

        assertEquals(TripType.TO_WORK, detector.classifyDestination(both, GeoPoint(37.565625, 126.98898)))
        assertEquals(TripType.TO_HOME, detector.classifyDestination(both, GeoPoint(37.4001, 127.1001)))
        assertEquals(TripType.OTHER, detector.classifyDestination(both, GeoPoint(37.505, 127.004)))
    }

    @Test
    fun `등록하지 않은 쪽은 비교하지 않는다`() {
        val onlyWork = CommutePlaces(work = work)

        assertEquals(TripType.OTHER, TripTypeDetector().classifyDestination(onlyWork, home.point))
        assertEquals(TripType.OTHER, TripTypeDetector().classifyDestination(CommutePlaces(), work.point))
    }
}
