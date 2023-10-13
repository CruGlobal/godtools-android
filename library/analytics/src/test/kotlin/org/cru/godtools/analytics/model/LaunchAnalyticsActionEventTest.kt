package org.cru.godtools.analytics.model

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.cru.godtools.shared.user.activity.UserCounterNames

class LaunchAnalyticsActionEventTest {
    @Test
    fun verifyActionName() {
        assertEquals("launch_1", LaunchAnalyticsActionEvent(1).action)
        assertEquals("launch_2", LaunchAnalyticsActionEvent(2).action)
        for (i in 3..4) assertEquals("launch_atleast_3", LaunchAnalyticsActionEvent(i).action)
        for (i in 5..9) assertEquals("launch_atleast_5", LaunchAnalyticsActionEvent(i).action)
        assertEquals("launch_atleast_10", LaunchAnalyticsActionEvent(10).action)
        assertEquals("launch_atleast_10", LaunchAnalyticsActionEvent(Random.nextInt(11, Int.MAX_VALUE)).action)
    }

    @Test
    fun `isForSystem(FIREBASE)`() {
        assertTrue(LaunchAnalyticsActionEvent(Random.nextInt(1, Int.MAX_VALUE)).isForSystem(AnalyticsSystem.FIREBASE))
        assertFalse(LaunchAnalyticsActionEvent(0).isForSystem(AnalyticsSystem.FIREBASE))
        assertFalse(LaunchAnalyticsActionEvent(Random.nextInt(Int.MIN_VALUE, 0)).isForSystem(AnalyticsSystem.FIREBASE))
    }

    @Test
    fun `isForSystem(USER)`() {
        assertTrue(LaunchAnalyticsActionEvent(0).isForSystem(AnalyticsSystem.USER))
    }

    @Test
    fun verifyUserCounterName() {
        val event = LaunchAnalyticsActionEvent(0)
        assertEquals(UserCounterNames.SESSION, event.userCounterName)
    }
}
