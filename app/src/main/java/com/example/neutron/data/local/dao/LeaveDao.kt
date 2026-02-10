package com.example.neutron.data.local.dao

import androidx.room.*
import com.example.neutron.data.local.entity.LeaveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveEntity) // 🔹 Fixed: Repository calls this

    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId")
    fun getLeavesByEmployee(employeeId: Long): Flow<List<LeaveEntity>>

    @Query("SELECT * FROM leave_requests")
    fun getAllLeaves(): Flow<List<LeaveEntity>>

    /**
     * 🔹 Fixed: Repository calls this to update status in Room.
     * We use employeeId and requestDate to find the unique record.
     */
    @Query("UPDATE leave_requests SET status = :status WHERE employeeId = :employeeId AND requestDate = :requestDate")
    suspend fun updateLeaveStatus(employeeId: Long, requestDate: Long, status: String)

    @Delete
    suspend fun deleteLeave(leave: LeaveEntity)
}