package com.example.data.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.CoachingProfile
import com.example.data.model.FeePayment
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfSlipGenerator {

    /**
     * Generates a printable PDF payment receipt and saves it to app cache.
     */
    fun generateReceiptPdf(
        context: Context,
        profile: CoachingProfile,
        student: Student,
        payment: FeePayment,
        totalPaidForMonth: Double = payment.amountPaid,
        monthlyFee: Double = student.monthlyFee
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842 // Standard A4 points
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val currency = profile.currencySymbol.ifBlank { "₹" }
        val pendingDue = (monthlyFee - totalPaidForMonth).coerceAtLeast(0.0)
        val isPaidInFull = pendingDue <= 0.0

        // 1. Decorative Header Background Banner
        paint.color = Color.parseColor("#312E81") // Deep Indigo
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 130f, paint)

        // Header Accent Strip
        paint.color = Color.parseColor("#4F46E5")
        canvas.drawRect(0f, 130f, pageWidth.toFloat(), 136f, paint)

        // Coaching Name
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val coachingTitle = profile.coachingName.ifBlank { "Coaching Institute" }
        canvas.drawText(coachingTitle, 36f, 50f, paint)

        // Teacher & Contact
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subtitle = "${profile.teacherName} • Mobile: ${profile.mobileNumber}"
        canvas.drawText(subtitle, 36f, 75f, paint)

        // Address
        if (profile.address.isNotBlank()) {
            canvas.drawText("Address: ${profile.address}", 36f, 96f, paint)
        }

        // Receipt Tag
        paint.color = Color.parseColor("#E0E7FF")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("OFFICIAL FEE SLIP", (pageWidth - 160).toFloat(), 50f, paint)

        // 2. Receipt Title Banner
        val receiptNumber = "REC-${payment.forMonthYear.replace("-", "")}-${String.format(Locale.US, "%04d", payment.id)}"
        
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FEE PAYMENT RECEIPT / फीस रसीद", 36f, 175f, paint)

        // Meta info row
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Receipt No: $receiptNumber", 36f, 196f, paint)
        canvas.drawText("Date: ${payment.paymentDate}", (pageWidth - 180).toFloat(), 196f, paint)

        // Divider
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(36f, 210f, (pageWidth - 36).toFloat(), 210f, paint)

        // 3. Student Details Box
        paint.color = Color.parseColor("#F8FAFC")
        val studentBox = RectF(36f, 224f, (pageWidth - 36).toFloat(), 304f)
        canvas.drawRoundRect(studentBox, 8f, 8f, paint)

        // Border for student box
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(studentBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Student Box Details
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Student Name: ${student.name}", 52f, 252f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val rollText = if (student.rollNumber.isNotBlank()) "Roll No: #${student.rollNumber}" else "Student ID: #${student.id}"
        canvas.drawText("$rollText   •   Batch: ${student.batch}", 52f, 274f, paint)
        canvas.drawText("Class: ${student.studentClass}   •   Phone: ${student.mobileNumber.ifBlank { "N/A" }}", 52f, 292f, paint)

        // Fee Period Tag on the right
        paint.color = Color.parseColor("#EEF2FF")
        val monthBox = RectF((pageWidth - 170).toFloat(), 240f, (pageWidth - 52).toFloat(), 286f)
        canvas.drawRoundRect(monthBox, 6f, 6f, paint)
        paint.color = Color.parseColor("#3730A3")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FEE PERIOD", (pageWidth - 156).toFloat(), 258f, paint)
        paint.textSize = 12f
        canvas.drawText(payment.forMonthYear, (pageWidth - 156).toFloat(), 276f, paint)

        // 4. Payment Breakdown Table
        val tableTop = 330f
        paint.color = Color.parseColor("#F1F5F9")
        val tableHeader = RectF(36f, tableTop, (pageWidth - 36).toFloat(), tableTop + 30f)
        canvas.drawRoundRect(tableHeader, 4f, 4f, paint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DESCRIPTION / विवरण", 52f, tableTop + 20f, paint)
        canvas.drawText("AMOUNT / रक़म", (pageWidth - 140).toFloat(), tableTop + 20f, paint)

        // Table Rows
        var currentY = tableTop + 55f
        fun drawRow(label: String, amountStr: String, isBold: Boolean = false, textColor: Int = Color.parseColor("#0F172A")) {
            paint.color = textColor
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            canvas.drawText(label, 52f, currentY, paint)
            canvas.drawText(amountStr, (pageWidth - 140).toFloat(), currentY, paint)

            paint.color = Color.parseColor("#F1F5F9")
            paint.strokeWidth = 1f
            canvas.drawLine(36f, currentY + 12f, (pageWidth - 36).toFloat(), currentY + 12f, paint)
            currentY += 34f
        }

        drawRow("Monthly Coaching Tuition Fee (${payment.forMonthYear})", "$currency ${String.format(Locale.US, "%,.0f", monthlyFee)}")
        drawRow("This Payment Received (${payment.paymentMode})", "$currency ${String.format(Locale.US, "%,.0f", payment.amountPaid)}", isBold = true, textColor = Color.parseColor("#059669"))
        drawRow("Total Cumulative Paid for Month", "$currency ${String.format(Locale.US, "%,.0f", totalPaidForMonth)}")
        
        val dueColor = if (pendingDue > 0) Color.parseColor("#DC2626") else Color.parseColor("#059669")
        val dueLabel = if (pendingDue > 0) "Remaining Balance Due (शेष बकाया)" else "Remaining Balance (बकाया शून्य)"
        drawRow(dueLabel, "$currency ${String.format(Locale.US, "%,.0f", pendingDue)}", isBold = true, textColor = dueColor)

        // 5. Payment Mode & Status Badges
        currentY += 15f
        paint.color = Color.parseColor("#475569")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Payment Mode: ${payment.paymentMode}", 52f, currentY, paint)
        if (payment.remarks.isNotBlank()) {
            canvas.drawText("Remarks / Note: ${payment.remarks}", 52f, currentY + 18f, paint)
        }

        // Status Stamp Box
        val stampBox = RectF((pageWidth - 210).toFloat(), currentY - 14f, (pageWidth - 36).toFloat(), currentY + 36f)
        paint.color = if (isPaidInFull) Color.parseColor("#DCFCE7") else Color.parseColor("#FEF3C7")
        canvas.drawRoundRect(stampBox, 8f, 8f, paint)
        paint.color = if (isPaidInFull) Color.parseColor("#16A34A") else Color.parseColor("#D97706")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(stampBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val stampText = if (isPaidInFull) "✔ PAID / पूर्ण चुकता" else "PARTIAL / आंशिक"
        canvas.drawText(stampText, (pageWidth - 195).toFloat(), currentY + 16f, paint)

        // 6. Signatures and Disclaimer
        val signY = 660f
        paint.color = Color.parseColor("#94A3B8")
        paint.strokeWidth = 1f
        canvas.drawLine((pageWidth - 200).toFloat(), signY, (pageWidth - 36).toFloat(), signY, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Authorized Signature / Director", (pageWidth - 196).toFloat(), signY + 18f, paint)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(profile.teacherName, (pageWidth - 196).toFloat(), signY + 34f, paint)

        // Bottom Terms Note
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        canvas.drawText("• This is a computer-generated fee receipt. No physical signature required.", 36f, 750f, paint)
        canvas.drawText("• Fees once paid are subject to the coaching institute's rules and policies.", 36f, 765f, paint)

        // Watermark Footer
        paint.color = Color.parseColor("#CBD5E1")
        paint.strokeWidth = 1f
        canvas.drawLine(36f, 785f, (pageWidth - 36).toFloat(), 785f, paint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Coaching Attendance & Fee Manager", 36f, 805f, paint)
        canvas.drawText("👑 𝐑𝐨𝐲𝐚𝐥 𝐀𝐈 𝐇𝐮𝐛𝟒𝐔", (pageWidth - 170).toFloat(), 805f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to cache file
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val cleanStudentName = student.name.replace("\\s+".toRegex(), "_")
        val fileName = "Receipt_${cleanStudentName}_${payment.forMonthYear}_$timeStamp.pdf"
        val cacheDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val pdfFile = File(cacheDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Downloads/saves the generated PDF file to the device's public Downloads directory.
     */
    fun saveToDownloads(context: Context, pdfFile: File, displayName: String): Uri? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CoachingReceipts")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { output ->
                        pdfFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    Toast.makeText(context, "Receipt saved to Downloads / CoachingReceipts!", Toast.LENGTH_LONG).show()
                    return uri
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "CoachingReceipts").apply { mkdirs() }
                val targetFile = File(targetDir, displayName)
                pdfFile.copyTo(targetFile, overwrite = true)
                Toast.makeText(context, "Receipt saved to: ${targetFile.absolutePath}", Toast.LENGTH_LONG).show()
                return Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    /**
     * Shares the PDF receipt via WhatsApp, Telegram, Email, etc.
     */
    fun shareReceiptPdf(context: Context, pdfFile: File, studentName: String, month: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Fee Receipt - $studentName ($month)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "फीस रसीद (Fee Receipt): छात्र $studentName की माह $month की फीस रसीद संलग्न है। धन्यवाद।"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Payment Slip / रसीद भेजें"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the PDF receipt in any PDF viewer installed on the device.
     */
    fun openPdfReceipt(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found on device.", Toast.LENGTH_SHORT).show()
        }
    }
}
