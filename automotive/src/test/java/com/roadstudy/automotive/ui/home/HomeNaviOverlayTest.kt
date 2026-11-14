package com.roadstudy.automotive.ui.home

import com.roadstudy.automotive.session.CommuteInfo
import com.roadstudy.automotive.session.CommutePlace
import com.roadstudy.automotive.session.CommutePlaces
import com.roadstudy.automotive.session.CommuteTarget
import com.roadstudy.automotive.session.GeoPoint
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.RoutePreview
import com.roadstudy.automotive.session.TripType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.LocalTime

class HomeNaviOverlayTest {

    private val base = FakeHomeDataSource().load()

    @Test
    fun `내비 경로가 없으면 원래 입력 그대로다`() {
        assertSame(base, base.withRoute(NaviSignals()))
    }

    @Test
    fun `거리나 시간을 모르면 원래 입력 그대로다`() {
        assertSame(base, base.withRoute(NaviSignals(remainingMeters = 4_200)))
        assertSame(base, base.withRoute(NaviSignals(remainingSec = 600)))
    }

    @Test
    fun `내비 경로가 있으면 목적지와 남은 거리와 시간이 바뀐다`() {
        val input = base.withRoute(
            NaviSignals(remainingMeters = 23_000, remainingSec = 3_600, destinationName = "더현대서울")
        )

        assertEquals("더현대서울", input.destinationName)
        assertEquals(23_000, input.route?.remainingMeters)
        assertEquals(3_600, input.route?.etaSec)
    }

    @Test
    fun `내비가 목적지 이름을 안 주면 원래 이름을 쓴다`() {
        val input = base.withRoute(NaviSignals(remainingMeters = 23_000, remainingSec = 3_600))

        assertEquals(base.destinationName, input.destinationName)
    }

    @Test
    fun `정체 구간 수는 내비가 알려주지 않으므로 비운다`() {
        val input = base.withRoute(NaviSignals(remainingMeters = 23_000, remainingSec = 3_600))

        assertNull(input.route?.congestionCount)
    }

    @Test
    fun `출근 판별과 학습 자료는 바꾸지 않는다`() {
        val input = base.withRoute(NaviSignals(remainingMeters = 23_000, remainingSec = 3_600))

        assertEquals(base.tripType, input.tripType)
        assertEquals(base.sections, input.sections)
    }

    private val work = CommutePlace("이노그리드 본사", GeoPoint(37.5658, 126.9887), "work-poi")
    private val home = CommutePlace("집", GeoPoint(37.40, 127.10), "home-poi")
    private val places = CommutePlaces(home = home, work = work)
    private val toWork = RoutePreview(CommuteTarget.WORK, work, 25_212, 1_721, congestionCount = 2)
    private val toHome = RoutePreview(CommuteTarget.HOME, home, 9_000, 1_200, congestionCount = 0)
    private val both = CommuteInfo(places, mapOf(CommuteTarget.WORK to toWork, CommuteTarget.HOME to toHome))
    private val morning = LocalTime.of(8, 0)
    private val evening = LocalTime.of(19, 0)

    @Test
    fun `안내 중이 아니면 아침에는 회사까지의 미리보기를 보여 주고 출근으로 본다`() {
        val input = base.copy(tripType = TripType.OTHER).withRoute(NaviSignals(), both, time = morning)

        assertEquals(TripType.TO_WORK, input.tripType)
        assertEquals("이노그리드 본사", input.destinationName)
        assertEquals(RouteSummary(25_212, 1_721, 2), input.route)
    }

    @Test
    fun `저녁에는 집까지의 미리보기를 보여 주고 퇴근으로 본다`() {
        val input = base.withRoute(NaviSignals(), both, time = evening)

        assertEquals(TripType.TO_HOME, input.tripType)
        assertEquals("집", input.destinationName)
        assertEquals(RouteSummary(9_000, 1_200, 0), input.route)
    }

    @Test
    fun `사용자가 고른 쪽이 시간대보다 먼저다`() {
        val input = base.withRoute(NaviSignals(), both, chosen = CommuteTarget.HOME, time = morning)

        assertEquals(TripType.TO_HOME, input.tripType)
        assertEquals("집", input.destinationName)
    }

