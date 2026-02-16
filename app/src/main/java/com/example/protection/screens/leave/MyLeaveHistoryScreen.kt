package com.example.protection.screens.leave

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.protection.domain.model.Employee
import com.example.protection.viewmodel.leave.LeaveViewModel

@Composable
fun MyLeaveHistoryScreen(
    viewModel: LeaveViewModel,
    currentUser: Employee?,
    onBack: () -> Unit
) {
    val employeeId = currentUser?.employeeId ?: ""
    val myLeaves by viewModel.getMyLeaveHistory(employeeId).collectAsState(initial = emptyList())

    // 🔹 NEW: Auto-Sync when screen opens
    LaunchedEffect(employeeId) {
        if (employeeId.isNotBlank()) {
            viewModel.refreshLeaves(employeeId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("My Leave Status", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (myLeaves.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No leave history found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn {
                items(myLeaves) { request ->
                    LeaveStatusCard(request)
                }
            }
        }
    }
}

@Composable
fun LeaveStatusCard(request: com.example.protection.data.local.entity.LeaveEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatDate(request.startDate), style = MaterialTheme.typography.titleMedium)

                val badgeColor = when (request.status.uppercase()) {
                    "APPROVED" -> Color(0xFF4CAF50)
                    "REJECTED" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }

                Surface(color = badgeColor, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = request.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Reason: ${request.reason}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}