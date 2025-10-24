package com.example.smov_monasterio

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.nextapp.monasterio.ui.theme.Black
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange
import com.nextapp.monasterio.ui.theme.MonasteryRed
import com.nextapp.monasterio.ui.theme.Smov_monasterioTheme
import com.nextapp.monasterio.ui.theme.White
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplica el tema de tu app (definido en ui.theme/Theme.kt)
            Smov_monasterioTheme {
                MonasteryAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonasteryAppScreen() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // --- INICIO DEL CÓDIGO NUEVO DEL MENÚ ---
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight(), // Ocupa toda la altura
                drawerContainerColor = MonasteryRed, // Fondo rojo
                drawerContentColor = White // Color de texto/iconos
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 1. Icono superior para cerrar
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu_24), // Puedes cambiarlo por un icono de 'cerrar'
                            contentDescription = stringResource(id = R.string.navigation_drawer_close)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2. Lista de Opciones
                    DrawerMenuItem(
                        text = stringResource(id = R.string.title_inicio),
                        onClick = {
                            // Cierra el menú
                            scope.launch { drawerState.close() }
                            // No hace nada más porque ya estamos en Inicio
                        }
                    )
                    DrawerMenuItem(
                        text = stringResource(id = R.string.menu_info),
                        onClick = {
                            // TODO: Navegar a pantalla de Información
                            Toast.makeText(context, "Próximamente: Info", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DrawerMenuItem(
                        text = stringResource(id = R.string.menu_history),
                        onClick = {
                            // TODO: Navegar a pantalla de Historia
                            Toast.makeText(context, "Próximamente: Historia", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DrawerMenuItem(
                        text = stringResource(id = R.string.menu_gallery),
                        onClick = {
                            // TODO: Navegar a pantalla de Galería
                            Toast.makeText(context, "Próximamente: Galería", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DrawerMenuItem(
                        text = stringResource(id = R.string.menu_profile),
                        onClick = {
                            // TODO: Navegar a pantalla de Perfil
                            Toast.makeText(context, "Próximamente: Perfil", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DrawerMenuItem(
                        text = stringResource(id = R.string.menu_settings),
                        onClick = {
                            // TODO: Navegar a pantalla de Ajustes
                            Toast.makeText(context, "Próximamente: Ajustes", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            // --- FIN DEL CÓDIGO NUEVO DEL MENÚ ---
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.title_inicio)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MonasteryRed,
                        titleContentColor = White,
                        navigationIconContentColor = White,
                        actionIconContentColor = White
                    ),
                    navigationIcon = {
                        // Icono de hamburguesa
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu_24),
                                contentDescription = stringResource(id = R.string.navigation_drawer_open)
                            )
                        }
                    },
                    actions = {
                        // Icono de lápiz (edición)
                        IconButton(onClick = {
                            Toast.makeText(context, "Modo edición (próximamente)", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.lapiz),
                                contentDescription = stringResource(id = R.string.edit_mode)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            HomeScreenContent(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        // 1. Referencias para los elementos
        val (background, crest, title, btnVisit, btnBook) = createRefs()

        // 2. Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.monastery_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.constrainAs(background) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                height = androidx.constraintlayout.compose.Dimension.fillToConstraints
            }
        )

        // 3. Escudo
        Image(
            painter = painterResource(id = R.drawable.escudo),
            contentDescription = "Escudo",
            modifier = Modifier
                .size(80.dp)
                .constrainAs(crest) {
                    top.linkTo(parent.top, margin = 32.dp)
                    start.linkTo(parent.start, margin = 24.dp)
                }
        )

        // 4. Título
        Text(
            text = stringResource(id = R.string.monastery_name),
            color = White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
            style = LocalTextStyle.current.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Black,
                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            modifier = Modifier.constrainAs(title) {
                top.linkTo(crest.top)
                bottom.linkTo(crest.bottom)
                start.linkTo(crest.end, margin = 16.dp)
                end.linkTo(parent.end, margin = 24.dp)
                width = androidx.constraintlayout.compose.Dimension.fillToConstraints
            }
        )

        // 5. Botón Visita Virtual
        Button(
            onClick = {
                context.startActivity(Intent(context, VirtualVisitActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryOrange),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnVisit) {
                    bottom.linkTo(btnBook.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
                .height(60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_map_24),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(id = R.string.virtual_visit))
        }

        // 6. Botón Reserva Cita
        Button(
            onClick = {
                context.startActivity(Intent(context, AppointmentActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnBook) {
                    bottom.linkTo(parent.bottom, margin = 80.dp)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
                .height(60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_time_24),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(id = R.string.book_appointment))
        }
    }
}

@Composable
fun DrawerMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Hace que toda la fila sea clickeable
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_right), // El nuevo icono de triángulo
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = White,
            // fontSize = 18.sp, // Ya no es necesario si usas el estilo
            style = MaterialTheme.typography.bodyMedium
        )
    }
}