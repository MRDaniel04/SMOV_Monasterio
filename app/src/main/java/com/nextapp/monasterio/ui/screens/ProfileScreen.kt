package com.nextapp.monasterio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.viewModels.AuthViewModel

@Composable
fun ProfileScreen(viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val userState by viewModel.currentUser.collectAsState()
    val user = userState
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            Text(text = "Iniciar sesión", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { viewModel.login(email, password) }) {
                Text("Iniciar sesión")
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
            Text(
                text = "Bienvenido, ${user.name}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { viewModel.logout() }) {
                Text("Cerrar sesión")
            }
        }
    }
}
