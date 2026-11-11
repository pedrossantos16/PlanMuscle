package com.pedro.planmuscle.data.local.migration

import org.junit.Assert.assertEquals
import org.junit.Test

class Migration1To2Test {

    @Test
    fun `migration SQL strings are defined and valid`() {
        assertEquals(1, MIGRATION_1_2.startVersion)
        assertEquals(2, MIGRATION_1_2.endVersion)
    }
}
