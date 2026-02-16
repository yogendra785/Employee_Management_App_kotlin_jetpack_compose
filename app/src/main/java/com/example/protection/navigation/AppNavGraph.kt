package com.example.protection.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.protection.screens.auth.LoginScreen
import com.example.protection.screens.attendance.AttendanceScreen
import com.example.protection.screens.dashboard.DashboardScreen
import com.example.protection.screens.employee.AddEmployeeScreen
import com.example.protection.screens.employee.EmployeeDetailScreen
import com.example.protection.screens.employee.EmployeeListScreen
import com.example.protection.screens.kyc.KycFormScreen
import com.example.protection.screens.leave.AdminLeaveListScreen
import com.example.protection.screens.leave.LeaveRequestScreen
import com.example.protection.screens.leave.MyLeaveHistoryScreen
import com.example.protection.screens.profile.ProfileScreen
import com.example.protection.screens.salary.SalaryManagementScreen
// 🔹 Add these new imports
import com.example.protection.screens.admin.SiteListScreen
import com.example.protection.screens.admin.SiteDetailScreen
import com.example.protection.viewmodel.attendance.AttendanceViewModel
import com.example.protection.viewmodel.auth.AuthViewModel
import com.example.protection.viewmodel.employee.AddEmployeeViewModel
import com.example.protection.viewmodel.employee.EmployeeViewModel
import com.example.protection.viewmodel.leave.LeaveViewModel
import com.example.protection.viewmodel.site.SiteDetailViewModel // Import if needed explicitly

fun NavGraphBuilder.appNavGraph(
    appNavController: NavHostController,
    rootNavController: NavHostController,
    authViewModel: AuthViewModel,
    userRole: String
) {
    // 0. LOGIN ROUTE
    composable(NavRoutes.LOGIN) {
        LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = {
                rootNavController.navigate(NavRoutes.DASHBOARD) {
                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                }
            }
        )
    }

    // 1. DASHBOARD
    composable(NavRoutes.DASHBOARD) {
        DashboardScreen(
            navController = appNavController,
            authViewModel = authViewModel
        )
    }

    // 2. ADMIN ROUTES
    if (userRole == "ADMIN") {
        composable(NavRoutes.ADD_EMPLOYEE) {
            val vm: AddEmployeeViewModel = hiltViewModel()
            AddEmployeeScreen(viewModel = vm, onBack = { appNavController.popBackStack() })
        }
        composable(NavRoutes.ADMIN_LEAVE_LIST) {
            val leaveVM: LeaveViewModel = hiltViewModel()
            AdminLeaveListScreen(viewModel = leaveVM, onBack = { appNavController.popBackStack() })
        }
        composable(NavRoutes.SALARY_MANAGEMENT) {
            SalaryManagementScreen(onBackClick = { appNavController.popBackStack() })
        }
        composable(NavRoutes.EMPLOYEE) {
            val vm: EmployeeViewModel = hiltViewModel()
            EmployeeListScreen(viewModel = vm, navigate = { route -> appNavController.navigate(route) })
        }

        // 🔹 FIX: MOVED MANAGE SITES HERE (So Dashboard can find it)
        composable(NavRoutes.MANAGE_SITES) {
            // Note: SiteViewModel is auto-injected by hiltViewModel() inside the screen
            SiteListScreen(
                onBack = { appNavController.popBackStack() },
                onSiteClick = { siteId ->
                    appNavController.navigate("site_detail/$siteId")
                }
            )
        }

        // 🔹 FIX: ADDED SITE DETAIL HERE
        composable(
            route = "site_detail/{siteId}", // Hardcoded string or define in NavRoutes
            arguments = listOf(navArgument("siteId") { type = NavType.StringType })
        ) {
            // Note: SiteDetailViewModel is auto-injected
            SiteDetailScreen(
                onBack = { appNavController.popBackStack() }
            )
        }
    }

    // 3. SHARED ROUTES
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

    // ... (Leaves, History, etc.) ...
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
            onBack = { appNavController.popBackStack() },
            onGenerateKyc = { id -> appNavController.navigate(NavRoutes.createKycRoute(id)) }
        )
    }

    composable(
        route = NavRoutes.KYC_FORM,
        arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
    ) { backStackEntry ->
        val empId = backStackEntry.arguments?.getString("employeeId") ?: ""
        val eVM: EmployeeViewModel = hiltViewModel()
        KycFormScreen(
            employeeId = empId,
            onBack = { appNavController.popBackStack() },
            employeeViewModel = eVM
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