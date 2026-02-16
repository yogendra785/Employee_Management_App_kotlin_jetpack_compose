package com.example.protection.screens.employee

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.protection.domain.model.Attendance
import com.example.protection.domain.model.AttendanceStatus
import com.example.protection.utils.formatDate


@Composable
fun AttendanceHistoryItem(attendance: Attendance) {
    ListItem(
        headlineContent = { Text(formatDate(attendance.date)) },
        trailingContent = {
            Text(
                text = attendance.status.name,
                color = if (attendance.status == AttendanceStatus.PRESENT)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    )
}