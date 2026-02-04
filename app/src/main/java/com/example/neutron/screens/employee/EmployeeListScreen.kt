package com.example.neutron.screens.employee


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.neutron.domain.model.Employee
import com.example.neutron.navigation.NavRoutes
import com.example.neutron.screens.dashboard.DashboardHeader
import com.example.neutron.viewmodel.employee.EmployeeViewModel
import kotlinx.coroutines.launch
import com.example.neutron.domain.model.DashboardStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    viewModel: EmployeeViewModel,
    navigate: (String) -> Unit
) {
    // 🔹 Explicitly collect flows as state
    val employees by viewModel.employees.collectAsState(initial = emptyList())
    val stats by viewModel.dashboardStats.collectAsState(initial = DashboardStats())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Staff Members", fontWeight = FontWeight.Bold)
                        Text(
                            "${employees.size} personnel enrolled",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search logic */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navigate(NavRoutes.ADD_EMPLOYEE) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Employee") },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.surface
        ) {
            if (employees.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // 🔹 Dashboard Section
                    item {
                        DashboardHeader(stats = stats)

                        Text(
                            text = "Manage Staff",
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 🔹 Employee List Section
                    items(
                        items = employees,
                        key = { employee: Employee -> employee.id }
                    ) { employee: Employee ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            EmployeeCard(
                                employee = employee,
                                onToggleActive = { viewModel.toggleEmployeeStatus(employee) },
                                onDelete = {
                                    viewModel.deleteEmployee(employee)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "${employee.name} removed",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
                                        }
                                    }
                                },
                                onClick = {
                                    navigate(NavRoutes.createDetailRoute(employee.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No staff members yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (employee.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (employee.isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (employee.isActive) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, fontWeight = FontWeight.Bold)
                Text(employee.department, style = MaterialTheme.typography.bodySmall)

                // Status Badge
                Surface(
                    modifier = Modifier.padding(top = 4.dp),
                    shape = CircleShape,
                    color = if (employee.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (employee.isActive) "Active" else "Inactive",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (employee.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Switch(
                checked = employee.isActive,
                onCheckedChange = { onToggleActive() }
            )
        }
    }
}