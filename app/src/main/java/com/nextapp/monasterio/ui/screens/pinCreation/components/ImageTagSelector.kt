package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.models.ImageTag

@Composable
fun ImageTagSelector(
    selectedTag: ImageTag?,
    onTagSelected: (ImageTag) -> Unit
) {
    // El diseño 2x2 (Column con dos Rows)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Primera Fila: Pintura y Escultura
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ImageTag.entries.take(2).forEach { tag ->
                // NOTA: Usamos .weight(1f) para repartir el espacio equitativamente
                TagChip(
                    tag = tag,
                    selectedTag = selectedTag,
                    onTagSelected = onTagSelected,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Segunda Fila: Arquitectura y Otro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ImageTag.entries.drop(2).forEach { tag ->
                TagChip(
                    tag = tag,
                    selectedTag = selectedTag,
                    onTagSelected = onTagSelected,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }
        }
    }
}

// IMPORTANTE: Definimos TagChip como una función de extensión de RowScope
@Composable
private fun RowScope.TagChip(
    tag: ImageTag,
    selectedTag: ImageTag?,
    onTagSelected: (ImageTag) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedTag == tag

    AssistChip(
        onClick = { onTagSelected(tag) },
        label = { Text(tag.displayName) },
        modifier = modifier,
        colors = if (isSelected)
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.colorScheme.onPrimary
            )
        else
            AssistChipDefaults.assistChipColors()
    )
}