package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fee_payments")
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val amountPaid: Double,
    val paymentDate: String, // YYYY-MM-DD
    val forMonthYear: String, // YYYY-MM e.g. "2026-09"
    val paymentMode: String = "Cash", // Cash, UPI, Online, Cheque
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
