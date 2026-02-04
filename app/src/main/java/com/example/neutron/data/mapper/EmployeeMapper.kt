package com.example.neutron.data.mapper

import com.example.neutron.data.local.entity.EmployeeEntity
import com.example.neutron.domain.model.Employee

// Entity → Domain model
fun EmployeeEntity.toEmployee(): Employee {
    return Employee(
        id = id,
        firebaseUid = firebaseUid,
        name = name,
        email = email,
        role = role,
        department = department,
        salary = salary, // 🔹 Pass salary here
        isActive = isActive,
        createdAt = createdAt,
        password = password,
        imagePath = imagePath
    )
}

fun Employee.toEmployeeEntity(): EmployeeEntity {
    return EmployeeEntity(
        id = id,
        firebaseUid = firebaseUid,
        name = name,
        email = email,
        role = role,
        department = department,
        salary = salary, // 🔹 Pass salary here
        isActive = isActive,
        createdAt = createdAt,
        password = password,
        imagePath = imagePath
    )
}