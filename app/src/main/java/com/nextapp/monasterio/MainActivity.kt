package com.nextapp.monasterio

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// --- 1. Definimos las "rutas" para nuestra navegación ---
object AppRoutes {
    const val INICIO = "inicio"
    const val INFO = "info"
    const val HISTORIA = "historia"
    const val GALERIA = "galeria"
    const val PERFIL = "perfil"
    const val AJUSTES = "ajustes"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- 2. Estado para guardar el título actual ---
    val currentTitle = remember { mutableStateOf(context.getString(R.string.title_inicio)) }

    // --- 3. Escuchamos los cambios de navegación para actualizar el título ---
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route
            currentTitle.value = when (route) {
                AppRoutes.INFO -> context.getString(R.string.title_info_general)
                AppRoutes.HISTORIA -> context.getString(R.string.title_history)
                AppRoutes.GALERIA -> context.getString(R.string.title_gallery)
                AppRoutes.PERFIL -> context.getString(R.string.title_profile)
                AppRoutes.AJUSTES -> context.getString(R.string.title_settings)
                else -> context.getString(R.string.title_inicio)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // --- 4. Pasamos el navegador y el scope al contenido del menú ---
            AppDrawerContent(
                navController = navController,
                scope = scope,
                drawerState = drawerState
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    // --- 5. El título ahora es dinámico ---
                    title = { Text(currentTitle.value) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MonasteryRed,
                        titleContentColor = White,
                        navigationIconContentColor = White,
                        actionIconContentColor = White
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            if (drawerState.isOpen) {
                                Icon(
                                    painter = painterResource(id = R.drawable.menu_close), // <-- Icono 'X' (Cerrar)
                                    contentDescription = stringResource(id = R.string.navigation_drawer_close)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_menu_24), // <-- Icono Hamburguesa (Abrir)
                                    contentDescription = stringResource(id = R.string.navigation_drawer_open)
                                )
                            }
                            // --- FIN DEL CAMBIO ---

                        }
                    },
                    actions = {
                        // El lápiz de edición se queda
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
            // --- 6. Aquí está el "marco de fotos" (NavHost) ---
            AppNavigationHost(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

// --- 7. El Host de Navegación (El "marco") ---
@Composable
fun AppNavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.INICIO, // Empezamos en "inicio"
        modifier = modifier
    ) {
        // Cada "composable" es una "foto" para el marco
        composable(AppRoutes.INICIO) {
            HomeScreenContent() // La pantalla de inicio que ya tenías
        }
        composable(AppRoutes.INFO) {
            InfoScreen() // Nueva pantalla
        }
        composable(AppRoutes.HISTORIA) {
            HistoriaScreen() // Nueva pantalla
        }
        composable(AppRoutes.GALERIA) {
            GaleriaScreen() // Nueva pantalla
        }
        composable(AppRoutes.PERFIL) {
            PerfilScreen() // Nueva pantalla
        }
        composable(AppRoutes.AJUSTES) {
            AjustesScreen() // Nueva pantalla
        }
    }
}

// --- 8. El contenido del menú lateral (actualizado) ---
@Composable
fun AppDrawerContent(
    navController: NavHostController,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    // Función helper para navegar y cerrar el menú
    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            // Esto evita que se acumulen pantallas en el historial
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
        // Cierra el menú
        scope.launch { drawerState.close() }
    }

    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MonasteryRed,
        drawerContentColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(onClick = { scope.launch { drawerState.close() } }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu_24),
                    contentDescription = stringResource(id = R.string.navigation_drawer_close)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Usamos la función helper
            DrawerMenuItem(
                text = stringResource(id = R.string.title_inicio),
                onClick = { navigateTo(AppRoutes.INICIO) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_info),
                onClick = { navigateTo(AppRoutes.INFO) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_history),
                onClick = { navigateTo(AppRoutes.HISTORIA) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_gallery),
                onClick = { navigateTo(AppRoutes.GALERIA) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_profile),
                onClick = { navigateTo(AppRoutes.PERFIL) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_settings),
                onClick = { navigateTo(AppRoutes.AJUSTES) }
            )
        }
    }
}


