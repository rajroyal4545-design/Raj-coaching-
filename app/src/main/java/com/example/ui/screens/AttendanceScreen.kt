package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
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
import com.example.ui.theme.IndigoLight
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

    val isToday = selectedDate == today
    val isPastDate = selectedDate < today

    val presentCount = students.count { attendanceMap[it.id]?.status == "PRESENT" }
    val absentCount = students.count { attendanceMap[it.id]?.status == "ABSENT" }
    val unmarkedCount = students.size - presentCount - absentCount

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

        // Attendance stats banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = PresentGreenLight
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Present / उपस्थित", style = MaterialTheme.typography.labelSmall, color = PresentGreen, fontWeight = FontWeight.Bold)
                    Text("$presentCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PresentGreen)
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = AbsentRedLight
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Absent / अनुपस्थित", style = MaterialTheme.typography.labelSmall, color = AbsentRed, fontWeight = FontWeight.Bold)
                    Text("$absentCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AbsentRed)
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = WarningAmberLight
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Unmarked / शेष", style = MaterialTheme.typography.labelSmall, color = WarningAmber, fontWeight = FontWeight.Bold)
                    Text("$unmarkedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarningAmber)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bulk Actions (Mark all present / Mark all absent)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.markAllPresent() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PresentGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("All Present (सब उपस्थित)", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { viewModel.markAllAbsent() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = AbsentRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text("All Absent", fontSize = 12.sp, color = AbsentRed)
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
                    val status = record?.status // "PRESENT", "ABSENT", or null

                    AttendanceStudentRow(
                        student = student,
                        status = status,
                        onMarkPresent = { viewModel.markAttendance(student.id, "PRESENT") },
                        onMarkAbsent = { viewModel.markAttendance(student.id, "ABSENT") },
                        onClear = { viewModel.removeAttendance(student.id) }
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
}

@Composable
fun AttendanceStudentRow(
    student: Student,
    status: String?,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                "PRESENT" -> Color(0xFFF0FDF4)
                "ABSENT" -> Color(0xFFFEF2F2)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 42, fontSize = 16)

            Spacer(modifier = Modifier.width(12.dp))

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
            }

            // Quick Toggle Buttons with large touch targets
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Present Button
                Button(
                    onClick = onMarkPresent,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "PRESENT") PresentGreen else Color(0xFFE2E8F0),
                        contentColor = if (status == "PRESENT") Color.White else Color(0xFF475569)
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("P", fontWeight = FontWeight.Bold)
                }

                // Absent Button
                Button(
                    onClick = onMarkAbsent,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "ABSENT") AbsentRed else Color(0xFFE2E8F0),
                        contentColor = if (status == "ABSENT") Color.White else Color(0xFF475569)
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("A", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
