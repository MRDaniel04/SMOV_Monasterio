package com.nextapp.monasterio.ui

import androidx.annotation.StringRes
import com.nextapp.monasterio.R

enum class GalleryType(val id: String, @StringRes val resourceId: Int) {
    ALL("all", R.string.gallery_type_all),
    CUADRO("cuadro", R.string.gallery_type_painting),
    ESCULTURA("escultura", R.string.gallery_type_sculpture),
    ARQUITECTURA("arquitectura", R.string.gallery_type_architecture),
    OTRO("otro", R.string.gallery_type_other);

    companion object {
        fun fromId(id: String): GalleryType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: OTRO
        }
    }
}
