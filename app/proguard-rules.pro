# ========================================
# REGLAS GENERALES
# ========================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exception
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Mantener clases de Android
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.fragment.app.Fragment

# ========================================
# FIREBASE
# ========================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firestore - MUY IMPORTANTE para tus modelos de datos
-keepclassmembers class com.example.myway.** {
  <fields>;
  <init>();
}
-keep class * extends com.google.firebase.firestore.** { *; }

# Firebase Auth
-keepattributes Signature
-keepattributes *Annotation*

# ========================================
# GOOGLE MAPS & LOCATION
# ========================================
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.android.gms.**

# Google Places
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.libraries.places.**

# Maps Utils (polylines)
-dontwarn com.google.maps.android.**

# ========================================
# GSON (para caché de rutas)
# ========================================
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Tus clases de datos que uses con Gson
-keep class com.example.myway.data.** { *; }
-keep class com.example.myway.model.** { *; }

# ========================================
# ROOM DATABASE
# ========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ========================================
# JETPACK COMPOSE
# ========================================
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ========================================
# KOTLIN & COROUTINES
# ========================================
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Kotlin Reflect
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ========================================
# COIL (carga de imágenes)
# ========================================
-keep class coil.** { *; }
-dontwarn coil.**

# ========================================
# NAVIGATION COMPOSE
# ========================================
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.**

# ========================================
# ACCOMPANIST (permisos)
# ========================================
-keep class com.google.accompanist.** { *; }
-dontwarn com.google.accompanist.**

# ========================================
# SCENEFORM
# ========================================
-keep class com.google.ar.sceneform.** { *; }
-dontwarn com.google.ar.sceneform.**

# ========================================
# DEBUGGING (descomenta si necesitas debuggear crashes)
# ========================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile