package com.pedro.planmuscle.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HydrationEngineTest {

    @Test
    fun `interpolated volume at 06 45 is 0 ml`() {
        val expected = HydrationEngine.getInterpolatedExpectedVolume(6, 45, 2600)
        assertEquals(0, expected)
    }

    @Test
    fun `interpolated volume at end of day is full target`() {
        val expected = HydrationEngine.getInterpolatedExpectedVolume(22, 10, 2600)
        assertEquals(2600, expected)
    }

    @Test
    fun `status message shows current and target volume`() {
        val message = HydrationEngine.getHydrationStatusMessage(1500, 15, 30, 2600)
        assertTrue(message.contains("1.5 L"))
    }
}
