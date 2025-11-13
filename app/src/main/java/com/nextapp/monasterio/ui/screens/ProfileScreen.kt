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

    // campos editables de usuario
    var name by remember { mutableStateOf(user?.name ?: "") }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isEditing) {
                EditableText(
                    text = name,
                    isEditing = isEditing,
                    onTextChange = { name = it },
                    label = "Nombre",
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        viewModel.updateUser(name = name, email = user.email) // O también email editable si quieres
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(id = R.string.edit_save_changes))
                }
            } else {
                Text(
                    text = "Bienvenido, ${user.name}",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { viewModel.logout() }) {
                    Text(stringResource(id = R.string.profile_logout))
                }
            }
        }
    }
}
