package com.example.protection.navigation

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

    //manage sites

    const val MANAGE_SITES = "manage_sites"

    const val SITE_DETAIL = "site_detail/{siteId}"
    const val MY_LEAVE_HISTORY = "my_leave_history"

    // Dynamic Route
    const val EMPLOYEE_DETAIL = "employee_detail/{employeeId}"
    fun createDetailRoute(id: String) = "employee_detail/$id"

    const val KYC_FORM = "kyc_form/{employeeId}"
    fun createKycRoute(id: String) = "kyc_form/$id"
}