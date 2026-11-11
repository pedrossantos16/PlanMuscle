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
    fun `interpolated volume at 09 00 slot is 400 ml`() {
        val expected = HydrationEngine.getInterpolatedExpectedVolume(9, 0, 2600)
        assertEquals(400, expected)
    }

    @Test
    fun `interpolated volume at 12 15 slot is 800 ml`() {
        val expected = HydrationEngine.getInterpolatedExpectedVolume(12, 15, 2600)
        assertEquals(800, expected)
    }

    @Test
    fun `interpolated volume at 15 30 slot is 1200 ml`() {
        val expected = HydrationEngine.getInterpolatedExpectedVolume(15, 30, 2600)
        assertEquals(1200, expected)
    }

    @Test
    fun `interpolated volume at 18 15 slot is 1700 ml`() {
        val expected = HydrationEngine.getInterpolatedExpectedVolume(18, 15, 2600)
        assertEquals(1700, expected)
    }

    @Test
    fun `interpolated volume at 22 05 slot is 2300 ml`() {
        val expected = HydrationEngine.getInterpolatedExpectedVolume(22, 5, 2600)
        assertEquals(2300, expected)
    }

    @Test
    fun `interpolated volume at end of day 23 00 is full 2600 ml`() {
        val expected = HydrationEngine.getInterpolatedExpectedVolume(23, 0, 2600)
        assertEquals(2600, expected)
    }

    @Test
    fun `custom target 3000 ml scales slots proportionally`() {
        val expected1215 = HydrationEngine.getInterpolatedExpectedVolume(12, 15, 3000)
        // 800 * (3000/2600) = 923 ml
        assertEquals(923, expected1215)
    }

    @Test
    fun `status message shows current and target volume`() {
        val message = HydrationEngine.getHydrationStatusMessage(1500, 15, 30, 2600)
        assertTrue(message.contains("1.5 L"))
    }
}
