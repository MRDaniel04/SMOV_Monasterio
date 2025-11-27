package com.nextapp.monasterio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.R
import com.nextapp.monasterio.viewModels.AuthViewModel
import com.nextapp.monasterio.ui.components.EditableText
import com.nextapp.monasterio.ui.components.EditableContent

@Composable
fun ProfileScreen(
    isEditing: Boolean,
    viewModel: AuthViewModel = viewModel(),
) {

    val userState by viewModel.currentUser.collectAsState()
    val user = userState
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // campos para iniciar sesion
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // campos editables de usuario - inicializar con los valores actuales
    var editableName by remember(user) { mutableStateOf(user?.name ?: "") }
    var editableEmail by remember(user) { mutableStateOf(user?.email ?: "") }

    // Detectar si hay cambios no guardados
    val hasChanges = remember(editableName, editableEmail, user) {
        editableName != (user?.name ?: "") || editableEmail != (user?.email ?: "")
    }

    // Función para cancelar cambios
    val cancelChanges = {
        editableName = user?.name ?: ""
        editableEmail = user?.email ?: ""
    }

    // Función para guardar cambios
    val saveChanges = {
        viewModel.updateUser(name = editableName, email = editableEmail)
    }

    if (isLoading) { // Vista de carga del estado
        // TODO mover a un componente para usar en otras paginas?
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cargando...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else if (user == null) { // Si el usuario no esta autenticado
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.profile_login), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.profile_email)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.profile_password)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { viewModel.login(email, password) }) {
                Text(stringResource(id = R.string.profile_login))
            }

            Spacer(modifier = Modifier.height(12.dp))

            error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        }
    } else { // Si el usuario esta autenticado
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isEditing) {
                // Usar EditableContent para gestionar el estado de edición
                EditableContent(
                    isEditing = isEditing,
                    hasChanges = hasChanges,
                    onSave = saveChanges,
                    onCancel = cancelChanges,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.title_profile),
                            style = MaterialTheme.typography.titleLarge
                        )

                        // Campo de nombre editable
                        Column {
                            Text(
                                text = stringResource(id = R.string.profile_name),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            EditableText(
                                text = editableName,
                                isEditing = isEditing,
                                onTextChange = { editableName = it },
                                label = stringResource(id = R.string.profile_name),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Campo de email editable
                        Column {
                            Text(
                                text = stringResource(id = R.string.profile_email),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            EditableText(
                                text = editableEmail,
                                isEditing = isEditing,
                                onTextChange = { editableEmail = it },
                                label = stringResource(id = R.string.profile_email),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                // Vista de solo lectura
                // Avatar / Icono de perfil
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.escudo),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = androidx.compose.ui.graphics.Color.Unspecified // Usar color original de la imagen
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Bienvenido, ${user.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tarjeta de información del usuario
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
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.outline_account_child_invert_24),
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
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.mail),
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

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_close_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.profile_logout))
                }
            }

            // Mostrar errores si los hay
            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
