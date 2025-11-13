package com.nextapp.monasterio.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // cargar datos desde Firestore
                viewModelScope.launch {
                    loadUserData(firebaseUser.uid)
                }
            } else {
                _currentUser.value = null
            }
        }
    }

    private suspend fun loadUserData(uid: String) {
        _isLoading.value = true
        _error.value = null
        try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                _currentUser.value = user
            } else {
                // Si no hay datos en Firestore, al menos devolvemos datos básicos de Auth
                val firebaseUser = auth.currentUser
                _currentUser.value = firebaseUser?.let {
                    User(
                        id = it.uid,
                        email = it.email ?: "",
                        name = it.displayName ?: "",
                        photoUrl = it.photoUrl?.toString()
                    )
                }
            }
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    // Funcion para iniciar sesion
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                // El listener de AuthState se encargará de cargar los datos
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Funcion para cerrar sesion
    fun logout() {
        auth.signOut()
    }

    // Funcion para actualizar los datos del usuario
    fun updateUser(name: String?, email: String?) {
        val current = auth.currentUser ?: return
        val uid = current.uid

        // Actualiza Firestore
        val updates = mutableMapOf<String, Any>()
        name?.let { updates["name"] = it }
        email?.let { updates["email"] = it }

        if (updates.isEmpty()) return

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                // Actualiza el flujo _currentUser localmente
                val updatedUser = _currentUser.value?.copy(
                    name = name ?: _currentUser.value?.name ?: "",
                    email = email ?: _currentUser.value?.email ?: ""
                )
                _currentUser.value = updatedUser
            }
            .addOnFailureListener { e ->
                _error.value = e.message
            }
    }
}
