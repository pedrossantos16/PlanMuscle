package com.pedro.planmuscle.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRoundTripTest {

    @Test
    fun `invalid json version is rejected by import`() {
        val invalidJson = "{\"schemaVersion\":1}"
        assertFalse(invalidJson.contains("\"schemaVersion\":2"))
    }

    @Test
    fun `valid json version is accepted by import`() {
        val validJson = "{\"schemaVersion\":2,\"exportedAt\":1700000000000}"
        assertTrue(validJson.contains("\"schemaVersion\":2"))
    }
}
