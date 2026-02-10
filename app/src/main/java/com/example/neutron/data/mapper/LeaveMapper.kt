package com.example.neutron.data.mapper

import com.example.neutron.data.local.entity.LeaveEntity
import com.example.neutron.domain.model.LeaveRequest

fun LeaveEntity.toDomain(): LeaveRequest {
    return LeaveRequest(
        id = id,
        employeeId = employeeId,
        employeeName = employeeName,
        startDate = startDate,
        endDate = endDate,
        reason = reason,
        status = status,
        requestDate = requestDate
    )
}

fun LeaveRequest.toEntity(): LeaveEntity {
    return LeaveEntity(
        id = id,
        employeeId = employeeId,
        employeeName = employeeName,
        startDate = startDate,
        endDate = endDate,
        reason = reason,
        status = status,
        requestDate = requestDate
    )
}