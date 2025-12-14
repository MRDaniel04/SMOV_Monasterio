package com.nextapp.monasterio.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.R
import java.io.File
import java.io.FileOutputStream

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource


@Composable
fun GenericPdfViewer(
    pdfFileName: String,
    initialZoom: Float? = null,
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val zoom = initialZoom ?: if (isTablet) 1.1f else 1.35f
    val verticalPadding = if (isTablet) 100.dp else 140.dp
    val horizontalPadding = if (isTablet) 40.dp else 10.dp

    // ... (Carga de archivo y Renderer igual que antes) ...
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(id = R.string.error_loading_pdf))
        }
        return
    }

    val renderer = remember(tempFile) {
        val fd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(fd)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = verticalPadding, bottom = verticalPadding,
                start = horizontalPadding, end = horizontalPadding
            ),
            modifier = Modifier.fillMaxSize().background(Color.LightGray)
                .graphicsLayer(scaleX = zoom, scaleY = zoom)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.size > 1) event.changes.forEach { it.consume() }
                        }
                    }
                }
        ) {
            items(renderer.pageCount) { index ->
                val bitmap = remember(index) {
                    try {
                        val page = renderer.openPage(index)
                        val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bmp
                    } catch (e: Exception) { null }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                            .border(2.dp, Color.Black).background(Color.White)
                    )
                }
            }
        }

        onBackClick?.let { action ->
            Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp).background(Color(0xBB000000))) {
                IconButton(onClick = { action() }, modifier = Modifier.padding(4.dp)) {
                    Icon(painterResource(id = R.drawable.arrow_back), contentDescription = stringResource(id = R.string.back_button), tint = Color.White)
                }
            }
        }
    }

    DisposableEffect(Unit) { onDispose { renderer.close() } }
}