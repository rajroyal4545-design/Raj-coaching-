package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CoachingProfile
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.util.PdfSlipGenerator
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun PaymentSlipDialog(
    profile: CoachingProfile,
    student: Student,
    payment: FeePayment,
    totalPaidForMonth: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currency = profile.currencySymbol.ifBlank { "₹" }
    val pendingDue = (student.monthlyFee - totalPaidForMonth).coerceAtLeast(0.0)
    val isPaidInFull = pendingDue <= 0.0

    val receiptNumber = remember(payment) {
        "REC-${payment.forMonthYear.replace("-", "")}-${String.format(Locale.US, "%04d", payment.id)}"
    }

    // Generate or cache PDF file
    val pdfFile = remember(payment, student, profile, totalPaidForMonth) {
        try {
            PdfSlipGenerator.generateReceiptPdf(
                context = context,
                profile = profile,
                student = student,
                payment = payment,
                totalPaidForMonth = totalPaidForMonth,
                monthlyFee = student.monthlyFee
            )
        } catch (e: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Receipt badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Fee Payment Slip",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "फीस रसीद • $receiptNumber",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPaidInFull) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = if (isPaidInFull) "PAID" else "PARTIAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaidInFull) PresentGreen else WarningAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Receipt Slip Paper Card (Visual Simulation of Paper Receipt)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Coaching Institute Info
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = profile.coachingName.ifBlank { "Coaching Institute" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${profile.teacherName} • ${profile.mobileNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (profile.address.isNotBlank()) {
                                Text(
                                    text = profile.address,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Student Meta
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Student Name / छात्र", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "Roll: #${student.rollNumber} • ${student.batch}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Payment Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(payment.paymentDate, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                Text("Month: ${payment.forMonthYear}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Financial breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monthly Tuition Fee", style = MaterialTheme.typography.bodySmall)
                                Text("$currency ${String.format(Locale.US, "%,.0f", student.monthlyFee)}", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount Paid in This Slip (${payment.paymentMode})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = PresentGreen)
                                Text("$currency ${String.format(Locale.US, "%,.0f", payment.amountPaid)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = PresentGreen)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Cumulative Paid (${payment.forMonthYear})", style = MaterialTheme.typography.bodySmall)
                                Text("$currency ${String.format(Locale.US, "%,.0f", totalPaidForMonth)}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining Due / शेष बकाया", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (pendingDue > 0) AbsentRed else PresentGreen)
                                Text("$currency ${String.format(Locale.US, "%,.0f", pendingDue)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = if (pendingDue > 0) AbsentRed else PresentGreen)
                            }
                        }

                        if (payment.remarks.isNotBlank()) {
                            Text(
                                text = "Note: ${payment.remarks}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Seal / Signature line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👑 𝐑𝐨𝐲𝐚𝐥 𝐀𝐈 𝐇𝐮𝐛𝟒𝐔",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Authorized Sign / मुद्रा",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Action Buttons: Download PDF & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            pdfFile?.let { file ->
                                val cleanName = student.name.replace("\\s+".toRegex(), "_")
                                val fileName = "Fee_Slip_${cleanName}_${payment.forMonthYear}.pdf"
                                PdfSlipGenerator.saveToDownloads(context, file, fileName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PresentGreen)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF\nडाउनलोड स्लिप", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }

                    Button(
                        onClick = {
                            pdfFile?.let { file ->
                                PdfSlipGenerator.shareReceiptPdf(context, file, student.name, payment.forMonthYear)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Slip\nस्लिप भेजें", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }

                // Open in PDF Viewer and Close Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        pdfFile?.let { file ->
                            PdfSlipGenerator.openPdfReceipt(context, file)
                        }
                    }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open PDF / देखें", fontSize = 12.sp)
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Close / बंद करें", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
