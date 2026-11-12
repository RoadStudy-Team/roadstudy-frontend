package com.roadstudy.automotive.ui.driving

import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.LearningMode
import com.roadstudy.automotive.session.ModeReason
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.VehicleSignals
import com.roadstudy.automotive.session.WiperLevel
import com.roadstudy.automotive.session.Step
import com.roadstudy.automotive.ui.home.FakeHomeDataSource
import com.roadstudy.shared.model.SampleStudySections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrivingDemoTest {

    private val demo = DrivingDemo(FakeHomeDataSource().load(), SampleStudySections.dbNormalization.sectionId)

    @Test
    fun `안전 상황은 첫 개념의 설명을 듣는 상태다`() {
        val state = demo.run(DrivingScenario.SAFE)

        assertEquals(LearningMode.NORMAL, state.mode)
        assertEquals(ModeReason.SAFE, state.reason)
        assertEquals(Step.EXPLAIN, state.step)
        assertEquals(1, state.conceptIndex)
        // 샘플 경로 38분에 들을 수 있는 개념은 4개
        assertEquals(4, state.conceptTotal)
    }

    @Test
    fun `떠올리기 상황은 질문을 끝낸 뒤의 단계다`() {
        val state = demo.run(DrivingScenario.THINKING)

        assertEquals(LearningMode.NORMAL, state.mode)
        assertEquals(Step.THINK, state.step)
    }

    @Test
    fun `야간 상황은 주의 모드다`() {
        val state = demo.run(DrivingScenario.NIGHT)

        assertEquals(LearningMode.CAUTION, state.mode)
        assertEquals(ModeReason.NIGHT, state.reason)
    }

    @Test
    fun `회전 앞 상황은 집중 모드이고 진행 중이던 개념이 유지된다`() {
        val state = demo.run(DrivingScenario.TURN_AHEAD)

        assertEquals(LearningMode.FOCUS, state.mode)
        assertEquals(ModeReason.UPCOMING_TURN, state.reason)
        assertEquals(1, state.conceptIndex)
    }

    @Test
    fun `도착 상황은 주차 모드이고 세션이 끝난 상태다`() {
        val state = demo.run(DrivingScenario.ARRIVED)

        assertEquals(LearningMode.PARKED, state.mode)
        assertEquals(ModeReason.PARKED, state.reason)
        assertTrue(state.finished)
    }

    // ----- 실제 차량 신호 (라이브) -----

    private fun live(
        vehicle: VehicleSignals,
        hasDriven: Boolean = true,
        navi: NaviSignals = NaviSignals()
    ) = demo.runLive(vehicle, navi, hasDriven, nowMillis = 0L)

    @Test
    fun `내비 경로가 있으면 남은 거리와 시간은 내비 값이다`() {
        val state = live(
            VehicleSignals(gear = Gear.DRIVE),
            navi = NaviSignals(remainingSec = 600, remainingMeters = 4_200)
        )

        assertEquals(4_200, state.remainingMeters)
        assertEquals(600, state.remainingSec)
    }

    @Test
    fun `내비 경로가 없으면 가짜 경로의 남은 거리와 시간을 쓴다`() {
        val state = live(VehicleSignals(gear = Gear.DRIVE))

        assertTrue(state.remainingMeters != null)
        assertTrue(state.remainingSec != null)
    }

    @Test
    fun `내비의 회전 직전 신호는 집중 모드로 이어진다`() {
        val state = live(
            VehicleSignals(gear = Gear.DRIVE),
            navi = NaviSignals(remainingSec = 600, remainingMeters = 4_200, distanceToNextTurnMeters = 250)
        )

        assertEquals(LearningMode.FOCUS, state.mode)
        assertEquals(ModeReason.UPCOMING_TURN, state.reason)
    }

    @Test
    fun `내비 경로가 없으면 남아 있는 회전과 주의 구간 신호는 무시한다`() {
        val state = live(
            VehicleSignals(gear = Gear.DRIVE),
            navi = NaviSignals(distanceToNextTurnMeters = 100, inCautionZone = true)
        )

        assertEquals(LearningMode.NORMAL, state.mode)
        assertEquals(ModeReason.SAFE, state.reason)
    }

    @Test
    fun `내비의 주의 구간 신호는 집중 모드로 이어진다`() {
        val state = live(
            VehicleSignals(gear = Gear.DRIVE),
            navi = NaviSignals(remainingSec = 600, remainingMeters = 4_200, inCautionZone = true)
        )

        assertEquals(ModeReason.CAUTION_ZONE, state.reason)
    }

    @Test
    fun `목적지에서 먼 곳의 P단은 도착이 아니라 잠시 정차다`() {
        val state = live(
            VehicleSignals(gear = Gear.PARK),
            navi = NaviSignals(remainingSec = 600, remainingMeters = 4_200)
        )

        assertFalse(state.finished)
        assertEquals(LearningMode.NORMAL, state.mode)
    }

    @Test
    fun `목적지 근처의 P단은 도착이다`() {
        val state = live(
            VehicleSignals(gear = Gear.PARK),
            navi = NaviSignals(remainingSec = 30, remainingMeters = 120)
        )

        assertEquals(LearningMode.PARKED, state.mode)
        assertTrue(state.finished)
    }

    @Test
    fun `실제 신호가 주행이고 이상이 없으면 안전 구간이다`() {
        val state = live(VehicleSignals(gear = Gear.DRIVE))

        assertEquals(LearningMode.NORMAL, state.mode)
        assertEquals(ModeReason.SAFE, state.reason)
    }

    @Test
    fun `와이퍼 강은 집중, 약은 주의로 이어진다`() {
        val heavy = live(VehicleSignals(gear = Gear.DRIVE, wiperLevel = WiperLevel.HIGH))
        val light = live(VehicleSignals(gear = Gear.DRIVE, wiperLevel = WiperLevel.LOW))

        assertEquals(LearningMode.FOCUS, heavy.mode)
        assertEquals(ModeReason.HEAVY_RAIN, heavy.reason)
        assertEquals(LearningMode.CAUTION, light.mode)
        assertEquals(ModeReason.LIGHT_RAIN, light.reason)
    }

    @Test
    fun `하향등은 야간 주의, 안개등은 집중으로 이어진다`() {
        val night = live(VehicleSignals(gear = Gear.DRIVE, lowBeamOn = true))
        val fog = live(VehicleSignals(gear = Gear.DRIVE, fogLightOn = true))

        assertEquals(ModeReason.NIGHT, night.reason)
        assertEquals(LearningMode.CAUTION, night.mode)
        assertEquals(ModeReason.FOG, fog.reason)
        assertEquals(LearningMode.FOCUS, fog.mode)
    }

    @Test
    fun `주행한 뒤 P단이 되면 도착이다`() {
        val state = live(VehicleSignals(gear = Gear.PARK), hasDriven = true)

        assertEquals(LearningMode.PARKED, state.mode)
        assertTrue(state.finished)
    }

    @Test
    fun `아직 주행한 적이 없으면 P단이어도 도착으로 보지 않는다`() {
        // 시작 버튼으로 들어왔지만 아직 출발 전인 상황
        val state = live(VehicleSignals(gear = Gear.PARK), hasDriven = false)

        assertEquals(LearningMode.NORMAL, state.mode)
        assertFalse(state.finished)
    }

    @Test
    fun `같은 상황은 몇 번을 돌려도 같은 결과다`() {
        assertEquals(demo.run(DrivingScenario.NIGHT), demo.run(DrivingScenario.NIGHT))
    }

    @Test
    fun `상황은 순서대로 돌고 마지막 다음은 처음이다`() {
        assertEquals(DrivingScenario.THINKING, DrivingScenario.SAFE.next())
        assertEquals(DrivingScenario.ARRIVED, DrivingScenario.TURN_AHEAD.next())
        assertEquals(DrivingScenario.SAFE, DrivingScenario.ARRIVED.next())
    }

    @Test
    fun `실행 인자의 이름으로 상황을 고르고 없으면 안전 상황이다`() {
        assertEquals(DrivingScenario.NIGHT, DrivingScenario.fromKey("night"))
        assertEquals(DrivingScenario.TURN_AHEAD, DrivingScenario.fromKey("TURN_AHEAD"))
        assertEquals(DrivingScenario.SAFE, DrivingScenario.fromKey(null))
        assertEquals(DrivingScenario.SAFE, DrivingScenario.fromKey("엉뚱한값"))
    }

    @Test
    fun `섹션이 없으면 개념이 없고 제목도 비어 있다`() {
        val emptyDemo = DrivingDemo(FakeHomeDataSource(startEmpty = true).load(), null)

        assertEquals("", emptyDemo.sectionTitle)
        assertFalse(emptyDemo.run(DrivingScenario.SAFE).conceptTotal > 0)
    }
}
