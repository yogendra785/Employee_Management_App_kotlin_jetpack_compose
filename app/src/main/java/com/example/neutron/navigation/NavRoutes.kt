package com.example.neutron.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val EMPLOYEE_LOGIN = "employee_login"

    const val MAIN_APP = "main_app"

    // Main Tabs
    const val DASHBOARD = "dashboard_screen"
    const val EMPLOYEE = "employee_screen"
    const val ATTENDANCE = "attendance"
    const val PROFILE = "profile_screen"

    // Admin Features
    const val ADD_EMPLOYEE = "add_employee"
    const val ADMIN_LEAVE_LIST = "admin_leave_list"
    const val SALARY_MANAGEMENT = "salary_management"

    // Employee Features
    const val LEAVE_REQUEST = "leave_requests"
    const val MY_LEAVE_HISTORY = "my_leave_history"

    // Dynamic Route
    const val EMPLOYEE_DETAIL = "employee_detail/{employeeId}"
    fun createDetailRoute(id: String) = "employee_detail/$id"
}