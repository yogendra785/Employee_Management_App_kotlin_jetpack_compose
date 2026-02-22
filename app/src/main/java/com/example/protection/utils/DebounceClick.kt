package com.example.protection.utils

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * A click modifier that prevents double-taps.
 * It disables the click action for [debounceTime] milliseconds after a click.
 */
fun Modifier.clickableDebounce(
    debounceTime: Long = 1000L,
    onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    this.clickable(
        indication = androidx.compose.material.ripple.rememberRipple(), // Visual ripple effect
        interactionSource = remember { MutableInteractionSource() }
    ) {
        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            onClick()
        }
    }
}