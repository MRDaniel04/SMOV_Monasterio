package com.nextapp.monasterio.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfViewerScreen(
    pdfFileName: String = "Informe_Smov.pdf"
) {
    val context = LocalContext.current

    // ⭐ Zoom inicial
    val initialZoom = 1.2f
    var zoom by remember { mutableFloatStateOf(initialZoom) }

    // Copiar PDF a caché
    val tempFile = remember(pdfFileName) {
        try {
            val file = File(context.cacheDir, pdfFileName)
            context.assets.open(pdfFileName).use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) { null }
    }

    if (tempFile == null) {
        Text("Error al cargar PDF")
        return
    }

    val renderer = remember(tempFile) {
        val fd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(fd)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = zoom, scaleY = zoom)
                .background(Color.LightGray)
        ) {

            items(renderer.pageCount) { index ->

                val bitmap = remember(index) {
                    val page = renderer.openPage(index)
                    val bmp = Bitmap.createBitmap(
                        page.width * 2,
                        page.height * 2,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bmp
                }

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(2.dp, Color.Black)
                        .background(Color.White)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { zoom = (zoom + 0.2f).coerceAtMost(4f) }) {
                Text("+")
            }
            Button(onClick = {
                zoom = (zoom - 0.2f).coerceAtLeast(initialZoom)   // ⭐ NO BAJA DE LO NORMAL
            }) {
                Text("-")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { renderer.close() }
    }
}
