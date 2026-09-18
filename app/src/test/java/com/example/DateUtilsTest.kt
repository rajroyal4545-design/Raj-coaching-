package com.example

import com.example.data.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun testExtractMonthYear() {
        assertEquals("2026-09", DateUtils.extractMonthYear("2026-09-18"))
        assertEquals("2026-08", DateUtils.extractMonthYear("2026-08-01"))
        assertEquals("2026-10", DateUtils.extractMonthYear("2026-10"))
        assertEquals("2026-07", DateUtils.extractMonthYear("15-07-2026"))
        assertEquals("2026-06", DateUtils.extractMonthYear("20/06/2026"))
    }

    @Test
    fun testStudentAdmittedInOrBefore() {
        // Student admitted in September 2026
        val joiningDate = "2026-09-15"

        // In August 2026, student should NOT show fee
        assertFalse(DateUtils.isStudentAdmittedInOrBefore(joiningDate, "2026-08"))

        // In September 2026 (admission month), student SHOULD show fee
        assertTrue(DateUtils.isStudentAdmittedInOrBefore(joiningDate, "2026-09"))

        // In October 2026 (later month), student SHOULD show fee
        assertTrue(DateUtils.isStudentAdmittedInOrBefore(joiningDate, "2026-10"))
    }

    @Test
    fun testBlankJoiningDateDefaultsToTrue() {
        assertTrue(DateUtils.isStudentAdmittedInOrBefore("", "2026-08"))
        assertTrue(DateUtils.isStudentAdmittedInOrBefore(null, "2026-08"))
    }
}
