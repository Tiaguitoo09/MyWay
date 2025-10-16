package com.example.myway.utils

import android.content.Context
import android.net.Uri

object ImageStorage {

    private const val PREFS_NAME = "perfil_prefs"
    private const val KEY_IMAGE_URI = "image_uri"

    fun guardarImagenUri(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_IMAGE_URI, uri.toString()).apply()
    }

    fun obtenerImagenUri(context: Context): Uri? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_IMAGE_URI, null)
        return uriString?.let { Uri.parse(it) }
    }

    fun eliminarImagen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_IMAGE_URI).apply()
    }
}