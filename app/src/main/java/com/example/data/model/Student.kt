package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val guardianName: String = "",
    val mobileNumber: String = "",
    val address: String = "",
    val studentClass: String = "10th",
    val batch: String = "Morning",
    val rollNumber: String = "",
    val joiningDate: String = "",
    val monthlyFee: Double = 1000.0,
    val isActive: Boolean = true,
    val avatarColorHex: String = "#2563EB"
)
