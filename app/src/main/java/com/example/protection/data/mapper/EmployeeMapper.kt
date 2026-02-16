package com.example.protection.data.mapper

import com.example.protection.data.local.entity.EmployeeEntity
import com.example.protection.domain.model.Employee

// 🔹 Entity (Database) → Domain model (UI/Logic)
fun EmployeeEntity.toEmployee(): Employee {
    return Employee(
        id = id,
        employeeId = employeeId,
        firebaseUid = firebaseUid,
        name = name,
        email = email,
        role = role,
        department = department,
        salary = salary,
        isActive = isActive,
        createdAt = createdAt,
        password = password,
        imagePath = imagePath,
        parentName = parentName,
        address = address,
        aadharNumber = aadharNumber,
        panNumber = panNumber,
        contactNumber = contactNumber,
        emergencyContact = emergencyContact
    )
}

// 🔹 Domain model (UI/Logic) → Entity (Database)
fun Employee.toEmployeeEntity(): EmployeeEntity {
    return EmployeeEntity(
        id = id,
        employeeId = employeeId,
        firebaseUid = firebaseUid,
        name = name,
        email = email,
        role = role,
        department = department,
        salary = salary,
        isActive = isActive,
        createdAt = createdAt,
        password = password,
        imagePath = imagePath,
        parentName = parentName,
        address = address,
        aadharNumber = aadharNumber,
        panNumber = panNumber,
        contactNumber = contactNumber,
        emergencyContact = emergencyContact
    )
}