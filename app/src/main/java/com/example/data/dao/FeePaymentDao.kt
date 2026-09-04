package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FeePayment
import kotlinx.coroutines.flow.Flow

@Dao
interface FeePaymentDao {
    @Query("SELECT * FROM fee_payments ORDER BY paymentDate DESC, id DESC")
    fun getAllPayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE forMonthYear = :monthYear ORDER BY paymentDate DESC")
    fun getPaymentsForMonth(monthYear: String): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: FeePayment): Long

    @Update
    suspend fun update(payment: FeePayment)

    @Delete
    suspend fun delete(payment: FeePayment)

    @Query("DELETE FROM fee_payments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM fee_payments WHERE studentId = :studentId")
    suspend fun deleteByStudent(studentId: Long)
}
