package com.example.protection.utils

import java.text.SimpleDateFormat
import java.util.*

fun startOfDay(timeMillis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timeMillis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun formatDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}