package com.nextapp.monasterio.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.FiguraData
import kotlinx.coroutines.tasks.await

object FiguraRepository {
    suspend fun getAllFiguras(): List<FiguraData> {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("figuras").get().await()
        return snapshot.toObjects(FiguraData::class.java)
    }
}
