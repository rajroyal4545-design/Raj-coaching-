package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.AvatarInitials
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SimpleDatePickerDialog
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedLight
import com.example.ui.theme.ClosedGray
import com.example.ui.theme.ClosedGrayLight
import com.example.ui.theme.LeaveBlue
import com.example.ui.theme.LeaveBlueLight
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenLight
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight
import com.example.ui.viewmodel.CoachingViewModel

@Composable
fun AttendanceScreen(viewModel: CoachingViewModel) {
    val students by viewModel.allStudents.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceMap by viewModel.attendanceForSelectedDate.collectAsState()
    val today = viewModel.todayDateString

    var showDatePicker by remember { mutableStateOf(false) }
    var showCloseConfirmDialog by remember { mutableStateOf(false) }

    val isToday = selectedDate == today
    val isPastDate = selectedDate < today

    // Calculate status counts
    val presentCount = students.count { attendanceMap[it.id]?.status == "PRESENT" }
    val absentCount = students.count { attendanceMap[it.id]?.status == "ABSENT" }
    val leaveCount = students.count { attendanceMap[it.id]?.status == "LEAVE" }
    val closedCount = students.count { attendanceMap[it.id]?.status == "CLOSED" }
    val isDayClosed = closedCount > 0 && closedCount >= (students.size / 2).coerceAtLeast(1)
    val unmarkedCount = if (isDayClosed) 0 else (students.size - presentCount - absentCount - leaveCount).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Date navigation bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.stepDate(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDatePicker = true },
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = selectedDate,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isToday) "Today / आज" else if (isPastDate) "Back Date / पिछली तारीख" else "Future Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPastDate) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.stepDate(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                }

                if (!isToday) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setSelectedDate(today) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Jump to Today", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Coaching Closed Banner (if day is marked closed)
        if (isDayClosed) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ClosedGrayLight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = ClosedGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Coaching Closed / कोचिंग बंद (अवकाश)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClosedGray
                            )
                            Text(
                                text = "इस तारीख पर कोचिंग का अवकाश घोषित है",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClosedGray
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearDayAttendance() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-Open / चालू करें", fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Attendance stats banner (Present, Absent, Leave, Unmarked)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = PresentGreenLight
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Present", style = MaterialTheme.typography.labelSmall, color = PresentGreen, fontWeight = FontWeight.Bold)
                    Text("$presentCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PresentGreen)
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = AbsentRedLight
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Absent", style = MaterialTheme.typography.labelSmall, color = AbsentRed, fontWeight = FontWeight.Bold)
                    Text("$absentCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AbsentRed)
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = LeaveBlueLight
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Leave (छुट्टी)", style = MaterialTheme.typography.labelSmall, color = LeaveBlue, fontWeight = FontWeight.Bold)
                    Text("$leaveCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LeaveBlue)
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = if (isDayClosed) ClosedGrayLight else WarningAmberLight
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isDayClosed) "Closed" else "Unmarked",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDayClosed) ClosedGray else WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isDayClosed) "बंद" else "$unmarkedCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDayClosed) ClosedGray else WarningAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bulk Action Bar with All Present, All Absent, All Leave, and Coaching Closed
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { viewModel.markAllPresent() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PresentGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.markAllAbsent() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("All Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.markAllLeave() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LeaveBlue)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("All Leave", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Dedicated "Coaching Closed (कोचिंग बंद)" Button
            OutlinedButton(
                onClick = { showCloseConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDayClosed) ClosedGray else Color(0xFF64748B)
                )
            ) {
                Icon(
                    imageVector = if (isDayClosed) Icons.Default.EventBusy else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isDayClosed) ClosedGray else Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDayClosed) "Mark Coaching as Open / पुनः चालू करें" else "Coaching Closed Today / आज कोचिंग बंद है",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Students Attendance List
        if (students.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.EventAvailable,
                title = "No Students Found",
                description = "Add students first from the Students tab to take attendance."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(students, key = { it.id }) { student ->
                    val record = attendanceMap[student.id]
                    val status = record?.status // "PRESENT", "ABSENT", "LEAVE", "CLOSED", or null

                    AttendanceStudentRow(
                        student = student,
                        status = status,
                        onMarkPresent = {
                            if (status == "PRESENT") viewModel.removeAttendance(student.id)
                            else viewModel.markAttendance(student.id, "PRESENT")
                        },
                        onMarkAbsent = {
                            if (status == "ABSENT") viewModel.removeAttendance(student.id)
                            else viewModel.markAttendance(student.id, "ABSENT")
                        },
                        onMarkLeave = {
                            if (status == "LEAVE") viewModel.removeAttendance(student.id)
                            else viewModel.markAttendance(student.id, "LEAVE")
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        SimpleDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { viewModel.setSelectedDate(it) },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showCloseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCloseConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = if (isDayClosed) Icons.Default.LockOpen else Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = if (isDayClosed) "Re-Open Coaching? / कोचिंग चालू करें?" else "Mark Coaching Closed? / कोचिंग बंद करें?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isDayClosed)
                        "Are you sure you want to re-open coaching for $selectedDate and clear closed status?"
                    else
                        "Do you want to mark $selectedDate as Coaching Holiday/Closed? All students will be marked as Closed for this date."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isDayClosed) {
                            viewModel.clearDayAttendance()
                        } else {
                            viewModel.markCoachingClosed()
                        }
                        showCloseConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDayClosed) PresentGreen else ClosedGray
                    )
                ) {
                    Text(if (isDayClosed) "Re-Open (चालू करें)" else "Yes, Close (बंद करें)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCloseConfirmDialog = false }) {
                    Text("Cancel / रद्द करें")
                }
            }
        )
    }
}

@Composable
fun AttendanceStudentRow(
    student: Student,
    status: String?,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onMarkLeave: () -> Unit
) {
    val isClosed = status == "CLOSED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                "PRESENT" -> Color(0xFFF0FDF4)
                "ABSENT" -> Color(0xFFFEF2F2)
                "LEAVE" -> Color(0xFFEFF6FF)
                "CLOSED" -> Color(0xFFF8FAFC)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 42, fontSize = 16)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Roll: #${student.rollNumber} • ${student.batch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isClosed) {
                    Text(
                        text = "Closed (अवकाश)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClosedGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Toggle Buttons with large touch targets: Present (P), Absent (A), Leave (L)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                // Present Button (P)
                Button(
                    onClick = onMarkPresent,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "PRESENT") PresentGreen else Color(0xFFE2E8F0),
                        contentColor = if (status == "PRESENT") Color.White else Color(0xFF475569)
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .width(44.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("P", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Absent Button (A)
                Button(
                    onClick = onMarkAbsent,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "ABSENT") AbsentRed else Color(0xFFE2E8F0),
                        contentColor = if (status == "ABSENT") Color.White else Color(0xFF475569)
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .width(44.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("A", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Leave Button (L)
                Button(
                    onClick = onMarkLeave,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "LEAVE") LeaveBlue else Color(0xFFE2E8F0),
                        contentColor = if (status == "LEAVE") Color.White else Color(0xFF475569)
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .width(44.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("L", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
