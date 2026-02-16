package com.example.protection.screens.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.protection.domain.model.AttendanceStatus

@Composable
fun AttendanceItem(
    name: String,
    status: AttendanceStatus?,
    isLocked: Boolean,
    isAdmin: Boolean,
    onPresent: () -> Unit,
    onAbsent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            // Present Button
            FilterChip(
                selected = status == AttendanceStatus.PRESENT,
                onClick = onPresent,
                enabled = !isLocked && isAdmin,
                label = { Text("Present") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Absent Button
            FilterChip(
                selected = status == AttendanceStatus.ABSENT,
                onClick = onAbsent,
                enabled = !isLocked && isAdmin,
                label = { Text("Absent") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}