package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AttendanceRecord
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.util.DateUtils
import com.example.ui.components.AvatarInitials
import com.example.ui.components.SimpleMonthPickerDialog
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.ClosedGray
import com.example.ui.theme.LeaveBlue
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.CoachingViewModel
import java.net.URLEncoder
import java.util.Locale

enum class ReportScope(val labelEn: String, val labelHi: String) {
    MONTHLY("This Month", "इस माह"),
    ALL_TIME("All Time", "कुल / अब तक")
}

enum class ReportFilter(val label: String) {
    ALL("All / सभी"),
    DUE_ONLY("⚠️ Due / बकाया"),
    PAID_ONLY("✅ Paid / चुकता"),
    LOW_ATTENDANCE("📉 Low Att. (<75%)")
}

enum class ReportSort(val label: String) {
    HIGHEST_DUE("Highest Due / अधिक बकाया"),
    LOWEST_ATTENDANCE("Lowest Attendance / कम हाजिरी"),
    NAME("Name / नाम (A-Z)"),
    ROLL_NUMBER("Roll No. / रोल नंबर")
}

/**
 * Clean data holder for student reporting metrics, including cumulative back-months dues.
 */
data class StudentReportData(
    val student: Student,
    val presentCount: Int,
    val absentCount: Int,
    val leaveCount: Int,
    val totalSessions: Int,
    val attendancePercent: Int,
    val expectedFee: Double,
    val paidFee: Double,
    val dueFee: Double, // Cumulative due (all back months + current month)
    val previousMonthsDue: Double = 0.0, // Due strictly from back/past months
    val currentMonthDue: Double = 0.0, // Due strictly from selected month
    val backMonthsCount: Int = 0,
    val isPaidInFull: Boolean
)

