package com.example.myway.utils

import android.content.Context
import android.util.Log

/**
 * Limpia toda la información del usuario:
 * - SharedPreferences (caché local)
 * - UsuarioTemporal (memoria)
 * - ImageStorage (foto guardada)
 */
fun limpiarCacheUsuario(context: Context) {
    Log.d("CacheUtils", "🧹 Limpiando caché de usuario...")
    
    // 1️⃣ Limpiar SharedPreferences (caché de foto y nombre)
    val sharedPrefs = context.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().apply {
        remove("cached_foto_perfil")
        remove("cached_nombre")
        apply()
    }
    
    // 2️⃣ Limpiar UsuarioTemporal (memoria)
    UsuarioTemporal.correo = null
    UsuarioTemporal.apellido = null
    UsuarioTemporal.nombre = null
    UsuarioTemporal.fechaNacimiento = null
    UsuarioTemporal.fotoUrl = null
    UsuarioTemporal.fotoLocalUri = null
    
    // 3️⃣ Limpiar ImageStorage (foto guardada en SharedPreferences)
    ImageStorage.eliminarImagen(context)
    
    Log.d("CacheUtils", "✅ Caché limpiado completamente")
}