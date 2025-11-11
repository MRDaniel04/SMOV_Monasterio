package com.nextapp.monasterio.models

data class PlanoData(
    val id: String = "",
    val nombre: String = "",
    val plano: String = "", // URL de la imagen del plano (Cloudinary)
    val figuras: List<String> = emptyList(), // referencias a /figuras/*
    val pines: List<String> = emptyList() // referencias a /pines/*
)