    @Test
    fun `고른 쪽이 등록돼 있지 않으면 등록된 쪽을 보여 준다`() {
        val onlyWork = CommuteInfo(CommutePlaces(work = work), mapOf(CommuteTarget.WORK to toWork))
        val input = base.withRoute(NaviSignals(), onlyWork, chosen = CommuteTarget.HOME, time = evening)

        assertEquals(TripType.TO_WORK, input.tripType)
        assertEquals("이노그리드 본사", input.destinationName)
    }

    @Test
    fun `미리보기가 아직 오지 않았으면 이름만 보이고 경로는 비운다`() {
        val input = base.withRoute(NaviSignals(), CommuteInfo(places), time = morning)

        assertEquals("이노그리드 본사", input.destinationName)
        assertEquals(TripType.TO_WORK, input.tripType)
        assertNull(input.route)
    }

    @Test
    fun `안내 중인 경로가 미리보기보다 먼저다`() {
        val navi = NaviSignals(
            remainingMeters = 23_000, remainingSec = 3_600, destinationName = "더현대서울",
            destinationPoint = GeoPoint(37.5259, 126.9284)
        )
        val input = base.withRoute(navi, both, time = morning)

        assertEquals("더현대서울", input.destinationName)
        assertEquals(23_000, input.route?.remainingMeters)
        assertEquals(TripType.OTHER, input.tripType)
    }

    @Test
    fun `안내 중인 목적지가 회사 근처면 출근, 집 근처면 퇴근이다`() {
        val toWorkNavi = NaviSignals(remainingMeters = 25_000, remainingSec = 1_740, destinationPoint = GeoPoint(37.565625, 126.98898))
        val toHomeNavi = toWorkNavi.copy(destinationPoint = GeoPoint(37.4001, 127.1001))

        assertEquals(TripType.TO_WORK, base.copy(tripType = TripType.OTHER).withRoute(toWorkNavi, CommuteInfo(places)).tripType)
        assertEquals(TripType.TO_HOME, base.withRoute(toHomeNavi, CommuteInfo(places)).tripType)
    }

    @Test
    fun `즐겨찾기를 받았는데 집·회사가 모두 없으면 경로를 비우고 기타 이동으로 본다`() {
        val input = base.withRoute(NaviSignals(), CommuteInfo(CommutePlaces()))

        assertNull(input.route)
        assertEquals("", input.destinationName)
        assertEquals(TripType.OTHER, input.tripType)
        assertEquals(base.sections, input.sections)
    }

    @Test
    fun `회사·집 버튼은 고른 쪽을 표시하고 등록하지 않은 쪽은 누를 수 없다`() {
        val toggle = commuteToggle(NaviSignals(), CommuteInfo(CommutePlaces(work = work)), chosen = null, time = evening)

        assertEquals(CommuteToggleUi(selected = CommuteTarget.WORK, workEnabled = true, homeEnabled = false), toggle)
        assertEquals(
            CommuteTarget.HOME,
            commuteToggle(NaviSignals(), both, chosen = CommuteTarget.HOME, time = morning)?.selected
        )
    }

    @Test
    fun `내비 안내 중이거나 즐겨찾기가 없으면 회사·집 버튼을 숨긴다`() {
        val guiding = NaviSignals(remainingMeters = 1_000, remainingSec = 120)

        assertNull(commuteToggle(guiding, both, chosen = null, time = morning))
        assertNull(commuteToggle(NaviSignals(), CommuteInfo(), chosen = null, time = morning))
        assertNull(commuteToggle(NaviSignals(), CommuteInfo(CommutePlaces()), chosen = null, time = morning))
    }

    @Test
    fun `내비 경로의 시간이 길수록 오늘의 학습 분량도 달라진다`() {
        val mapper = HomeUiMapper()
        val now = LocalTime.of(8, 0)
        val short = mapper.map(base.withRoute(NaviSignals(remainingMeters = 2_000, remainingSec = 5 * 60)), now)
        val long = mapper.map(base.withRoute(NaviSignals(remainingMeters = 60_000, remainingSec = 90 * 60)), now)

        assertEquals(true, long.learningConceptCount > short.learningConceptCount)
        assertEquals("08:05", short.arrivalTime)
    }
}
