package com.example.neutron.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // 🔹 Ensure this is the only Color import
import androidx.compose.ui.graphics.vector.ImageVector // 🔹 Correct type for Icons
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.neutron.domain.model.DashboardStats

@Composable
fun DashboardHeader(
    stats: DashboardStats,
    onSyncClick: () -> Unit // 🔹 Add this callback to make it functional
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // 1. Title and Sync Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Business Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Cloud Backup Button
            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Stats Cards Row (Properly aligned)
        Row(modifier = Modifier.fillMaxWidth()) {
            // Card 1: Payout
            StatCard(
                label = "Monthly Payout",
                value = "₹${stats.estimatedMonthlyPayout}",
                icon = Icons.Default.Payments,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Card 2: Attendance
            StatCard(
                label = "Attendance",
                value = "${stats.averageAttendanceRate.toInt()}%",
                icon = Icons.Default.TrendingUp,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}