package com.example.myway.utils

import android.content.Context
import android.util.Log


fun limpiarCacheUsuario(context: Context) {
    Log.d("CacheUtils", "🧹 Limpiando caché de usuario...")
    

    val sharedPrefs = context.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().apply {
        remove("cached_foto_perfil")
        remove("cached_nombre")
        apply()
    }
    

    UsuarioTemporal.correo = null
    UsuarioTemporal.apellido = null
    UsuarioTemporal.nombre = null
    UsuarioTemporal.fechaNacimiento = null
    UsuarioTemporal.fotoUrl = null
    UsuarioTemporal.fotoLocalUri = null
    

    ImageStorage.eliminarImagen(context)
    
    Log.d("CacheUtils", "✅ Caché limpiado completamente")
}