package com.example.protection.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.protection.navigation.NavRoutes
import com.example.protection.viewmodel.auth.AuthState
import com.example.protection.viewmodel.auth.AuthViewModel

@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    // 🔹 Role Check
    val role = (authState as? AuthState.Authenticated)?.role ?: "EMPLOYEE"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp)
    ) {
        // Top Spacer for Status Bar
        Spacer(modifier = Modifier.height(52.dp))

        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (role == "ADMIN") "Admin Dashboard" else "Staff Portal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "24 Protection Service",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Logout Button
            IconButton(
                onClick = { authViewModel.logout() },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Grid Menu ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Common: Attendance (Full Width)
            item(span = { GridItemSpan(2) }) {
                DashboardCard(
                    title = "Attendance",
                    subtitle = if (role == "ADMIN") "Monitor daily staff presence" else "Mark your presence today",
                    icon = Icons.Default.Today,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { navController.navigate(NavRoutes.ATTENDANCE) }
                )
            }

            if (role == "ADMIN") {
                // --- ADMIN OPTIONS ---

                // 2. Employees (Add/Manage)
                item {
                    DashboardCard(
                        title = "Employees",
                        subtitle = "Add & Manage",
                        icon = Icons.Default.Groups,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { navController.navigate(NavRoutes.EMPLOYEE) }
                    )
                }

                // 3. 🆕 Manage Sites (Add/Delete Locations)
                item {
                    DashboardCard(
                        title = "Manage Sites",
                        subtitle = "Locations & Posts",
                        icon = Icons.Default.LocationCity, // City/Building Icon
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = { navController.navigate(NavRoutes.MANAGE_SITES) }
                    )
                }

                // 4. Leave Approvals
                item {
                    DashboardCard(
                        title = "Approvals",
                        subtitle = "Leave Requests",
                        icon = Icons.AutoMirrored.Filled.FactCheck,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { navController.navigate(NavRoutes.ADMIN_LEAVE_LIST) }
                    )
                }

                // 5. Payroll
                item {
                    DashboardCard(
                        title = "Payroll",
                        subtitle = "Salary Process",
                        icon = Icons.Default.Payments,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        onClick = { navController.navigate(NavRoutes.SALARY_MANAGEMENT) }
                    )
                }

            } else {
                // --- EMPLOYEE OPTIONS ---

                item {
                    DashboardCard(
                        title = "Request Leave",
                        subtitle = "Submit App",
                        icon = Icons.Default.PostAdd,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { navController.navigate(NavRoutes.LEAVE_REQUEST) }
                    )
                }
                item {
                    DashboardCard(
                        title = "Leave Status",
                        subtitle = "View History",
                        icon = Icons.Default.History,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = { navController.navigate(NavRoutes.MY_LEAVE_HISTORY) }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Text
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}