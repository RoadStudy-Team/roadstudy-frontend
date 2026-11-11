package com.roadstudy.automotive.session

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class TripTypeDetectorTest {

    private val home = GeoPoint(37.5563, 126.9220)
    private val work = GeoPoint(37.3947, 127.1112)
    private val elsewhere = GeoPoint(37.4979, 127.0276)
    private val routine = CommuteRoutine(home, work)
    private val detector = TripTypeDetector()

    @Test fun destinationWorkIsToWork() = assertEquals(
        TripType.TO_WORK,
        detector.detect(routine, home, work, 38 * 60, LocalTime.of(8, 10), DayOfWeek.MONDAY)
    )

    @Test fun destinationHomeIsToHomeEvenLateAtNight() = assertEquals(
        TripType.TO_HOME,
        detector.detect(routine, work, home, 42 * 60, LocalTime.of(23, 10), DayOfWeek.MONDAY)
    )

    @Test fun noDestinationUsesStartAndTime() = assertEquals(
        TripType.TO_WORK,
        detector.detect(routine, home, null, null, LocalTime.of(7, 50), DayOfWeek.TUESDAY)
    )

    @Test fun weekendWithoutDestinationIsOther() = assertEquals(
        TripType.OTHER,
        detector.detect(routine, home, null, null, LocalTime.of(10, 0), DayOfWeek.SATURDAY)
    )

    @Test fun otherDestinationIsOther() = assertEquals(
        TripType.OTHER,
        detector.detect(routine, work, elsewhere, 46 * 60, LocalTime.of(14, 0), DayOfWeek.WEDNESDAY)
    )

    @Test fun shortTripIsShort() = assertEquals(
        TripType.SHORT,
        detector.detect(routine, home, elsewhere, 8 * 60, LocalTime.of(14, 0), DayOfWeek.WEDNESDAY)
    )
}
