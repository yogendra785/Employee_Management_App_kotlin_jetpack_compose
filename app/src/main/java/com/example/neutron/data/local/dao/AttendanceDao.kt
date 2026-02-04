package com.example.neutron.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.neutron.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY id DESC")
    fun getAttendanceByDate(date: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceForEmployee(employeeId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>


    @Query("SELECT * FROM attendance WHERE employeeId = :empId")
    fun getAttendanceByEmployee(empId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendanceList(): List<AttendanceEntity>
}