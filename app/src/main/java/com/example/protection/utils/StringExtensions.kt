package com.example.protection.utils

fun String.clean(): String {
    return this.trim().replace("\\s+".toRegex(), " ")
}