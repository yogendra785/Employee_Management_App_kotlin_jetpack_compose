package com.example.neutron.screens.leave

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.neutron.data.local.entity.LeaveEntity
import com.example.neutron.viewmodel.leave.LeaveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLeaveListScreen(
    viewModel: LeaveViewModel,
    onBack: () -> Unit
) {
    // Observing leaves from the database
    val allLeaves by viewModel.allLeaves.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Approvals") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            val pendingRequests = allLeaves.filter { it.status == "PENDING" }

            if (pendingRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending leave requests", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(pendingRequests) { leave ->
                        AdminLeaveItem(
                            leave = leave,
                            onApprove = { viewModel.updateLeaveStatus(leave, "APPROVED") },
                            onReject = { viewModel.updateLeaveStatus(leave, "REJECTED") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLeaveItem(
    leave: LeaveEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = leave.employeeName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                SuggestionChip(
                    onClick = { },
                    label = { Text(leave.status) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Reason:", style = MaterialTheme.typography.labelLarge)
            Text(leave.reason, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 This is where formatDate is called
            val dateText = "${formatDate(leave.startDate)} - ${formatDate(leave.endDate)}"
            Text(text = "Duration: $dateText", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onReject, modifier = Modifier.padding(end = 8.dp)) {
                    Text("Reject")
                }
                Button(onClick = onApprove) {
                    Text("Approve")
                }
            }
        }
    }
}

// 🔹 THE FIX: Place this function here, outside of the @Composable functions
fun formatDate(millis: Long): String {
    return if (millis > 0) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(millis))
    } else {
        "N/A"
    }
}