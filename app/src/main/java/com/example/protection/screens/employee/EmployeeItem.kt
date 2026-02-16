package com.example.protection.screens.employee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.protection.domain.model.Employee

@Composable
fun EmployeeItem(
    employee: Employee,
    onToggleActive: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = employee.name, style = MaterialTheme.typography.titleMedium)
                Text(text = employee.department, style = MaterialTheme.typography.bodySmall)
            }

            TextButton(onClick = onToggleActive) {
                Text(
                    text = if (employee.isActive) "Active" else "Inactive",
                    color = if (employee.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    textDecoration = if (!employee.isActive) TextDecoration.LineThrough else TextDecoration.None
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}