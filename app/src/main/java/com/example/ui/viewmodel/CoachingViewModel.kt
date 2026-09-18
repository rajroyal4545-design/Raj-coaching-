package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.cloud.CloudAuthState
import com.example.data.cloud.CloudBackupPayload
import com.example.data.cloud.CloudSyncManager
import com.example.data.model.AttendanceRecord
import com.example.data.model.CoachingProfile
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.repository.CoachingRepository
import com.example.data.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StudentFeeSummary(
    val student: Student,
    val totalPaidForMonth: Double,
    val pendingForMonth: Double,
    val totalLifetimePaid: Double,
    val isPaidInFull: Boolean
)

data class StudentAttendanceSummary(
    val student: Student,
    val totalDaysMarked: Int,
    val presentDays: Int,
    val absentDays: Int,
    val attendancePercentage: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
class CoachingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CoachingRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = CoachingRepository(db)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    val todayDateString: String = dateFormat.format(Date())
    val currentMonthString: String = monthFormat.format(Date())

    // Selected Date for Attendance tab
    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Selected Month for Reports & Fees
    private val _selectedMonth = MutableStateFlow(currentMonthString)
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Search query for Students
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    // Selected Batch Filter
    private val _selectedBatchFilter = MutableStateFlow("All")
    val selectedBatchFilter: StateFlow<String> = _selectedBatchFilter.asStateFlow()

    // Active Profile
    val coachingProfile: StateFlow<CoachingProfile> = repository.coachingProfile
        .map { it ?: CoachingProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CoachingProfile()
        )

    // All Students
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered Students
    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        _studentSearchQuery,
        _selectedBatchFilter
    ) { students, query, batch ->
        students.filter { s ->
            val matchesQuery = query.isBlank() ||
                s.name.contains(query, ignoreCase = true) ||
                s.rollNumber.contains(query, ignoreCase = true) ||
                s.studentClass.contains(query, ignoreCase = true) ||
                s.mobileNumber.contains(query, ignoreCase = true) ||
                s.joiningDate.contains(query, ignoreCase = true)
            val matchesBatch = batch == "All" || s.batch.equals(batch, ignoreCase = true)
            matchesQuery && matchesBatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Attendance records for the selected date
    val attendanceForSelectedDate: StateFlow<Map<Long, AttendanceRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .map { records -> records.associateBy { it.studentId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Attendance records for TODAY specifically (for Dashboard stats)
    val attendanceToday: StateFlow<Map<Long, AttendanceRecord>> = repository.getAttendanceForDate(todayDateString)
        .map { records -> records.associateBy { it.studentId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // All Payments
    val allPayments: StateFlow<List<FeePayment>> = repository.allPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Monthly payments for current selected month
    val paymentsForSelectedMonth: StateFlow<List<FeePayment>> = _selectedMonth
        .flatMapLatest { month -> repository.getPaymentsForMonth(month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Monthly attendance for reports
    val attendanceForSelectedMonth: StateFlow<List<AttendanceRecord>> = _selectedMonth
        .flatMapLatest { month -> repository.getAttendanceForMonth(month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dashboard metrics
    val dashboardMetrics = combine(
        allStudents,
        attendanceToday,
        allPayments,
        _selectedMonth
    ) { students, todayAttendance, payments, currentMonth ->
        val activeStudents = students.filter { it.isActive }
        val totalStudentsCount = activeStudents.size

        var presentCount = 0
        var absentCount = 0
        var leaveCount = 0
        var isClosed = false

        if (todayAttendance.isNotEmpty() && todayAttendance.values.all { it.status == "CLOSED" }) {
            isClosed = true
        }

        activeStudents.forEach { s ->
            val record = todayAttendance[s.id]
            if (record != null) {
                when (record.status) {
                    "PRESENT" -> presentCount++
                    "ABSENT" -> absentCount++
                    "LEAVE" -> leaveCount++
                    "CLOSED" -> isClosed = true
                }
            }
        }
        val unmarkedCount = if (isClosed) 0 else (totalStudentsCount - presentCount - absentCount - leaveCount).coerceAtLeast(0)

        val currentMonthPayments = payments.filter { it.forMonthYear == currentMonth }
        val collectedFee = currentMonthPayments.sumOf { it.amountPaid }

        // Only expect fee from active students admitted in or before currentMonth
        val eligibleStudentsForMonth = activeStudents.filter { s ->
            DateUtils.isStudentAdmittedInOrBefore(s.joiningDate, currentMonth) ||
                currentMonthPayments.any { it.studentId == s.id }
        }
        val totalExpectedFee = eligibleStudentsForMonth.sumOf { it.monthlyFee }
        val pendingFee = (totalExpectedFee - collectedFee).coerceAtLeast(0.0)

        DashboardData(
            totalStudents = totalStudentsCount,
            todayPresent = presentCount,
            todayAbsent = absentCount,
            todayLeave = leaveCount,
            isTodayClosed = isClosed,
            todayUnmarked = unmarkedCount,
            currentMonthCollection = collectedFee,
            totalExpectedFee = totalExpectedFee,
            pendingFeeAmount = pendingFee
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardData()
    )

    // Student Fee Summaries for the selected month:
    // Only show fee for the month in which the student took admission and onwards.
    val studentFeeSummaries = combine(
        allStudents,
        paymentsForSelectedMonth,
        allPayments,
        _selectedMonth
    ) { students, monthPayments, allP, currentMonth ->
        val eligibleStudents = students.filter { student ->
            DateUtils.isStudentAdmittedInOrBefore(student.joiningDate, currentMonth) ||
                monthPayments.any { it.studentId == student.id }
        }
        eligibleStudents.map { student ->
            val studentMonthPaid = monthPayments
                .filter { it.studentId == student.id }
                .sumOf { it.amountPaid }
            val studentLifetimePaid = allP
                .filter { it.studentId == student.id }
                .sumOf { it.amountPaid }
            val pending = (student.monthlyFee - studentMonthPaid).coerceAtLeast(0.0)
            StudentFeeSummary(
                student = student,
                totalPaidForMonth = studentMonthPaid,
                pendingForMonth = pending,
                totalLifetimePaid = studentLifetimePaid,
                isPaidInFull = studentMonthPaid >= student.monthlyFee
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Actions
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun setSelectedBatchFilter(batch: String) {
        _selectedBatchFilter.value = batch
    }

    fun stepDate(days: Int) {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_selectedDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, days)
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (_: Exception) {
            _selectedDate.value = todayDateString
        }
    }

    fun updateCoachingProfile(profile: CoachingProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun addStudent(student: Student) {
        viewModelScope.launch {
            repository.addStudent(student)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun markAttendance(studentId: Long, status: String) {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.markAttendance(studentId, date, status)
        }
    }

    fun markAllPresent() {
        val date = _selectedDate.value
        val students = allStudents.value.filter { it.isActive }
        viewModelScope.launch {
            repository.markAllAttendance(date, students, "PRESENT")
        }
    }

    fun markAllAbsent() {
        val date = _selectedDate.value
        val students = allStudents.value.filter { it.isActive }
        viewModelScope.launch {
            repository.markAllAttendance(date, students, "ABSENT")
        }
    }

    fun markAllLeave() {
        val date = _selectedDate.value
        val students = allStudents.value.filter { it.isActive }
        viewModelScope.launch {
            repository.markAllAttendance(date, students, "LEAVE")
        }
    }

    fun markCoachingClosed() {
        val date = _selectedDate.value
        val students = allStudents.value.filter { it.isActive }
        viewModelScope.launch {
            repository.markAllAttendance(date, students, "CLOSED")
        }
    }

    fun clearDayAttendance() {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.clearAttendanceForDate(date)
        }
    }

    fun removeAttendance(studentId: Long) {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.removeAttendance(studentId, date)
        }
    }

    fun addFeePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.addFeePayment(payment)
        }
    }

    fun updateFeePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.updateFeePayment(payment)
        }
    }

    fun deleteFeePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.deleteFeePayment(payment)
        }
    }

    fun getStudentPayments(studentId: Long) = repository.getPaymentsForStudent(studentId)
    fun getStudentAttendance(studentId: Long) = repository.getAttendanceForStudent(studentId)

    // Cloud Authentication & Multi-Device Sync
    private val cloudSyncManager = CloudSyncManager(application)
    val cloudAuthState: StateFlow<CloudAuthState> = cloudSyncManager.authState

    fun saveFirebaseConfig(projectId: String, apiKey: String) {
        cloudSyncManager.saveFirebaseConfig(projectId, apiKey)
    }

    fun registerCloud(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = cloudSyncManager.registerWithEmailPassword(email, pass)
            result.onSuccess { msg ->
                uploadToCloud { _, _ -> }
                onResult(true, msg)
            }.onFailure { err ->
                onResult(false, err.message ?: "Registration failed")
            }
        }
    }

    fun loginCloud(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = cloudSyncManager.loginWithEmailPassword(email, pass)
            result.onSuccess { msg ->
                downloadFromCloud { success, _ ->
                    if (success) {
                        onResult(true, "Login successful! Cloud database synced on this device.")
                    } else {
                        onResult(true, "Login successful!")
                    }
                }
            }.onFailure { err ->
                onResult(false, err.message ?: "Login failed")
            }
        }
    }

    fun logoutCloud() {
        cloudSyncManager.logout()
    }

    fun uploadToCloud(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val payload = repository.getBackupPayload()
                val result = cloudSyncManager.uploadToCloud(payload)
                result.onSuccess { msg ->
                    onResult(true, msg)
                }.onFailure { err ->
                    onResult(false, err.message ?: "Upload failed")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Sync error")
            }
        }
    }

    fun downloadFromCloud(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = cloudSyncManager.downloadFromCloud()
                result.onSuccess { payload ->
                    repository.restoreAllData(payload)
                    onResult(true, "Data successfully downloaded & synced from Cloud!")
                }.onFailure { err ->
                    onResult(false, err.message ?: "Download failed")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Download error")
            }
        }
    }

    suspend fun exportBackupJson(): String {
        val payload = repository.getBackupPayload()
        return cloudSyncManager.exportBackupJson(payload)
    }

    fun importBackupJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = cloudSyncManager.importBackupJson(jsonString)
                result.onSuccess { payload ->
                    repository.restoreAllData(payload)
                    onResult(true, "Data restored successfully! ${payload.students.size} students loaded.")
                }.onFailure { err ->
                    onResult(false, err.message ?: "Invalid backup file")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Import error")
            }
        }
    }
}

data class DashboardData(
    val totalStudents: Int = 0,
    val todayPresent: Int = 0,
    val todayAbsent: Int = 0,
    val todayLeave: Int = 0,
    val isTodayClosed: Boolean = false,
    val todayUnmarked: Int = 0,
    val currentMonthCollection: Double = 0.0,
    val totalExpectedFee: Double = 0.0,
    val pendingFeeAmount: Double = 0.0
)
