package com.example.neutron.screens.leave

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.neutron.domain.model.Employee
import com.example.neutron.viewmodel.leave.LeaveViewModel

@Composable
fun MyLeaveHistoryScreen(
    viewModel: LeaveViewModel,
    currentUser: Employee?, // 🔹 Received from NavGraph
    onBack: () -> Unit
) {
    // 🔹 FIXED: Using real currentUser.id instead of placeholder 123
    val myLeaves by viewModel.getMyLeaveHistory(currentUser?.id ?: 0L).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 🔹 Uses the formatDate helper we defined for the Admin screen
                                Text(
                                    text = formatDate(request.startDate),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // Status badge with dynamic coloring
                                val badgeColor = when (request.status.uppercase()) {
                                    "APPROVED" -> Color(0xFF4CAF50) // Green
                                    "REJECTED" -> MaterialTheme.colorScheme.error // Red
                                    else -> MaterialTheme.colorScheme.primary // Blue/Pending
                                }

                                Surface(
                                    color = badgeColor,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = request.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reason: ${request.reason}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Requested on: ${formatDate(request.requestDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}