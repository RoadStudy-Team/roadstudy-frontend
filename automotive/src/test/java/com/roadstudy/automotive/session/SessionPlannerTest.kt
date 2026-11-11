package com.roadstudy.automotive.session

import com.roadstudy.automotive.session.TestFixtures.dbSection
import com.roadstudy.shared.model.ConceptStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPlannerTest {

    private val planner = SessionPlanner()
    private val statuses = mapOf(
        "c01" to ConceptStatus.LEARNED,
        "c02" to ConceptStatus.LEARNED,
        "c03" to ConceptStatus.NEEDS_REVIEW,
        "c04" to ConceptStatus.NEEDS_REVIEW
    )
    private val reviewQueue = listOf("c04", "c03")

    @Test fun toWorkPutsNewConceptsFirst() {
        val plan = planner.plan(dbSection, statuses, reviewQueue, etaSec = 38 * 60, tripType = TripType.TO_WORK)
        assertEquals(PlanItemKind.NEW, plan.items.first().kind)
        assertEquals("c05", plan.items.first().concept.id)
    }

    @Test fun toHomePutsReviewFirst() {
        val plan = planner.plan(dbSection, statuses, reviewQueue, etaSec = 42 * 60, tripType = TripType.TO_HOME)
        assertEquals(listOf("c04", "c03"), plan.items.take(2).map { it.concept.id })
    }

    @Test fun planFitsInsideUsableTime() {
        val eta = 20 * 60
        val plan = planner.plan(dbSection, statuses, reviewQueue, etaSec = eta, tripType = TripType.TO_WORK)
        assertTrue(plan.estimatedLearningSec <= eta * SessionPlanner.USABLE_RATIO)
    }

    @Test fun parkOnlyConceptsAreNeverPlayedWhileDriving() {
        val plan = planner.plan(dbSection, statuses, reviewQueue, etaSec = 3 * 60 * 60, tripType = TripType.TO_WORK)
        assertTrue(plan.items.none { it.concept.id == "c08" })
        assertEquals(listOf("c08"), plan.parkOnlyConcepts.map { it.id })
    }

    @Test fun otherTripIsCappedAtThirtyMinutes() {
        val plan = planner.plan(dbSection, statuses, reviewQueue, etaSec = 4 * 60 * 60, tripType = TripType.OTHER)
        assertTrue(plan.estimatedLearningSec <= SessionPlanner.OTHER_TRIP_CAP_SEC * SessionPlanner.USABLE_RATIO)
    }

    @Test fun shortTripHasNoSession() {
        val plan = planner.plan(dbSection, statuses, reviewQueue, etaSec = 10 * 60, tripType = TripType.SHORT)
        assertTrue(plan.items.isEmpty())
    }
}
