package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.CoachingProfileDao
import com.example.data.dao.FeePaymentDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.CoachingProfile
import com.example.data.model.FeePayment
import com.example.data.model.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CoachingProfile::class,
        Student::class,
        AttendanceRecord::class,
        FeePayment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun coachingProfileDao(): CoachingProfileDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun feePaymentDao(): FeePaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coaching_manager.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            val profileDao = database.coachingProfileDao()
            profileDao.insertOrUpdate(
                CoachingProfile(
                    id = 1,
                    coachingName = "Apex Coaching Classes",
                    teacherName = "Prof. R.K. Sharma",
                    mobileNumber = "+91 98765 43210",
                    address = "Station Road, Kota, Rajasthan",
                    currencySymbol = "₹"
                )
            )

            val studentDao = database.studentDao()
            val s1 = studentDao.insert(
                Student(
                    name = "Aarav Sharma",
                    guardianName = "Mukesh Sharma",
                    mobileNumber = "9876500001",
                    address = "Sector 4, Kota",
                    studentClass = "10th",
                    batch = "Morning Batch",
                    rollNumber = "101",
                    joiningDate = "2026-08-01",
                    monthlyFee = 1200.0,
                    avatarColorHex = "#2563EB"
                )
            )
            val s2 = studentDao.insert(
                Student(
                    name = "Priya Verma",
                    guardianName = "Rajesh Verma",
                    mobileNumber = "9876500002",
                    address = "Civil Lines, Kota",
                    studentClass = "12th",
                    batch = "Evening Batch",
                    rollNumber = "102",
                    joiningDate = "2026-08-05",
                    monthlyFee = 1500.0,
                    avatarColorHex = "#7C3AED"
                )
            )
            val s3 = studentDao.insert(
                Student(
                    name = "Rohan Gupta",
                    guardianName = "Anil Gupta",
                    mobileNumber = "9876500003",
                    address = "New Market, Kota",
                    studentClass = "10th",
                    batch = "Morning Batch",
                    rollNumber = "103",
                    joiningDate = "2026-08-10",
                    monthlyFee = 1200.0,
                    avatarColorHex = "#059669"
                )
            )
            val s4 = studentDao.insert(
                Student(
                    name = "Sneha Patel",
                    guardianName = "Dinesh Patel",
                    mobileNumber = "9876500004",
                    address = "Gandhi Nagar, Kota",
                    studentClass = "12th",
                    batch = "Evening Batch",
                    rollNumber = "104",
                    joiningDate = "2026-08-12",
                    monthlyFee = 1500.0,
                    avatarColorHex = "#D97706"
                )
            )

            // Seed sample fee payment for September
            val feeDao = database.feePaymentDao()
            feeDao.insert(
                FeePayment(
                    studentId = s1,
                    amountPaid = 1200.0,
                    paymentDate = "2026-09-02",
                    forMonthYear = "2026-09",
                    paymentMode = "UPI",
                    remarks = "Full September fee received"
                )
            )
            feeDao.insert(
                FeePayment(
                    studentId = s2,
                    amountPaid = 1000.0,
                    paymentDate = "2026-09-03",
                    forMonthYear = "2026-09",
                    paymentMode = "Cash",
                    remarks = "Partial payment (500 balance)"
                )
            )

            // Seed today's attendance (using local date 2026-09-03)
            val attendanceDao = database.attendanceDao()
            attendanceDao.insertOrUpdate(
                AttendanceRecord(
                    studentId = s1,
                    date = "2026-09-03",
                    status = "PRESENT"
                )
            )
            attendanceDao.insertOrUpdate(
                AttendanceRecord(
                    studentId = s2,
                    date = "2026-09-03",
                    status = "PRESENT"
                )
            )
            attendanceDao.insertOrUpdate(
                AttendanceRecord(
                    studentId = s3,
                    date = "2026-09-03",
                    status = "ABSENT"
                )
            )
            // s4 is left unmarked today to test one-tap marking!
        }
    }
}
