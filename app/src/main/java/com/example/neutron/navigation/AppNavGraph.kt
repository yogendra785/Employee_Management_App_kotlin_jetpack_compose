package com.example.neutron.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.neutron.screens.attendance.AttendanceScreen
import com.example.neutron.screens.dashboard.DashboardScreen
import com.example.neutron.screens.employee.AddEmployeeScreen
import com.example.neutron.screens.employee.EmployeeDetailScreen
import com.example.neutron.screens.employee.EmployeeListScreen
import com.example.neutron.screens.leave.AdminLeaveListScreen
import com.example.neutron.screens.leave.LeaveRequestScreen
import com.example.neutron.screens.leave.MyLeaveHistoryScreen
import com.example.neutron.screens.profile.ProfileScreen
import com.example.neutron.screens.salary.SalaryManagementScreen
import com.example.neutron.viewmodel.attendance.AttendanceViewModel
import com.example.neutron.viewmodel.auth.AuthViewModel
import com.example.neutron.viewmodel.employee.AddEmployeeViewModel
import com.example.neutron.viewmodel.employee.EmployeeViewModel
import com.example.neutron.viewmodel.leave.LeaveViewModel

fun NavGraphBuilder.appNavGraph(
    appNavController: NavHostController,
    rootNavController: NavHostController,
    authViewModel: AuthViewModel,
    userRole: String
) {
    // 1. DASHBOARD
    composable(NavRoutes.DASHBOARD) {
        DashboardScreen(
            navController = appNavController, // Passes nav controller to handle grid clicks
            authViewModel = authViewModel
        )
    }

    // 2. ADMIN ROUTES
    if (userRole == "ADMIN") {
        composable(NavRoutes.ADD_EMPLOYEE) {
            val vm: AddEmployeeViewModel = hiltViewModel()
            AddEmployeeScreen(
                viewModel = vm,
                onBack = { appNavController.popBackStack() }
            )
        }

        composable(NavRoutes.ADMIN_LEAVE_LIST) {
            val leaveVM: LeaveViewModel = hiltViewModel()
            AdminLeaveListScreen(
                viewModel = leaveVM,
                onBack = { appNavController.popBackStack() }
            )
        }

        composable(NavRoutes.SALARY_MANAGEMENT) {
            SalaryManagementScreen(
                onBackClick = { appNavController.popBackStack() }
            )
        }

        composable(NavRoutes.EMPLOYEE) {
            val vm: EmployeeViewModel = hiltViewModel()
            EmployeeListScreen(
                viewModel = vm,
                navigate = { route -> appNavController.navigate(route) }
            )
        }
    }

    // 3. SHARED / EMPLOYEE ROUTES
    composable(NavRoutes.ATTENDANCE) {
        val eVM: EmployeeViewModel = hiltViewModel()
        val aVM: AttendanceViewModel = hiltViewModel()
        AttendanceScreen(
            employeeViewModel = eVM,
            attendanceViewModel = aVM,
            authViewModel = authViewModel,
            userRole = userRole,
            onBackClick = { appNavController.popBackStack() }
        )
    }

    composable(NavRoutes.LEAVE_REQUEST) {
        val leaveVM: LeaveViewModel = hiltViewModel()
        val currentUser by authViewModel.currentUser.collectAsState()
        LeaveRequestScreen(
            viewModel = leaveVM,
            currentUser = currentUser,
            onBack = { appNavController.popBackStack() }
        )
    }

    composable(NavRoutes.MY_LEAVE_HISTORY) {
        val leaveVM: LeaveViewModel = hiltViewModel()
        val currentUser by authViewModel.currentUser.collectAsState()
        MyLeaveHistoryScreen(
            viewModel = leaveVM,
            currentUser = currentUser,
            onBack = { appNavController.popBackStack() }
        )
    }

    // 4. COMMON ROUTES
    composable(
        route = NavRoutes.EMPLOYEE_DETAIL,
        arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
    ) { backStackEntry ->
        val empId = backStackEntry.arguments?.getString("employeeId") ?: ""
        val eVM: EmployeeViewModel = hiltViewModel()
        val aVM: AttendanceViewModel = hiltViewModel()

        EmployeeDetailScreen(
            employeeId = empId,
            employeeViewModel = eVM,
            attendanceViewModel = aVM,
            onBack = { appNavController.popBackStack() }
        )
    }

    composable(NavRoutes.PROFILE) {
        ProfileScreen(
            navController = appNavController,
            rootNavController = rootNavController,
            authViewModel = authViewModel,
            onLogout = {
                authViewModel.logout()
                rootNavController.navigate(NavRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}