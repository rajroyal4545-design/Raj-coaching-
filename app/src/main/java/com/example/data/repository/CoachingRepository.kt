package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.cloud.CloudBackupPayload
import com.example.data.model.AttendanceRecord
import com.example.data.model.CoachingProfile
import com.example.data.model.FeePayment
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

class CoachingRepository(private val database: AppDatabase) {

    private val profileDao = database.coachingProfileDao()
    private val studentDao = database.studentDao()
    private val attendanceDao = database.attendanceDao()
    private val feePaymentDao = database.feePaymentDao()

    val coachingProfile: Flow<CoachingProfile?> = profileDao.getProfile()
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allPayments: Flow<List<FeePayment>> = feePaymentDao.getAllPayments()

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceForDate(date)
    }

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceForStudent(studentId)
    }

    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceForMonth(monthPrefix)
    }

    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>> {
        return feePaymentDao.getPaymentsForStudent(studentId)
    }

    fun getPaymentsForMonth(monthYear: String): Flow<List<FeePayment>> {
        return feePaymentDao.getPaymentsForMonth(monthYear)
    }

    suspend fun updateProfile(profile: CoachingProfile) {
        profileDao.insertOrUpdate(profile)
    }

    suspend fun addStudent(student: Student): Long {
        return studentDao.insert(student)
    }

    suspend fun updateStudent(student: Student) {
        studentDao.update(student)
    }

    suspend fun deleteStudent(student: Student) {
        // Cascade delete attendance and fee payments
        attendanceDao.deleteByStudent(student.id)
        feePaymentDao.deleteByStudent(student.id)
        studentDao.delete(student)
    }

    suspend fun markAttendance(studentId: Long, date: String, status: String, note: String = "") {
        attendanceDao.insertOrUpdate(
            AttendanceRecord(
                studentId = studentId,
                date = date,
                status = status,
                note = note
            )
        )
    }

    suspend fun markAllAttendance(date: String, students: List<Student>, status: String) {
        val records = students.map { student ->
            AttendanceRecord(
                studentId = student.id,
                date = date,
                status = status
            )
        }
        attendanceDao.insertOrUpdateAll(records)
    }

    suspend fun removeAttendance(studentId: Long, date: String) {
        attendanceDao.deleteByStudentAndDate(studentId, date)
    }

    suspend fun clearAttendanceForDate(date: String) {
        attendanceDao.deleteForDate(date)
    }

    suspend fun addFeePayment(payment: FeePayment): Long {
        return feePaymentDao.insert(payment)
    }

    suspend fun updateFeePayment(payment: FeePayment) {
        feePaymentDao.update(payment)
    }

    suspend fun deleteFeePayment(payment: FeePayment) {
        feePaymentDao.delete(payment)
    }

    suspend fun getBackupPayload(): CloudBackupPayload {
        val profile = profileDao.getProfileSync() ?: CoachingProfile()
        val students = studentDao.getAllStudentsSync()
        val attendance = attendanceDao.getAllAttendanceSync()
        val payments = feePaymentDao.getAllPaymentsSync()
        return CloudBackupPayload(
            profile = profile,
            students = students,
            attendance = attendance,
            payments = payments
        )
    }

    suspend fun restoreAllData(payload: CloudBackupPayload) {
        attendanceDao.deleteAllAttendance()
        feePaymentDao.deleteAllPayments()
        studentDao.deleteAllStudents()

        profileDao.insertOrUpdate(payload.profile)
        if (payload.students.isNotEmpty()) {
            studentDao.insertAll(payload.students)
        }
        if (payload.attendance.isNotEmpty()) {
            attendanceDao.insertOrUpdateAll(payload.attendance)
        }
        if (payload.payments.isNotEmpty()) {
            feePaymentDao.insertAll(payload.payments)
        }
    }
}
