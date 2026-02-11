package com.example.neutron.data.local.dao

import androidx.room.*
import com.example.neutron.data.local.entity.LeaveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveEntity)

    // 🔹 FIXED: Changed parameter from Long to String to match Entity
    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId")
    fun getLeavesByEmployee(employeeId: String): Flow<List<LeaveEntity>>

    @Query("SELECT * FROM leave_requests")
    fun getAllLeaves(): Flow<List<LeaveEntity>>

    /**
     * 🔹 FIXED: Changed parameter from Long to String.
     * Updates the status of a specific leave request.
     */
    @Query("UPDATE leave_requests SET status = :status WHERE employeeId = :employeeId AND requestDate = :requestDate")
    suspend fun updateLeaveStatus(employeeId: String, requestDate: Long, status: String)

    @Delete
    suspend fun deleteLeave(leave: LeaveEntity)
}