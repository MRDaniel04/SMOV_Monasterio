package com.nextapp.monasterio.ui.screens.pinEdition

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nextapp.monasterio.R
import java.io.File
import java.io.FileOutputStream

@Composable
fun ManualEdicionScreen(navController: NavHostController) {

    val context = LocalContext.current
    val pdfFileName = "Informe_Smov.pdf"


    var scale by remember { mutableFloatStateOf(1.1f) }

    val minZoom = 1.0f
    val maxZoom = 4.0f

    val tempFile = remember(pdfFileName) {
        try {
            val file = File(context.cacheDir, pdfFileName)
            context.assets.open(pdfFileName).use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    if (tempFile == null) {
        Text("Error al cargar el manual de edición")
        return
    }

    val renderer = remember(tempFile) {
        val fd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(fd)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(minZoom, maxZoom)
                    }
                }
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
            ) {
                items(renderer.pageCount) { index ->

                    val bitmap = remember(index) {
                        try {
                            val page = renderer.openPage(index)
                            val bmp = Bitmap.createBitmap(
                                page.width * 2,
                                page.height * 2,
                                Bitmap.Config.ARGB_8888
                            )
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()
                            bmp
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
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
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color(0xBB000000))
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { renderer.close() }
    }
}
