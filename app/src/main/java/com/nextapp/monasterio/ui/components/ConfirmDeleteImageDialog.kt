package com.nextapp.monasterio.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nextapp.monasterio.R

/**
 * Diálogo de confirmación para eliminar una imagen
 */
@Composable
fun ConfirmDeleteImageDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = stringResource(id = R.string.image_delete_confirm_title),
    message: String = stringResource(id = R.string.image_delete_confirm_message)
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(id = R.string.image_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.confirm_cancel_button))
                }
            }
        )
    }
}
