package com.example

import com.example.data.cloud.CloudBackupPayload
import com.example.data.cloud.CloudSyncManager
import com.example.data.model.AttendanceRecord
import com.example.data.model.CoachingProfile
import com.example.data.model.FeePayment
import com.example.data.model.Student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CloudSyncManagerTest {

    @Test
    fun testExportAndImportBackupJson() {
        val context = RuntimeEnvironment.getApplication()
        val manager = CloudSyncManager(context)

        val profile = CoachingProfile(
            id = 1,
            coachingName = "Apex Academy",
            teacherName = "Prof. Sharma",
            mobileNumber = "9876543210",
            address = "Civil Lines, Delhi",
            currencySymbol = "₹"
        )

        val student1 = Student(
            id = 101L,
            name = "Aman Kumar",
            guardianName = "R. K. Kumar",
            mobileNumber = "9988776655",
            studentClass = "Class 10",
            batch = "Morning",
            rollNumber = "01",
            joiningDate = "2026-08-01",
            monthlyFee = 1500.0,
            isActive = true
        )

        val attendance1 = AttendanceRecord(
            id = 201L,
            studentId = 101L,
            date = "2026-09-18",
            status = "PRESENT",
            note = "On time"
        )

        val payment1 = FeePayment(
            id = 301L,
            studentId = 101L,
            amountPaid = 1500.0,
            paymentDate = "2026-09-10",
            forMonthYear = "2026-09",
            paymentMode = "UPI",
            remarks = "September fee paid via GPay"
        )

        val originalPayload = CloudBackupPayload(
            profile = profile,
            students = listOf(student1),
            attendance = listOf(attendance1),
            payments = listOf(payment1)
        )

        val json = manager.exportBackupJson(originalPayload)
        assertNotNull(json)
        assertTrue(json.contains("Apex Academy"))
        assertTrue(json.contains("Aman Kumar"))
        assertTrue(json.contains("PRESENT"))

        val importResult = manager.importBackupJson(json)
        assertTrue(importResult.isSuccess)

        val restored = importResult.getOrNull()
        assertNotNull(restored)
        assertEquals("Apex Academy", restored!!.profile.coachingName)
        assertEquals(1, restored.students.size)
        assertEquals("Aman Kumar", restored.students[0].name)
        assertEquals(1, restored.attendance.size)
        assertEquals("PRESENT", restored.attendance[0].status)
        assertEquals("On time", restored.attendance[0].note)
        assertEquals(1, restored.payments.size)
        assertEquals(1500.0, restored.payments[0].amountPaid, 0.01)
        assertEquals("2026-09", restored.payments[0].forMonthYear)
    }
}