// --- 9. Las funciones que ya tenías (no cambian) ---

// Esta es la plantilla para cada fila del menú
@Composable
fun DrawerMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = White,
            style = MaterialTheme.typography.bodyMedium // Usa el estilo del tema
        )
    }
}

// Esta es tu pantalla de inicio
@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (background, crest, title, btnVisit, btnBook) = createRefs()

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


// --- 10. Contenido de las nuevas pantallas (Marcadores de posición) ---
// Puedes mover estas funciones a sus propios archivos .kt si quieres organizar mejor

@Composable
fun InfoScreen() {
    // Columna principal que ocupa toda la pantalla
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // --- 1. CONTENIDO PRINCIPAL (SCROLLABLE) ---
        // Esta columna ocupa todo el espacio MENOS el recuadro de abajo
        Column(
            modifier = Modifier
                .weight(1f) // Esto empuja el Card de abajo al fondo
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Permite scroll si el contenido es largo
        ) {
            // Aquí puedes empezar a añadir el contenido que quieras
            Text(
                text = "Información General",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aquí irá todo el texto y las imágenes sobre la información general del monasterio. " +
                        "Este contenido puede ser muy largo y el usuario podrá hacer scroll, " +
                        "pero el recuadro de datos de abajo se quedará siempre fijo.",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // --- 2. RECUADRO ROJO DE INFORMACIÓN ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MonasteryRed // El color rojo de tu tema
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Título del recuadro
                Text(
                    text = stringResource(id = R.string.general_info),
                    style = MaterialTheme.typography.titleLarge,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Fila de Localización
                InfoRow(
                    iconResId = R.drawable.location,
                    text = stringResource(id = R.string.info_location)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fila de Horarios
                InfoRow(
                    iconResId = R.drawable.ic_time_24, // Reusamos el icono del reloj
                    text = stringResource(id = R.string.info_hours)
                )
            }
        }
    }
}

@Composable
fun HistoriaScreen() {
    // Usamos una Columna con scroll por si el contenido es muy largo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Permite hacer scroll
            .padding(16.dp)
    ) {
        // Aquí creamos cada uno de los desplegables
        ExpandableHistoryCard(
            title = stringResource(id = R.string.history_1800),
            content = stringResource(id = R.string.history_content_placeholder)
        )

        ExpandableHistoryCard(
            title = stringResource(id = R.string.history_1900),
            content = stringResource(id = R.string.history_content_placeholder)
        )

        ExpandableHistoryCard(
            title = stringResource(id = R.string.history_1950),
            content = stringResource(id = R.string.history_content_placeholder)
        )

        ExpandableHistoryCard(
            title = stringResource(id = R.string.history_actualidad),
            content = stringResource(id = R.string.history_content_placeholder)
        )
    }
}

@Composable
fun GaleriaScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Contenido de Galería", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun PerfilScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Contenido de Perfil", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun AjustesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Contenido de Ajustes", style = MaterialTheme.typography.headlineMedium)
    }
}

/**
 * Una fila de icono + texto para el recuadro de información.
 */
@Composable
fun InfoRow(
    iconResId: Int,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = White, // Icono en color blanco
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = White, // Texto en color blanco
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Un Card de historial expandible que muestra/oculta contenido.
 * @param title El texto del título (ej: "1800").
 * @param content El texto a mostrar cuando se expande.
 */
@Composable
fun ExpandableHistoryCard(
    title: String,
    content: String
) {
    // 1. Estado para saber si el card está expandido o no
    var expanded by remember { mutableStateOf(false) }

    // 2. Estado para animar la rotación de la flecha
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 3. La fila superior (título + flecha) que es clickeable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded } // Cambia el estado al hacer clic
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible
            )
            Icon(
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = "Expandir/Colapsar",
                modifier = Modifier.rotate(rotationAngle) // Gira la flecha
            )
        }

        // Línea divisoria
        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        // 4. El contenido que aparece y desaparece con animación
        AnimatedVisibility(visible = expanded) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }
    }
}