@Composable
fun ReportsScreen(viewModel: CoachingViewModel) {
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allAttendance by viewModel.allAttendanceRecords.collectAsState()
    val monthPayments by viewModel.paymentsForSelectedMonth.collectAsState()
    val monthAttendance by viewModel.attendanceForSelectedMonth.collectAsState()
    val profile by viewModel.coachingProfile.collectAsState()
    val currency = profile.currencySymbol.ifBlank { "₹" }

    var selectedScope by remember { mutableStateOf(ReportScope.MONTHLY) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var reportFilter by remember { mutableStateOf(ReportFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatch by remember { mutableStateOf("All") }
    var currentSort by remember { mutableStateOf(ReportSort.HIGHEST_DUE) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Dialogs
    var studentForPayment by remember { mutableStateOf<Student?>(null) }
    var studentForDetails by remember { mutableStateOf<Student?>(null) }

    // Available Batches
    val batches = remember(allStudents) {
        listOf("All") + allStudents.map { it.batch }.filter { it.isNotBlank() }.distinct()
    }

    // Active attendance & payments depending on scope
    val effectiveAttendance = if (selectedScope == ReportScope.MONTHLY) monthAttendance else allAttendance

    // Eligible students for the scope
    val eligibleStudents = remember(allStudents, selectedMonth, selectedScope, monthPayments) {
        if (selectedScope == ReportScope.MONTHLY) {
            allStudents.filter { s ->
                DateUtils.isStudentAdmittedInOrBefore(s.joiningDate, selectedMonth) ||
                    monthPayments.any { it.studentId == s.id }
            }
        } else {
            allStudents
        }
    }

    // Pre-calculate per-student metrics with cumulative back-month dues
    val studentDataList = remember(eligibleStudents, effectiveAttendance, allPayments, monthPayments, selectedScope, selectedMonth) {
        eligibleStudents.map { s ->
            val att = effectiveAttendance.filter { it.studentId == s.id }
            val pCount = att.count { it.status == "PRESENT" }
            val aCount = att.count { it.status == "ABSENT" }
            val lCount = att.count { it.status == "LEAVE" }
            val markedTotal = pCount + aCount
            val attPct = if (markedTotal > 0) ((pCount.toDouble() / markedTotal) * 100).toInt() else 0

            val admissionMonth = DateUtils.extractMonthYear(s.joiningDate)
            val earliestP = allPayments.filter { it.studentId == s.id }.minOfOrNull { it.forMonthYear }
            val startMonth = when {
                admissionMonth != null && earliestP != null -> minOf(admissionMonth, earliestP)
                admissionMonth != null -> admissionMonth
                earliestP != null -> earliestP
                else -> selectedMonth
            }

            val monthsUpToCurrent = DateUtils.getMonthsList(startMonth, selectedMonth)
            val pastMonths = monthsUpToCurrent.filter { it < selectedMonth }

            val pastExpected = pastMonths.size * s.monthlyFee
            val pastPaid = allPayments
                .filter { it.studentId == s.id && it.forMonthYear < selectedMonth }
                .sumOf { it.amountPaid }
            val pastDue = (pastExpected - pastPaid).coerceAtLeast(0.0)
            val pastAdvance = (pastPaid - pastExpected).coerceAtLeast(0.0)

            val studentMonthPaid = monthPayments
                .filter { it.studentId == s.id }
                .sumOf { it.amountPaid }
            val effectiveMonthPaid = studentMonthPaid + pastAdvance
            val pendingCurrentMonth = (s.monthlyFee - effectiveMonthPaid).coerceAtLeast(0.0)

            val totalDue: Double
            val expectedFee: Double
            val paidFee: Double
            val backMonthsCount: Int

            if (selectedScope == ReportScope.MONTHLY) {
                totalDue = pastDue + pendingCurrentMonth
                expectedFee = (pastMonths.size + 1) * s.monthlyFee
                paidFee = pastPaid + studentMonthPaid
                backMonthsCount = if (pastDue > 0.0 && s.monthlyFee > 0.0) {
                    Math.ceil(pastDue / s.monthlyFee).toInt().coerceAtMost(pastMonths.size)
                } else 0
            } else {
                // ALL_TIME Scope
                val allActiveMonths = monthsUpToCurrent
                expectedFee = allActiveMonths.size * s.monthlyFee
                paidFee = allPayments.filter { it.studentId == s.id }.sumOf { it.amountPaid }
                totalDue = (expectedFee - paidFee).coerceAtLeast(0.0)
                backMonthsCount = 0
            }

            StudentReportData(
                student = s,
                presentCount = pCount,
                absentCount = aCount,
                leaveCount = lCount,
                totalSessions = markedTotal,
                attendancePercent = attPct,
                expectedFee = expectedFee,
                paidFee = paidFee,
                dueFee = totalDue,
                previousMonthsDue = pastDue,
                currentMonthDue = pendingCurrentMonth,
                backMonthsCount = backMonthsCount,
                isPaidInFull = totalDue <= 0.0
            )
        }
    }

    // Aggregates for executive summary
    val totalStudentsCount = studentDataList.size
    val totalPresentSum = studentDataList.sumOf { it.presentCount }
    val totalAbsentSum = studentDataList.sumOf { it.absentCount }
    val totalExpectedSum = studentDataList.sumOf { it.expectedFee }
    val totalPaidSum = studentDataList.sumOf { it.paidFee }
    val totalDueSum = studentDataList.sumOf { it.dueFee }
    val avgAttendancePct = if (totalPresentSum + totalAbsentSum > 0) {
        ((totalPresentSum.toDouble() / (totalPresentSum + totalAbsentSum)) * 100).toInt()
    } else 0

    // Filter and sort students
    val filteredStudents = remember(studentDataList, searchQuery, reportFilter, selectedBatch, currentSort) {
        studentDataList
            .filter { data ->
                val s = data.student
                val matchesQuery = searchQuery.isBlank() ||
                    s.name.contains(searchQuery, ignoreCase = true) ||
                    s.rollNumber.contains(searchQuery, ignoreCase = true) ||
                    s.mobileNumber.contains(searchQuery, ignoreCase = true) ||
                    s.studentClass.contains(searchQuery, ignoreCase = true)

                val matchesBatch = selectedBatch == "All" || s.batch.equals(selectedBatch, ignoreCase = true)

                val matchesFilter = when (reportFilter) {
                    ReportFilter.ALL -> true
                    ReportFilter.DUE_ONLY -> data.dueFee > 0.0
                    ReportFilter.PAID_ONLY -> data.dueFee <= 0.0
                    ReportFilter.LOW_ATTENDANCE -> data.totalSessions > 0 && data.attendancePercent < 75
                }
                matchesQuery && matchesBatch && matchesFilter
            }
            .let { list ->
                when (currentSort) {
                    ReportSort.HIGHEST_DUE -> list.sortedByDescending { it.dueFee }
                    ReportSort.LOWEST_ATTENDANCE -> list.sortedBy { it.attendancePercent }
                    ReportSort.NAME -> list.sortedBy { it.student.name.lowercase(Locale.ROOT) }
                    ReportSort.ROLL_NUMBER -> list.sortedBy { it.student.rollNumber.toIntOrNull() ?: Int.MAX_VALUE }
                }
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Top Scope Tab Selector (Monthly vs All-Time)
            TabRow(
                selectedTabIndex = selectedScope.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                ReportScope.values().forEach { scope ->
                    Tab(
                        selected = selectedScope == scope,
                        onClick = { selectedScope = scope },
                        text = {
                            Text(
                                text = "${scope.labelEn} (${scope.labelHi})",
                                fontWeight = if (selectedScope == scope) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }
        }

        // 2. Month Selector & Report Share Header
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedScope == ReportScope.MONTHLY) "Month Report / मासिक रिपोर्ट" else "Overall Report / कुल रिपोर्ट",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedScope == ReportScope.MONTHLY) "Month: $selectedMonth (पिछला बकाया शामिल)" else "All recorded data (${profile.coachingName})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Share complete report button
                        Button(
                            onClick = {
                                val reportText = buildString {
                                    appendLine("📊 ${profile.coachingName} - Student Attendance & Fee Report")
                                    appendLine("📅 Period: ${if (selectedScope == ReportScope.MONTHLY) selectedMonth else "Overall (All-Time)"}")
                                    if (profile.teacherName.isNotBlank()) appendLine("👨‍🏫 Teacher: ${profile.teacherName}")
                                    appendLine("=================================")
                                    appendLine("👥 Total Students: $totalStudentsCount")
                                    appendLine("📈 Avg Attendance: $avgAttendancePct%")
                                    appendLine("💰 Total Fee: $currency ${String.format(Locale.US, "%,.0f", totalExpectedSum)}")
                                    appendLine("💵 Total Paid: $currency ${String.format(Locale.US, "%,.0f", totalPaidSum)}")
                                    appendLine("⚠️ Total Due (सब मिलाकर): $currency ${String.format(Locale.US, "%,.0f", totalDueSum)}")
                                    appendLine("=================================")
                                    appendLine("STUDENT DETAILS (Present / Paid / Total Due):")
                                    filteredStudents.forEach { data ->
                                        val s = data.student
                                        appendLine("• ${s.name} (Roll #${s.rollNumber}, ${s.batch})")
                                        appendLine("  Att: ${data.presentCount}P / ${data.absentCount}A (${data.attendancePercent}%)")
                                        if (data.previousMonthsDue > 0.0) {
                                            appendLine("  Fee: $currency${s.monthlyFee.toInt()} | Paid: $currency${data.paidFee.toInt()} | Due: $currency${data.dueFee.toInt()} (पिछला: $currency${data.previousMonthsDue.toInt()} + इस माह: $currency${data.currentMonthDue.toInt()})")
                                        } else {
                                            appendLine("  Fee: $currency${s.monthlyFee.toInt()} | Paid: $currency${data.paidFee.toInt()} | Due: $currency${data.dueFee.toInt()}")
                                        }
                                    }
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Student Report"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.sp)
                        }
                    }

                    // Month Navigation Controls (Only for Monthly scope)
                    if (selectedScope == ReportScope.MONTHLY) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val prev = DateUtils.getAdjacentMonth(selectedMonth, -1)
                                    viewModel.setSelectedMonth(prev)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Prev", fontSize = 12.sp)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable { showMonthPicker = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selectedMonth,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val next = DateUtils.getAdjacentMonth(selectedMonth, 1)
                                    viewModel.setSelectedMonth(next)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Text("Next", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 3. Executive KPI Overview Cards (Attendance & Payment Summary with Cumulative Dues)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Executive Summary / मुख्य सारांश",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Attendance KPI row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportSummaryItem("Students", "$totalStudentsCount", MaterialTheme.colorScheme.primary)
                        ReportSummaryItem("Total Present", "$totalPresentSum", PresentGreen)
                        ReportSummaryItem("Total Absent", "$totalAbsentSum", AbsentRed)
                        ReportSummaryItem("Att. Rate", "$avgAttendancePct%", if (avgAttendancePct >= 75) PresentGreen else WarningAmber)
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Fees KPI row (Paid & Cumulative Due highlighted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportSummaryItem("Total Expected", "$currency ${String.format(Locale.US, "%,.0f", totalExpectedSum)}")
                        ReportSummaryItem("Total Paid (जमा)", "$currency ${String.format(Locale.US, "%,.0f", totalPaidSum)}", PresentGreen)
                        ReportSummaryItem("Total Due (कुल बकाया)", "$currency ${String.format(Locale.US, "%,.0f", totalDueSum)}", if (totalDueSum > 0) AbsentRed else Color.Gray)
                    }
                }
            }
        }

        // 4. Search and Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Search by name, roll no, mobile...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Filter & Sort Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Students (${filteredStudents.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Sort menu button
                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentSort.label.substringBefore(" /"), fontSize = 11.sp)
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            ReportSort.values().forEach { sort ->
                                DropdownMenuItem(
                                    text = { Text(sort.label, fontSize = 13.sp) },
                                    onClick = {
                                        currentSort = sort
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ReportFilter.values()) { filter ->
                        FilterChip(
                            selected = reportFilter == filter,
                            onClick = { reportFilter = filter },
                            label = { Text(filter.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Batch Filters (if more than 1 batch)
                if (batches.size > 2) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(batches) { batch ->
                            FilterChip(
                                selected = selectedBatch == batch,
                                onClick = { selectedBatch = batch },
                                label = { Text(if (batch == "All") "All Batches" else batch, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 5. Student List (Showing Total Present, Total Paid, and Cumulative Back Months Due)
        if (filteredStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍 No students found", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No matching records found for the selected filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredStudents, key = { it.student.id }) { data ->
                StudentReportCard(
                    data = data,
                    currency = currency,
                    periodLabel = if (selectedScope == ReportScope.MONTHLY) selectedMonth else "Overall",
                    coachingName = profile.coachingName,
                    onCollectFee = { studentForPayment = data.student },
                    onViewDetails = { studentForDetails = data.student }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Month Picker Dialog
    if (showMonthPicker) {
        SimpleMonthPickerDialog(
            currentMonthYear = selectedMonth,
            onMonthSelected = {
                viewModel.setSelectedMonth(it)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    // Quick Fee Payment Dialog (Defaults to student's total cumulative due!)
    studentForPayment?.let { student ->
        val data = studentDataList.find { it.student.id == student.id }
        val dueAmount = data?.dueFee ?: 0.0

        com.example.ui.screens.FeePaymentFormDialog(
            initialPayment = null,
            students = allStudents,
            preselectedStudent = student,
            defaultAmount = if (dueAmount > 0.0) dueAmount else student.monthlyFee,
            defaultMonth = selectedMonth,
            onDismiss = { studentForPayment = null },
            onSave = { payment ->
                viewModel.addFeePayment(payment)
                studentForPayment = null
                Toast.makeText(context, "Fee collected for ${student.name}!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Student Details & History Dialog
    studentForDetails?.let { student ->
        val data = studentDataList.find { it.student.id == student.id }
        val studentPayments = allPayments.filter { it.studentId == student.id }

        StudentReportDetailDialog(
            student = student,
            data = data,
            payments = studentPayments,
            currency = currency,
            scopeLabel = if (selectedScope == ReportScope.MONTHLY) selectedMonth else "All Time",
            onDismiss = { studentForDetails = null },
            onCollectFee = {
                studentForPayment = student
                studentForDetails = null
            }
        )
    }
}

/**
 * Individual Student Report Card displaying Attendance (Present/Absent) and Payment (Paid/Total Due including back months).
 */
@Composable
fun StudentReportCard(
    data: StudentReportData,
    currency: String,
    periodLabel: String,
    coachingName: String,
    onCollectFee: () -> Unit,
    onViewDetails: () -> Unit
) {
    val student = data.student
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Student Header (Avatar, Name, Roll, Status Badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 44, fontSize = 16)
                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Roll: #${student.rollNumber} • ${student.studentClass} • ${student.batch}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Payment Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        data.isPaidInFull -> Color(0xFFDCFCE7)
                        data.paidFee > 0 -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                data.isPaidInFull -> "PAID / चुकता"
                                data.paidFee > 0 -> "PARTIAL / आंशिक"
                                else -> "DUE / बकाया"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                data.isPaidInFull -> PresentGreen
                                data.paidFee > 0 -> WarningAmber
                                else -> AbsentRed
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Attendance & Payment Two-Column Overview Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Column: Total Present & Attendance Stats (Green Theme)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ATTENDANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (data.attendancePercent >= 75) PresentGreen.copy(alpha = 0.15f) else AbsentRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${data.attendancePercent}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (data.attendancePercent >= 75) PresentGreen else AbsentRed,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${data.presentCount}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PresentGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Present / उपस्थित",
                                fontSize = 11.sp,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Absent & Leave subtext
                        Text(
                            text = "❌ Absent: ${data.absentCount}  •  🏖️ Leave: ${data.leaveCount}",
                            fontSize = 10.sp,
                            color = Color(0xFF4B5563)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { (data.attendancePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (data.attendancePercent >= 75) PresentGreen else WarningAmber,
                            trackColor = Color(0xFFDCFCE7)
                        )
                    }
                }

                // Right Column: Payment Details (Total Fee, Paid, and Cumulative Back Months Due)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (data.dueFee > 0) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (data.dueFee > 0) Color(0xFFFDE68A) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PAYMENT DETAILS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (data.dueFee > 0) Color(0xFF92400E) else Color(0xFF475569)
                            )
                            Text(
                                text = "Fee: $currency${student.monthlyFee.toInt()}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Paid amount
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Paid (जमा): ",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = "$currency ${String.format(Locale.US, "%,.0f", data.paidFee)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PresentGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Total Due amount (All back months + current month)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Total Due (बकाया): ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (data.dueFee > 0) AbsentRed else Color(0xFF475569)
                            )
                            Text(
                                text = if (data.dueFee > 0) "$currency ${String.format(Locale.US, "%,.0f", data.dueFee)}" else "₹0 (Nil)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (data.dueFee > 0) AbsentRed else PresentGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Back months breakdown indication
                        if (data.previousMonthsDue > 0.0) {
                            Text(
                                text = "⚠️ पिछला: $currency${data.previousMonthsDue.toInt()} + इस माह: $currency${data.currentMonthDue.toInt()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AbsentRed
                            )
                        } else {
                            Text(
                                text = if (data.dueFee > 0) "⚠️ ${currency}${data.dueFee.toInt()} Pending" else "✓ No pending dues",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (data.dueFee > 0) AbsentRed else PresentGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Action Buttons (WhatsApp Parent, Collect Fee, Call Parent)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Reminder Button
                OutlinedButton(
                    onClick = {
                        val message = buildString {
                            appendLine("नमस्ते! ${coachingName} से छात्र फीस व हाजिरी विवरण:")
                            appendLine("👤 छात्र का नाम: ${student.name}")
                            appendLine("📋 रोल नंबर: ${student.rollNumber} (${student.batch})")
                            appendLine("📅 अवधि: $periodLabel")
                            appendLine("---------------------------")
                            appendLine("✅ कुल उपस्थिति (Present): ${data.presentCount} दिन (${data.attendancePercent}%)")
                            appendLine("❌ कुल अनुपस्थिति (Absent): ${data.absentCount} दिन")
                            appendLine("---------------------------")
                            appendLine("💰 निर्धारित मासिक फीस: $currency${student.monthlyFee.toInt()}")
                            appendLine("💵 कुल जमा (Paid): $currency${data.paidFee.toInt()}")
                            appendLine("⚠️ कुल बकाया (Total Due): $currency${data.dueFee.toInt()}")
                            if (data.previousMonthsDue > 0.0) {
                                appendLine("👉 (पिछले ${data.backMonthsCount} माह का बकाया: $currency${data.previousMonthsDue.toInt()} + इस माह: $currency${data.currentMonthDue.toInt()})")
                                appendLine("कृपया पिछले महीनों का कुल बकाया जल्द से जल्द जमा कराने का कष्ट करें।")
                            } else if (data.dueFee > 0) {
                                appendLine("कृपया बकाया फीस समय पर जमा करने की कृपा करें।")
                            } else {
                                appendLine("फीस पूर्ण रूप से जमा है। धन्यवाद!")
                            }
                            appendLine("धन्यवाद - ${coachingName}")
                        }

                        val formattedPhone = student.mobileNumber.replace(Regex("[^0-9]"), "").let { num ->
                            if (num.length == 10) "91$num" else num
                        }
                        val encodedMsg = try {
                            URLEncoder.encode(message, "UTF-8")
                        } catch (_: Exception) {
                            ""
                        }
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMsg")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, message)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Send Student Report"))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text(text = "💬 WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // If fee is due, show prominent "Pay Fee" button
                if (data.dueFee > 0) {
                    Button(
                        onClick = onCollectFee,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Pay / जमा", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "History / विवरण", fontSize = 12.sp)
                    }
                }

                // Call Phone Icon Button
                if (student.mobileNumber.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobileNumber}"))
                            try {
                                context.startActivity(callIntent)
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed dialog showing student's complete attendance and fee payment transaction history.
 */
@Composable
fun StudentReportDetailDialog(
    student: Student,
    data: StudentReportData?,
    payments: List<FeePayment>,
    currency: String,
    scopeLabel: String,
    onDismiss: () -> Unit,
    onCollectFee: () -> Unit
) {
    val presentCount = data?.presentCount ?: 0
    val absentCount = data?.absentCount ?: 0
    val leaveCount = data?.leaveCount ?: 0
    val attendancePercent = data?.attendancePercent ?: 0
    val expectedFee = data?.expectedFee ?: student.monthlyFee
    val paidFee = data?.paidFee ?: 0.0
    val dueFee = data?.dueFee ?: 0.0
    val prevDue = data?.previousMonthsDue ?: 0.0
    val curDue = data?.currentMonthDue ?: 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 48, fontSize = 18)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Roll: #${student.rollNumber} • ${student.studentClass} • ${student.batch}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (student.mobileNumber.isNotBlank()) {
                            Text(text = "📞 ${student.mobileNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Divider()

                Text(
                    text = "Summary for $scopeLabel",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Attendance breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ReportSummaryItem("Present", "$presentCount d", PresentGreen)
                    ReportSummaryItem("Absent", "$absentCount d", AbsentRed)
                    ReportSummaryItem("Leave", "$leaveCount d", LeaveBlue)
                    ReportSummaryItem("Att. %", "$attendancePercent%", if (attendancePercent >= 75) PresentGreen else WarningAmber)
                }

                // Payment breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ReportSummaryItem("Expected", "$currency ${expectedFee.toInt()}")
                    ReportSummaryItem("Paid (जमा)", "$currency ${paidFee.toInt()}", PresentGreen)
                    ReportSummaryItem("Due (कुल बकाया)", "$currency ${dueFee.toInt()}", if (dueFee > 0) AbsentRed else PresentGreen)
                }

                // Back months breakdown callout if past due exists
                if (prevDue > 0.0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "बकाया का विवरण (Due Breakdown):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AbsentRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• पिछले महीनों का बकाया: $currency ${String.format(Locale.US, "%,.0f", prevDue)}",
                                fontSize = 11.sp,
                                color = AbsentRed
                            )
                            Text(
                                text = "• इस माह का बकाया: $currency ${String.format(Locale.US, "%,.0f", curDue)}",
                                fontSize = 11.sp,
                                color = AbsentRed
                            )
                            Text(
                                text = "• कुल मिलाकर बकाया राशि: $currency ${String.format(Locale.US, "%,.0f", dueFee)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AbsentRed
                            )
                        }
                    }
                }

                Divider()

                // Recent Payment Transactions List
                Text(
                    text = "Payment Transactions / जमा रसीदें (${payments.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                if (payments.isEmpty()) {
                    Text(
                        text = "No payments recorded for this period.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        payments.take(4).forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Date: ${p.paymentDate} • ${p.paymentMode}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "Month: ${p.forMonthYear} • Receipt #${p.id}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(
                                        text = "$currency ${String.format(Locale.US, "%,.0f", p.amountPaid)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PresentGreen
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Close / बंद करें")
                    }

                    if (dueFee > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onCollectFee,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                        ) {
                            Text("Collect Fee / फीस जमा")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSummaryItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
