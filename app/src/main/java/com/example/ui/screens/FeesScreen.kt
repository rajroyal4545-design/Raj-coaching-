package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.components.AvatarInitials
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SimpleDatePickerDialog
import com.example.ui.components.SimpleMonthPickerDialog
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.CoachingViewModel
import com.example.ui.viewmodel.StudentFeeSummary
import java.util.Locale

@Composable
fun FeesScreen(viewModel: CoachingViewModel) {
    val feeSummaries by viewModel.studentFeeSummaries.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val profile by viewModel.coachingProfile.collectAsState()
    val currency = profile.currencySymbol

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Student Status, 1: Recent Payments
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var paymentToEdit by remember { mutableStateOf<FeePayment?>(null) }
    var paymentToDelete by remember { mutableStateOf<FeePayment?>(null) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var preselectedStudentForPayment by remember { mutableStateOf<Student?>(null) }

    val totalCollected = feeSummaries.sumOf { it.totalPaidForMonth }
    val totalPending = feeSummaries.sumOf { it.pendingForMonth }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Month Selector Bar & Summary Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Fee Month / फीस का महीना",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedMonth,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedButton(
                            onClick = { showMonthPicker = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Collected / जमा फीस", style = MaterialTheme.typography.labelSmall, color = PresentGreen)
                                Text(
                                    text = "$currency ${String.format(Locale.US, "%,.0f", totalCollected)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PresentGreen
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Pending / बकाया फीस", style = MaterialTheme.typography.labelSmall, color = AbsentRed)
                                Text(
                                    text = "$currency ${String.format(Locale.US, "%,.0f", totalPending)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AbsentRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs: Student-wise vs Payment Records
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Student Status (${feeSummaries.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("All Payments (${allPayments.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTabIndex == 0) {
                // Student Status List
                if (feeSummaries.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Payments,
                        title = "No Students",
                        description = "Add students in the Students tab to track fees."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(feeSummaries, key = { it.student.id }) { summary ->
                            StudentFeeCard(
                                summary = summary,
                                currency = currency,
                                onPayNow = {
                                    preselectedStudentForPayment = summary.student
                                    showAddPaymentDialog = true
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            } else {
                // Payment History List
                if (allPayments.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Payments,
                        title = "No Payments Recorded",
                        description = "Click '+ Add Payment' to record your first fee transaction."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allPayments, key = { it.id }) { payment ->
                            val student = allStudents.find { it.id == payment.studentId }
                            PaymentHistoryRow(
                                payment = payment,
                                studentName = student?.name ?: "Student #${payment.studentId}",
                                currency = currency,
                                onEdit = { paymentToEdit = payment },
                                onDelete = { paymentToDelete = payment }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }

        // FAB to Record Fee Payment
        FloatingActionButton(
            onClick = {
                preselectedStudentForPayment = null
                showAddPaymentDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = Color(0xFF059669),
            contentColor = Color.White
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Add Payment")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Record Payment", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Payment Dialog
    if (showAddPaymentDialog) {
        FeePaymentFormDialog(
            initialPayment = null,
            students = allStudents,
            preselectedStudent = preselectedStudentForPayment,
            defaultMonth = selectedMonth,
            onDismiss = {
                showAddPaymentDialog = false
                preselectedStudentForPayment = null
            },
            onSave = { payment ->
                viewModel.addFeePayment(payment)
                showAddPaymentDialog = false
                preselectedStudentForPayment = null
            }
        )
    }

    // Edit Payment Dialog
    paymentToEdit?.let { payment ->
        FeePaymentFormDialog(
            initialPayment = payment,
            students = allStudents,
            preselectedStudent = allStudents.find { it.id == payment.studentId },
            defaultMonth = payment.forMonthYear,
            onDismiss = { paymentToEdit = null },
            onSave = { updated ->
                viewModel.updateFeePayment(updated)
                paymentToEdit = null
            }
        )
    }

    // Delete Payment Confirmation
    paymentToDelete?.let { payment ->
        ConfirmDeleteDialog(
            title = "Delete Payment Record / भुगतान हटाएं?",
            message = "Are you sure you want to delete this payment record of $currency ${payment.amountPaid}?",
            onConfirm = {
                viewModel.deleteFeePayment(payment)
                paymentToDelete = null
            },
            onDismiss = { paymentToDelete = null }
        )
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
fun StudentFeeCard(
    summary: StudentFeeSummary,
    currency: String,
    onPayNow: () -> Unit
) {
    val student = summary.student
    val isPaid = summary.isPaidInFull
    val isPartial = summary.totalPaidForMonth > 0 && !isPaid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 42, fontSize = 16)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Roll: #${student.rollNumber} • ${student.batch}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isPaid -> Color(0xFFDCFCE7)
                        isPartial -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    }
                ) {
                    Text(
                        text = when {
                            isPaid -> "PAID"
                            isPartial -> "PARTIAL"
                            else -> "DUE"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isPaid -> PresentGreen
                            isPartial -> WarningAmber
                            else -> AbsentRed
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Monthly Fee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$currency ${String.format(Locale.US, "%,.0f", student.monthlyFee)}", fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Paid", style = MaterialTheme.typography.labelSmall, color = PresentGreen)
                    Text("$currency ${String.format(Locale.US, "%,.0f", summary.totalPaidForMonth)}", fontWeight = FontWeight.SemiBold, color = PresentGreen)
                }
                Column {
                    Text("Pending Due", style = MaterialTheme.typography.labelSmall, color = AbsentRed)
                    Text("$currency ${String.format(Locale.US, "%,.0f", summary.pendingForMonth)}", fontWeight = FontWeight.Bold, color = AbsentRed)
                }

                Button(
                    onClick = onPayNow,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("+ Pay", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryRow(
    payment: FeePayment,
    studentName: String,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$currency ${String.format(Locale.US, "%,.0f", payment.amountPaid)} via ${payment.paymentMode} • For ${payment.forMonthYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PresentGreen,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Date: ${payment.paymentDate}" + (if (payment.remarks.isNotBlank()) " • ${payment.remarks}" else ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Payment", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = AbsentRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeePaymentFormDialog(
    initialPayment: FeePayment?,
    students: List<Student>,
    preselectedStudent: Student?,
    defaultMonth: String,
    onDismiss: () -> Unit,
    onSave: (FeePayment) -> Unit
) {
    var selectedStudent by remember {
        mutableStateOf(preselectedStudent ?: students.firstOrNull())
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    var amountText by remember {
        mutableStateOf(
            initialPayment?.amountPaid?.let { String.format(Locale.US, "%.0f", it) }
                ?: selectedStudent?.monthlyFee?.let { String.format(Locale.US, "%.0f", it) }
                ?: "1000"
        )
    }
    var paymentDate by remember { mutableStateOf(initialPayment?.paymentDate ?: "2026-09-03") }
    var forMonthYear by remember { mutableStateOf(initialPayment?.forMonthYear ?: defaultMonth) }
    var paymentMode by remember { mutableStateOf(initialPayment?.paymentMode ?: "Cash") }
    var remarks by remember { mutableStateOf(initialPayment?.remarks ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val paymentModes = listOf("Cash", "UPI", "Online", "Cheque")

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
                    text = if (initialPayment == null) "Record Fee Payment / फीस भुगतान" else "Edit Payment / भुगतान संपादित करें",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Student Dropdown Selector
                Text("Select Student / छात्र चुनें *", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.name ?: "Select student",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (Roll #${s.rollNumber} - ${s.batch})") },
                                onClick = {
                                    selectedStudent = s
                                    if (initialPayment == null) {
                                        amountText = String.format(Locale.US, "%.0f", s.monthlyFee)
                                    }
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid (₹) / जमा राशि") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Date (supports back-dating!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = paymentDate,
                        onValueChange = { paymentDate = it },
                        label = { Text("Payment Date") },
                        supportingText = { Text("Back-dating supported") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Month for which fee is being paid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = forMonthYear,
                        onValueChange = { forMonthYear = it },
                        label = { Text("Fee For Month (YYYY-MM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { showMonthPicker = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Mode Chips
                Text("Payment Mode / भुगतान का माध्यम:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentModes.forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Note (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val studentId = selectedStudent?.id ?: return@Button
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount <= 0.0) return@Button

                            val payment = (initialPayment ?: FeePayment(
                                studentId = studentId,
                                amountPaid = amount,
                                paymentDate = paymentDate,
                                forMonthYear = forMonthYear
                            )).copy(
                                studentId = studentId,
                                amountPaid = amount,
                                paymentDate = paymentDate,
                                forMonthYear = forMonthYear,
                                paymentMode = paymentMode,
                                remarks = remarks.trim()
                            )
                            onSave(payment)
                        }
                    ) {
                        Text("Save / सहेजें")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        SimpleDatePickerDialog(
            initialDate = paymentDate,
            onDateSelected = { paymentDate = it },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showMonthPicker) {
        SimpleMonthPickerDialog(
            currentMonthYear = forMonthYear,
            onMonthSelected = { forMonthYear = it },
            onDismiss = { showMonthPicker = false }
        )
    }
}
