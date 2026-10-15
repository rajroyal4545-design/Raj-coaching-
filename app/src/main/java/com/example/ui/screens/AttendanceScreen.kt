package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current
    val students by viewModel.allStudents.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceMap by viewModel.attendanceForSelectedDate.collectAsState()
    val today = viewModel.todayDateString

    var showDatePicker by remember { mutableStateOf(false) }
    var showCloseConfirmDialog by remember { mutableStateOf(false) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    val isToday = selectedDate == today
    val isPastDate = selectedDate < today

    // Back date lock state: automatically resets to false when date changes
    var isBackDateUnlocked by remember(selectedDate) { mutableStateOf(false) }
    val isLocked = isPastDate && !isBackDateUnlocked

    // Calculate status counts
    val presentCount = students.count { attendanceMap[it.id]?.status == "PRESENT" }
    val absentCount = students.count { attendanceMap[it.id]?.status == "ABSENT" }
    val leaveCount = students.count { attendanceMap[it.id]?.status == "LEAVE" }
    val closedCount = students.count { attendanceMap[it.id]?.status == "CLOSED" }

    // Day is considered closed if all records are CLOSED or closedCount represents the majority
    val isDayClosed = (closedCount > 0 && (students.isEmpty() || closedCount >= (students.size / 2).coerceAtLeast(1))) ||
        (attendanceMap.isNotEmpty() && attendanceMap.values.all { it.status == "CLOSED" })

    val unmarkedCount = if (isDayClosed) 0 else (students.size - presentCount - absentCount - leaveCount).coerceAtLeast(0)
    val isDark = isSystemInDarkTheme()

    // Helper to guard action modifications against locked back days
    val executeWithLockCheck: (() -> Unit) -> Unit = { action ->
        if (isLocked) {
            showUnlockDialog = true
            Toast.makeText(
                context,
                "पिछली तारीख की उपस्थिति लॉक है! बदलाव के लिए अनलॉक करें।",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            action()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Date navigation bar
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

        // 2. Back Date Lock Banner (Shown for past dates)
        if (isPastDate) {
            if (isLocked) {
                // Locked state banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF2D2415) else Color(0xFFFFFBEB)
                    ),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = WarningAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "पिछली उपस्थिति लॉक है (Locked)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                                )
                                Text(
                                    text = "बदलाव करने के लिए Unlock बटन दबाएं",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
                                )
                            }
                        }

                        Button(
                            onClick = { showUnlockDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                // Unlocked state banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF142E20) else Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, PresentGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = PresentGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔓 संपादन हेतु अनलॉक है (Unlocked to Edit)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF86EFAC) else Color(0xFF166534)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                isBackDateUnlocked = false
                                Toast.makeText(context, "उपस्थिति पुनः लॉक कर दी गई / Locked", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lock Again", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 3. CASE A: COACHING CLOSED
        // When day is closed: Do NOT show student list! Show BADE WORD ME CLOSED hero card!
        if (isDayClosed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                ),
                border = BorderStroke(2.dp, if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Big Badge Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = "Closed",
                            tint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    // BADE WORD ME CLOSED
                    Text(
                        text = "CLOSED",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 6.sp
                        ),
                        color = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                    )

                    Text(
                        text = "कोचिंग बंद है (अवकाश)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "तारीख: $selectedDate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "आज संस्थान में आधिकारिक अवकाश है।\nइस दिन किसी भी छात्र का नाम या उपस्थिति दर्ज नहीं है।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Re-Open Coaching button (checks lock if on a back date)
                    Button(
                        onClick = {
                            executeWithLockCheck {
                                showCloseConfirmDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PresentGreen),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Re-Open Coaching / पुनः चालू करें",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            // 4. CASE B: COACHING OPEN
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
                    color = WarningAmberLight
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Unmarked", style = MaterialTheme.typography.labelSmall, color = WarningAmber, fontWeight = FontWeight.Bold)
                        Text("$unmarkedCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarningAmber)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bulk Action Bar (All Present, All Absent, All Leave, Coaching Closed)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            executeWithLockCheck { viewModel.markAllPresent() }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PresentGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            executeWithLockCheck { viewModel.markAllAbsent() }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            executeWithLockCheck { viewModel.markAllLeave() }
                        },
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
                    onClick = {
                        executeWithLockCheck { showCloseConfirmDialog = true }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF64748B)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coaching Closed Today / आज कोचिंग बंद है",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Students Attendance List (Only shown when coaching is NOT closed!)
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
                        val status = record?.status // "PRESENT", "ABSENT", "LEAVE", or null

                        AttendanceStudentRow(
                            student = student,
                            status = status,
                            isLocked = isLocked,
                            onLockedAttempt = {
                                showUnlockDialog = true
                                Toast.makeText(
                                    context,
                                    "पिछली तारीख लॉक है! बदलाव के लिए अनलॉक करें।",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
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
    }

    // Date Picker Dialog
    if (showDatePicker) {
        SimpleDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { viewModel.setSelectedDate(it) },
            onDismiss = { showDatePicker = false }
        )
    }

    // Unlock Dialog for Back Date
    if (showUnlockDialog) {
        AlertDialog(
            onDismissRequest = { showUnlockDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Unlock Attendance? / उपस्थिति अनलॉक करें?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "यह पिछली तारीख ($selectedDate) की उपस्थिति है। क्या आप इसमें बदलाव करने के लिए इसे अनलॉक करना चाहते हैं?\n\n(This will allow you to edit attendance records for this back date.)"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isBackDateUnlocked = true
                        showUnlockDialog = false
                        Toast.makeText(
                            context,
                            "तारीख $selectedDate संपादन हेतु अनलॉक हो गई / Unlocked",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                ) {
                    Text("Unlock (अनलॉक करें)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUnlockDialog = false }) {
                    Text("Cancel / रद्द करें")
                }
            }
        )
    }

    // Coaching Closed / Re-Open Confirm Dialog
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
                        "Are you sure you want to re-open coaching for $selectedDate and clear closed status?\n(क्या आप $selectedDate के लिए कोचिंग पुनः चालू करना चाहते हैं?)"
                    else
                        "Do you want to mark $selectedDate as Coaching Holiday/Closed? All students list will be hidden and marked as Closed.\n(क्या आप $selectedDate को कोचिंग का अवकाश घोषित करना चाहते हैं?)"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isDayClosed) {
                            viewModel.clearDayAttendance()
                            Toast.makeText(context, "Coaching re-opened / कोचिंग पुनः चालू की गई", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.markCoachingClosed()
                            Toast.makeText(context, "Coaching marked as closed / कोचिंग बंद दर्ज की गई", Toast.LENGTH_SHORT).show()
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
    isLocked: Boolean = false,
    onLockedAttempt: () -> Unit = {},
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onMarkLeave: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val cardBg = if (isDark) {
        when (status) {
            "PRESENT" -> Color(0xFF142E20)
            "ABSENT" -> Color(0xFF331518)
            "LEAVE" -> Color(0xFF14203A)
            else -> MaterialTheme.colorScheme.surface
        }
    } else {
        when (status) {
            "PRESENT" -> Color(0xFFF0FDF4)
            "ABSENT" -> Color(0xFFFEF2F2)
            "LEAVE" -> Color(0xFFEFF6FF)
            else -> MaterialTheme.colorScheme.surface
        }
    }

    val cardBorder = if (isDark) {
        when (status) {
            "PRESENT" -> BorderStroke(1.5.dp, PresentGreen.copy(alpha = 0.8f))
            "ABSENT" -> BorderStroke(1.5.dp, AbsentRed.copy(alpha = 0.8f))
            "LEAVE" -> BorderStroke(1.5.dp, LeaveBlue.copy(alpha = 0.8f))
            else -> BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
        }
    } else {
        when (status) {
            "PRESENT" -> BorderStroke(1.dp, PresentGreen.copy(alpha = 0.4f))
            "ABSENT" -> BorderStroke(1.dp, AbsentRed.copy(alpha = 0.4f))
            "LEAVE" -> BorderStroke(1.dp, LeaveBlue.copy(alpha = 0.4f))
            else -> BorderStroke(1.dp, Color(0xFFE2E8F0))
        }
    }

    val studentNameColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val rollColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = studentNameColor
                    )
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = WarningAmber.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Text(
                    text = "Roll: #${student.rollNumber} • ${student.batch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = rollColor
                )
            }

            // Quick Toggle Buttons: Present (P), Absent (A), Leave (L)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                val unselectedBtnBg = if (isDark) Color(0xFF273549) else Color(0xFFE2E8F0)
                val unselectedBtnContent = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

                // Present Button (P)
                Button(
                    onClick = {
                        if (isLocked) onLockedAttempt()
                        else onMarkPresent()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "PRESENT") PresentGreen else unselectedBtnBg,
                        contentColor = if (status == "PRESENT") Color.White else unselectedBtnContent
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .width(44.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("P", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Absent Button (A)
                Button(
                    onClick = {
                        if (isLocked) onLockedAttempt()
                        else onMarkAbsent()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "ABSENT") AbsentRed else unselectedBtnBg,
                        contentColor = if (status == "ABSENT") Color.White else unselectedBtnContent
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .width(44.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("A", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Leave Button (L)
                Button(
                    onClick = {
                        if (isLocked) onLockedAttempt()
                        else onMarkLeave()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "LEAVE") LeaveBlue else unselectedBtnBg,
                        contentColor = if (status == "LEAVE") Color.White else unselectedBtnContent
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .width(44.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("L", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
