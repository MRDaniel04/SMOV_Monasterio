package com.nextapp.monasterio.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.R

import com.nextapp.monasterio.ui.components.EditableText
import com.nextapp.monasterio.viewModels.AuthViewModel
import com.nextapp.monasterio.ui.components.MonasteryButton
@Composable
fun ProfileScreen(
    isEditing: Boolean,
    isDiscarding: Boolean = false,
    onKeepEditing: () -> Unit = {},
    viewModel: AuthViewModel = viewModel(),
) {
    val userState by viewModel.currentUser.collectAsState()
    val user = userState
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Detectar orientación
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (user == null) {
        // --- PANTALLA DE LOGIN ---
        if (isLandscape) {
            LoginScreenLandscape(viewModel, error)
        } else {
            LoginScreenPortrait(viewModel, error)
        }
    } else {
        // --- PANTALLA DE PERFIL (AUTENTICADO) ---
        // Lógica de edición (común para ambas orientaciones)
        var editableName by remember(user) { mutableStateOf(user.name ?: "") }
        var editableEmail by remember(user) { mutableStateOf(user.email ?: "") }

        val hasChanges = remember(editableName, editableEmail, user) {
            editableName != (user.name ?: "") || editableEmail != (user.email ?: "")
        }

        val saveChanges = { viewModel.updateUser(name = editableName, email = editableEmail) }
        val cancelChanges = {
            editableName = user.name ?: ""
            editableEmail = user.email ?: ""
        }

        // Manejador reutilizable para guardar cambios al salir de modo edición
        val isSaving = com.nextapp.monasterio.ui.components.EditModeHandler(
            isEditing = isEditing,
            hasChanges = hasChanges,
            isDiscarding = isDiscarding,
            onSave = saveChanges,
            onDiscard = cancelChanges,
            onKeepEditing = onKeepEditing
        )

        if (isEditing || isSaving) {
            // MODO EDICIÓN (Lo mantenemos simple en vertical con scroll para no complicar)
            EditableProfileView(
                isEditing = true,
                hasChanges = hasChanges,
                onSave = saveChanges,
                onCancel = cancelChanges,
                name = editableName,
                email = editableEmail,
                onNameChange = { editableName = it },
                onEmailChange = { editableEmail = it }
            )
        } else {
            // MODO LECTURA (Aquí aplicamos el cambio de diseño radical)
            if (isLandscape) {
                ProfileViewLandscape(user, viewModel)
            } else {
                ProfileViewPortrait(user, viewModel)
            }
        }
    }
}

@Composable
fun LoginScreenPortrait(viewModel: AuthViewModel, error: String?) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.profile_login), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        LoginForm(email, { email = it }, password, { password = it })

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.profile_login))
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun LoginScreenLandscape(viewModel: AuthViewModel, error: String?) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Un poco más de margen para que respire
        verticalAlignment = Alignment.CenterVertically, // Centrado vertical general
        horizontalArrangement = Arrangement.spacedBy(32.dp) // Separación entre la zona de escribir y la de pulsar
    ) {
        // --- IZQUIERDA: FORMULARIO (Campos de texto) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), // Para centrar verticalmente el contenido
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.profile_login),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Campos de Email y Contraseña
            LoginForm(email, { email = it }, password, { password = it })
        }

        // --- DERECHA: BOTÓN DE ACCIÓN Y ERRORES ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón grande y llamativo
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp) // Bordes redondeados
            ) {
                Text(
                    text = stringResource(id = R.string.profile_login),
                    modifier = Modifier.padding(vertical = 8.dp) // Más alto para facilitar el toque
                )
            }

            // Mensaje de error debajo del botón
            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LoginForm(email: String, onEmailChange: (String) -> Unit, pass: String, onPassChange: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(id = R.string.profile_email)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = pass,
        onValueChange = onPassChange,
        label = { Text(stringResource(id = R.string.profile_password)) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ProfileViewPortrait(user: com.nextapp.monasterio.models.User, viewModel: AuthViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // Scroll por si acaso
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.escudo),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.5f),
            tint = androidx.compose.ui.graphics.Color.Unspecified
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bienvenido, ${user.name}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))

        UserInfoCard(user)

        Spacer(modifier = Modifier.height(48.dp))

        LogoutButton(viewModel)
    }
}

@Composable
fun ProfileViewLandscape(user: com.nextapp.monasterio.models.User, viewModel: AuthViewModel) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.escudo),
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(0.8f), // Un poco más grande en tablet
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
        }
        Box(
            modifier = Modifier
                .weight(0.4f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            UserInfoCard(user)
        }
        Box(
            modifier = Modifier.weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            LogoutButton(viewModel)
        }
    }
}

@Composable
fun UserInfoCard(user: com.nextapp.monasterio.models.User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Nombre
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_account_child_invert_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.profile_name),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.name ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            // Email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.mail),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.profile_email),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.email ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun LogoutButton(viewModel: AuthViewModel) {
    MonasteryButton(
        onClick = { viewModel.logout() },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.widthIn(min = 200.dp) // Ancho mínimo para que se vea bien
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_close_24),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(id = R.string.profile_logout))
    }
}

@Composable
fun EditableProfileView(
    isEditing: Boolean,
    hasChanges: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    name: String,
    email: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.title_profile),
                style = MaterialTheme.typography.titleLarge
            )
            // Campo Nombre
            Column {
                Text(
                    text = stringResource(id = R.string.profile_name),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                EditableText(
                    text = name,
                    isEditing = isEditing,
                    onTextChange = onNameChange,
                    label = stringResource(id = R.string.profile_name),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
