// Archivo generado: EdicionFondoInicio.kt (versión corregida)
// NOTA: Ajustado según tus indicaciones

package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nextapp.monasterio.R
import com.nextapp.monasterio.data.ImagenRepository
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange

// COLORES
val EditModePurple = Color(0xFF9C27B0)
val RedTopBar = Color(0xFFC00000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdicionFondoTopBar() {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.title_inicio),
                    color = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = RedTopBar,
            scrolledContainerColor = RedTopBar,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        navigationIcon = {
            IconButton(onClick = { /* No hace nada */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu_24),
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = { /* No hace nada */ }) {
                Image(
                    painter = painterResource(id = R.drawable.espanya),
                    contentDescription = "Cambiar Idioma",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

@Composable
fun EdicionFondoInicio(navController: NavController) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val repo = ImagenRepository()
    var imagenFondoInicio by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val data = repo.getImagenFondoInicio()
        imagenFondoInicio = data?.url
    }


    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { }
    }

    Scaffold(
        topBar = { EdicionFondoTopBar() },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            val (background, crest, btnsContainer, actionControls) = createRefs()

            val backgroundModifier = Modifier.constrainAs(background) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }

            // ESTADO
            var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
            var currentSavedImageUrl by remember { mutableStateOf<String?>(null) }

            val backgroundUriToDisplay = selectedImageUri ?: currentSavedImageUrl?.let { Uri.parse(it) }

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                selectedImageUri = uri
            }

            // Mostrar fondo
            when (backgroundUriToDisplay) {
                is Uri -> {
                    AsyncImage(
                        model = backgroundUriToDisplay,
                        contentDescription = "Fondo personalizado",
                        contentScale = ContentScale.Crop,
                        modifier = backgroundModifier
                    )
                }

                else -> {
                    if (imagenFondoInicio != null) {
                        AsyncImage(
                            model = imagenFondoInicio,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = backgroundModifier
                        )
                    } else {
                        // fallback mientras carga o si Firestore falla
                        Image(
                            painter = painterResource(id = R.drawable.monastery_background),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = backgroundModifier
                        )
                    }
                }

            }

            // LOGO SUPERIOR
            Image(
                painter = painterResource(id = R.drawable.huelgas_inicio),
                contentDescription = "Escudo",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.constrainAs(crest) {
                    top.linkTo(parent.top, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )

            // SOLO EL BOTÓN MORADO "Cambiar fondo"
            if (selectedImageUri == null) Column(
                modifier = Modifier.constrainAs(btnsContainer) {
                    bottom.linkTo(parent.bottom, margin = 160.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    width = Dimension.fillToConstraints
                }
                ,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedImageUri == null) {
                            imagePickerLauncher.launch("image/*")
                        } else {
                            selectedImageUri = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EditModePurple),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.lapiz),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp).size(48.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            "Cambiar fondo",
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.width(64.dp).weight(1f))
                    }
                }
            }

            if (selectedImageUri != null) Column(
                modifier = Modifier.constrainAs(btnsContainer) {
                    bottom.linkTo(parent.bottom, margin = 160.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    width = Dimension.fillToConstraints
                },
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                // 🔶 VISITA VIRTUAL
                Button(
                    onClick = { /* inactivo */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MonasteryOrange),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier=Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    )  {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map_24),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(48.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            stringResource(id = R.string.virtual_visit),
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(64.dp).weight(1f))
                    }
                }

                // 🟩 MODO NIÑOS
                Button(
                    onClick = { /* inactivo */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6EB017)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier=Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    )  {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_account_child_invert_24),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(48.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            stringResource(id = R.string.child_mode),
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(64.dp).weight(1f))
                    }
                }

                // 🔵 RESERVA
                Button(
                    onClick = { /* inactivo */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier=Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_time_24),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(48.dp)

                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            stringResource(id = R.string.book_appointment),
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.width(64.dp).weight(1f))
                    }
                }
            }


            // BARRA DE ACCIÓN INFERIOR (Confirmar / Cancelar)
            AnimatedVisibility(
                visible = selectedImageUri != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.constrainAs(actionControls) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            ) {
                ActionControlBar(
                    onConfirm = {
                        currentSavedImageUrl = selectedImageUri.toString()
                        selectedImageUri = null
                        navController.popBackStack()
                    },
                    onCancel = {
                        selectedImageUri = null
                    }
                )
            }
        }
    }
}

// BOTONES INFERIORES RED / GREEN
@Composable
fun ActionControlBar(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 🔴 CANCELAR
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(50),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) {

            Spacer(Modifier.width(4.dp))
            Text("Cancelar", color = Color.White)
        }

        // 🟢 CONFIRMAR (MISMO TAMAÑO)
        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(50),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) {

            Spacer(Modifier.width(4.dp))
            Text("Confirmar", color = Color.White)
        }
    }
}
