package com.nextapp.monasterio.utils

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role

/**
 * Modificador personalizado que evita clics múltiples rápidos (debounce).
 * El tiempo de espera por defecto es de 500ms.
 */
fun Modifier.clickableSingle(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "clickableSingle"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }
) {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = {
            val currentTime = SystemClock.uptimeMillis()
            if (currentTime - lastClickTime > 500L) {
                lastClickTime = currentTime
                onClick()
            }
        }
    )
}

/**
 * Versión de clickableSingle que permite pasar interactionSource e indication.
 */
fun Modifier.clickableSingle(
    interactionSource: MutableInteractionSource,
    indication: androidx.compose.foundation.Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "clickableSingle"
        properties["interactionSource"] = interactionSource
        properties["indication"] = indication
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }
) {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = {
            val currentTime = SystemClock.uptimeMillis()
            if (currentTime - lastClickTime > 500L) {
                lastClickTime = currentTime
                onClick()
            }
        }
    )
}
