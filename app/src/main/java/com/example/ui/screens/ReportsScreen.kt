package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarInitials
import com.example.ui.components.SimpleMonthPickerDialog
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.CoachingViewModel
import java.util.Locale

@Composable
fun ReportsScreen(viewModel: CoachingViewModel) {
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val monthPayments by viewModel.paymentsForSelectedMonth.collectAsState()
    val monthAttendance by viewModel.attendanceForSelectedMonth.collectAsState()
    val profile by viewModel.coachingProfile.collectAsState()
    val currency = profile.currencySymbol

    var showMonthPicker by remember { mutableStateOf(false) }

    // Aggregate monthly numbers
    val totalStudents = allStudents.size
    val totalExpectedFee = allStudents.sumOf { it.monthlyFee }
    val totalCollectedFee = monthPayments.sumOf { it.amountPaid }
    val totalPendingFee = (totalExpectedFee - totalCollectedFee).coerceAtLeast(0.0)

    val totalAttendanceRecords = monthAttendance.size
    val totalPresentRecords = monthAttendance.count { it.status == "PRESENT" }
    val totalAbsentRecords = monthAttendance.count { it.status == "ABSENT" }
    val overallAttendancePercent = if (totalAttendanceRecords > 0) {
        ((totalPresentRecords.toDouble() / totalAttendanceRecords) * 100).toInt()
    } else 0

    // Unique dates attendance was recorded in this month
    val uniqueSessionDays = monthAttendance.map { it.date }.distinct().size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Month Selector Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Monthly Performance Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Month: $selectedMonth",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row {
                        OutlinedButton(
                            onClick = { showMonthPicker = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Month")
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = {
                                val reportText = buildString {
                                    appendLine("📊 ${profile.coachingName} - Monthly Report ($selectedMonth)")
                                    appendLine("Teacher: ${profile.teacherName}")
                                    appendLine("------------------------------")
                                    appendLine("👥 Total Students: $totalStudents")
                                    appendLine("📅 Working Days: $uniqueSessionDays")
                                    appendLine("✅ Total Present: $totalPresentRecords")
                                    appendLine("❌ Total Absent: $totalAbsentRecords")
                                    appendLine("📈 Attendance Rate: $overallAttendancePercent%")
                                    appendLine("------------------------------")
                                    appendLine("💰 Total Fee Expected: $currency ${String.format(Locale.US, "%,.0f", totalExpectedFee)}")
                                    appendLine("💵 Total Fee Collected: $currency ${String.format(Locale.US, "%,.0f", totalCollectedFee)}")
                                    appendLine("⚠️ Total Pending Fee: $currency ${String.format(Locale.US, "%,.0f", totalPendingFee)}")
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Monthly Report"))
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Attendance Summary / उपस्थिति सारांश",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportMetric("Classes Held", "$uniqueSessionDays days")
                        ReportMetric("Total Present", "$totalPresentRecords", PresentGreen)
                        ReportMetric("Total Absent", "$totalAbsentRecords", AbsentRed)
                        ReportMetric("Avg Attendance", "$overallAttendancePercent%")
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Fee Collection Summary / फीस सारांश",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportMetric("Expected Fee", "$currency ${String.format(Locale.US, "%,.0f", totalExpectedFee)}")
                        ReportMetric("Collected Fee", "$currency ${String.format(Locale.US, "%,.0f", totalCollectedFee)}", PresentGreen)
                        ReportMetric("Pending Fee", "$currency ${String.format(Locale.US, "%,.0f", totalPendingFee)}", AbsentRed)
                    }
                }
            }
        }

        // Student-by-Student breakdown header
        item {
            Text(
                text = "Student-wise Breakdown / छात्र-वार स्थिति",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Student-by-student cards
        items(allStudents, key = { it.id }) { student ->
            val studentAtt = monthAttendance.filter { it.studentId == student.id }
            val studentPresent = studentAtt.count { it.status == "PRESENT" }
            val studentAbsent = studentAtt.count { it.status == "ABSENT" }
            val studentTotal = studentPresent + studentAbsent
            val studentAttPercent = if (studentTotal > 0) ((studentPresent.toDouble() / studentTotal) * 100).toInt() else 0

            val studentPaid = monthPayments.filter { it.studentId == student.id }.sumOf { it.amountPaid }
            val studentPending = (student.monthlyFee - studentPaid).coerceAtLeast(0.0)
            val isPaidInFull = studentPaid >= student.monthlyFee

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 42, fontSize = 16)
                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Roll: #${student.rollNumber} • ${student.batch}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Att: $studentPresent/$studentTotal days ($studentAttPercent%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (studentAttPercent >= 75) PresentGreen else AbsentRed,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isPaidInFull) Color(0xFFDCFCE7) else if (studentPaid > 0) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = if (isPaidInFull) "PAID" else if (studentPaid > 0) "PARTIAL" else "DUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaidInFull) PresentGreen else if (studentPaid > 0) WarningAmber else AbsentRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Due: $currency ${String.format(Locale.US, "%,.0f", studentPending)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (studentPending > 0) AbsentRed else Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showMonthPicker) {
        SimpleMonthPickerDialog(
            currentMonthYear = selectedMonth,
            onMonthSelected = { viewModel.setSelectedMonth(it) },
            onDismiss = { showMonthPicker = false }
        )
    }
}

@Composable
fun ReportMetric(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
