package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AttendanceRecord
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.components.AvatarInitials
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.PresentGreen
import com.example.ui.viewmodel.CoachingViewModel
import java.util.Locale

@Composable
fun StudentsScreen(viewModel: CoachingViewModel) {
    val students by viewModel.filteredStudents.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val selectedBatch by viewModel.selectedBatchFilter.collectAsState()
    val profile by viewModel.coachingProfile.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var studentForDetail by remember { mutableStateOf<Student?>(null) }

    val batches = remember(allStudents) {
        listOf("All") + allStudents.map { it.batch }.filter { it.isNotBlank() }.distinct()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setStudentSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, roll, class... / खोजें") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Batch filter chips
            if (batches.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(batches) { batch ->
                        FilterChip(
                            selected = selectedBatch == batch,
                            onClick = { viewModel.setSelectedBatchFilter(batch) },
                            label = { Text(if (batch == "All") "All Batches (${allStudents.size})" else batch) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (students.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Person,
                    title = if (searchQuery.isEmpty()) "No Students Added Yet" else "No matching students found",
                    description = if (searchQuery.isEmpty()) "Add your coaching students to start tracking attendance and fee records easily." else "Try adjusting your search query or batch filter.",
                    actionLabel = if (searchQuery.isEmpty()) "+ Add First Student" else null,
                    onAction = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(students, key = { it.id }) { student ->
                        StudentCard(
                            student = student,
                            currency = profile.currencySymbol,
                            onClick = { studentForDetail = student },
                            onEdit = { studentToEdit = student },
                            onDelete = { studentToDelete = student }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Student
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Student", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        StudentFormDialog(
            student = null,
            onDismiss = { showAddDialog = false },
            onSave = { newStudent ->
                viewModel.addStudent(newStudent)
                showAddDialog = false
            }
        )
    }

    // Edit Student Dialog
    studentToEdit?.let { student ->
        StudentFormDialog(
            student = student,
            onDismiss = { studentToEdit = null },
            onSave = { updatedStudent ->
                viewModel.updateStudent(updatedStudent)
                studentToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { student ->
        ConfirmDeleteDialog(
            title = "Delete Student / छात्र हटाएं?",
            message = "Are you sure you want to delete ${student.name}? All their attendance and fee records will also be permanently removed.",
            onConfirm = {
                viewModel.deleteStudent(student)
                studentToDelete = null
            },
            onDismiss = { studentToDelete = null }
        )
    }

    // Student Detail Dialog
    studentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            viewModel = viewModel,
            currency = profile.currencySymbol,
            onDismiss = { studentForDetail = null },
            onEdit = {
                studentToEdit = student
                studentForDetail = null
            }
        )
    }
}

@Composable
fun StudentCard(
    student: Student,
    currency: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInitials(
                name = student.name,
                colorHex = student.avatarColorHex,
                size = 46,
                fontSize = 17
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (student.rollNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "#${student.rollNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Class ${student.studentClass} • ${student.batch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Fee: $currency ${String.format(Locale.US, "%,.0f", student.monthlyFee)}/mo",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF059669)
                )
            }

            // Call shortcut button
            if (student.mobileNumber.isNotBlank()) {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${student.mobileNumber}")
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Student",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Edit button
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StudentFormDialog(
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var guardianName by remember { mutableStateOf(student?.guardianName ?: "") }
    var mobileNumber by remember { mutableStateOf(student?.mobileNumber ?: "") }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var studentClass by remember { mutableStateOf(student?.studentClass ?: "10th") }
    var batch by remember { mutableStateOf(student?.batch ?: "Morning") }
    var rollNumber by remember { mutableStateOf(student?.rollNumber ?: "") }
    var joiningDate by remember { mutableStateOf(student?.joiningDate ?: "") }
    var monthlyFeeText by remember { mutableStateOf(student?.monthlyFee?.let { String.format(Locale.US, "%.0f", it) } ?: "1000") }
    var nameError by remember { mutableStateOf(false) }

    val colors = listOf("#2563EB", "#7C3AED", "#059669", "#D97706", "#DC2626", "#0D9488")
    var selectedColor by remember { mutableStateOf(student?.avatarColorHex ?: colors.random()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (student == null) "Add New Student / नया छात्र" else "Edit Student / छात्र संपादित करें",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Student Name * / छात्र का नाम") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Name is required") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll No / रोल नंबर") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = studentClass,
                        onValueChange = { studentClass = it },
                        label = { Text("Class / कक्षा") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Batch / बैच (e.g. Morning, Evening)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = guardianName,
                    onValueChange = { guardianName = it },
                    label = { Text("Father/Mother Name / अभिभावक का नाम") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number / मोबाइल नंबर") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = monthlyFeeText,
                    onValueChange = { monthlyFeeText = it },
                    label = { Text("Monthly Fee (₹) / मासिक फीस") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / पता") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar color selection
                Text("Select Tag Color:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        val parsed = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .clickable { selectedColor = hex }
                                .padding(if (selectedColor == hex) 3.dp else 0.dp)
                        ) {
                            if (selectedColor == hex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val fee = monthlyFeeText.toDoubleOrNull() ?: 1000.0
                            val updated = (student ?: Student(name = name)).copy(
                                name = name.trim(),
                                guardianName = guardianName.trim(),
                                mobileNumber = mobileNumber.trim(),
                                address = address.trim(),
                                studentClass = studentClass.trim(),
                                batch = batch.trim(),
                                rollNumber = rollNumber.trim(),
                                joiningDate = joiningDate.ifBlank { "2026-09-01" },
                                monthlyFee = fee,
                                avatarColorHex = selectedColor
                            )
                            onSave(updated)
                        }
                    ) {
                        Text("Save / सहेजें")
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDetailDialog(
    student: Student,
    viewModel: CoachingViewModel,
    currency: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val payments by viewModel.getStudentPayments(student.id).collectAsState(initial = emptyList())
    val attendanceRecords by viewModel.getStudentAttendance(student.id).collectAsState(initial = emptyList())

    val totalPaid = payments.sumOf { it.amountPaid }
    val presentCount = attendanceRecords.count { it.status == "PRESENT" }
    val absentCount = attendanceRecords.count { it.status == "ABSENT" }
    val totalDays = presentCount + absentCount
    val attPercentage = if (totalDays > 0) ((presentCount.toDouble() / totalDays) * 100).toInt() else 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 52, fontSize = 20)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Roll: #${student.rollNumber} • Class ${student.studentClass}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action contact buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (student.mobileNumber.isNotBlank()) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${student.mobileNumber}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val url = "https://api.whatsapp.com/send?phone=${student.mobileNumber}&text=Hello%20${student.name},"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    // Fallback to regular dialer
                                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobileNumber}"))
                                    context.startActivity(dial)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontSize = 13.sp, color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Student details list
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow("Batch / बैच:", student.batch)
                        DetailRow("Guardian / अभिभावक:", student.guardianName.ifBlank { "Not specified" })
                        DetailRow("Phone / फ़ोन:", student.mobileNumber.ifBlank { "Not provided" })
                        DetailRow("Address / पता:", student.address.ifBlank { "Not provided" })
                        DetailRow("Monthly Fee / फीस:", "$currency ${String.format(Locale.US, "%,.0f", student.monthlyFee)}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Attendance Stats
                Text("Attendance History / उपस्थिति", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Present", style = MaterialTheme.typography.labelSmall, color = PresentGreen)
                            Text("$presentCount", fontWeight = FontWeight.Bold, color = PresentGreen)
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Absent", style = MaterialTheme.typography.labelSmall, color = AbsentRed)
                            Text("$absentCount", fontWeight = FontWeight.Bold, color = AbsentRed)
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rate", style = MaterialTheme.typography.labelSmall)
                            Text("$attPercentage%", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fee Payments List
                Text("Payment History / फीस इतिहास", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                if (payments.isEmpty()) {
                    Text("No payment recorded yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    payments.take(5).forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$currency ${String.format(Locale.US, "%,.0f", p.amountPaid)} (${p.paymentMode})",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Date: ${p.paymentDate} • For: ${p.forMonthYear}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
