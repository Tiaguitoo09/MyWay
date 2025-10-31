package com.example.myway.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.myway.BuildConfig
import com.example.myway.data.models.FavoritePlace
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class FavoritesRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val placesClient = if (!Places.isInitialized()) {
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
        Places.createClient(context)
    } else {
        Places.createClient(context)
    }

    companion object {
        private const val COLLECTION_FAVORITOS = "favoritos"
        private const val STORAGE_FAVORITES_PATH = "favorites_photos"
        private const val TAG = "FavoritesRepository"
    }

    /** Obtener todos los favoritos en tiempo real **/
    fun getAllFavorites(): Flow<List<FavoritePlace>> = callbackFlow {
        val userId = auth.currentUser?.uid
        Log.d(TAG, "🔄 Obteniendo favoritos para userId: $userId")

        if (userId == null) {
            Log.e(TAG, "❌ No hay usuario autenticado")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val collectionRef = db.collection(COLLECTION_FAVORITOS)
            .document(userId)
            .collection("lugares")

        val listener = collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al escuchar cambios: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val favoritos = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data?.let { data ->
                            FavoritePlace(
                                id = data["id"] as? String ?: doc.id,
                                name = data["name"] as? String ?: "",
                                address = data["address"] as? String,
                                photoUrl = data["photoUrl"] as? String,
                                latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                                longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                                timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al parsear documento ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                trySend(favoritos)
            }

        awaitClose {
            listener.remove()
        }
    }

    /** Verificar si un lugar es favorito **/
    suspend fun isFavorite(placeId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext false
                val doc = db.collection(COLLECTION_FAVORITOS)
                    .document(userId)
                    .collection("lugares")
                    .document(placeId)
                    .get()
                    .await()
                doc.exists()
            } catch (e: Exception) {
                false
            }
        }
    }

    /** Subir foto a Firebase Storage **/
    private suspend fun uploadPhotoToStorage(
        bitmap: Bitmap,
        userId: String,
        placeId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📸 Iniciando subida de foto para placeId: $placeId")

            // Convertir bitmap a byte array con compresión
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val imageData = baos.toByteArray()

            Log.d(TAG, "📦 Tamaño de imagen: ${imageData.size / 1024} KB")

            // Referencia en Storage
            val storageRef = storage.reference
                .child("$STORAGE_FAVORITES_PATH/$userId/$placeId.jpg")

            // Subir imagen
            val uploadTask = storageRef.putBytes(imageData).await()
            Log.d(TAG, "✅ Upload task completado: ${uploadTask.metadata?.path}")

            // Obtener URL de descarga
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d(TAG, "✅ URL de descarga obtenida: $downloadUrl")

            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al subir foto a Storage: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /** Descargar foto de Google Places **/
    private suspend fun fetchPlacePhoto(place: Place): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val photoMetadata = place.photoMetadatas?.firstOrNull()

            if (photoMetadata == null) {
                Log.w(TAG, "⚠️ No hay metadatos de foto para este lugar")
                return@withContext null
            }

            Log.d(TAG, "📷 Descargando foto de Google Places...")
            Log.d(TAG, "📷 Photo metadata: width=${photoMetadata.width}, height=${photoMetadata.height}")

            val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(800)  // Aumentado para mejor calidad
                .setMaxHeight(800)
                .build()

            val photoResponse = placesClient.fetchPhoto(photoRequest).await()
            val bitmap = photoResponse.bitmap

            Log.d(TAG, "✅ Foto descargada: ${bitmap.width}x${bitmap.height}")

            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al descargar foto de Places: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /** Guardar lugar en favoritos **/
    /** Guardar lugar en favoritos **/
    suspend fun saveFavorite(placeId: String, placeName: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "💾 Iniciando guardado de favorito: $placeName")

                val userId = auth.currentUser?.uid
                    ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

                Log.d(TAG, "👤 UserId: $userId")

                // ✅ DETECTAR SI ES LUGAR DE FIREBASE O GOOGLE PLACES
                if (!placeId.startsWith("ChIJ") && !placeId.startsWith("Ei")) {
                    // 📦 Es un lugar de Firebase - obtener datos de Firestore
                    Log.d(TAG, "📦 Lugar de Firebase detectado: $placeId")

                    try {
                        val firestore = FirebaseFirestore.getInstance()
                        val doc = firestore.collection("lugares")
                            .document(placeId)
                            .get()
                            .await()

                        if (doc.exists()) {
                            val favoriteData = hashMapOf(
                                "id" to placeId,
                                "name" to (doc.getString("name") ?: placeName),
                                "address" to doc.getString("address"),
                                "photoUrl" to doc.getString("photoUrl"),
                                "latitude" to (doc.getDouble("latitude") ?: 0.0),
                                "longitude" to (doc.getDouble("longitude") ?: 0.0),
                                "rating" to (doc.getDouble("rating") ?: 0.0), // ← AGREGAR ESTA LÍNEA
                                "timestamp" to System.currentTimeMillis()
                            )

                            Log.d(TAG, "💾 Guardando lugar de Firebase en favoritos...")
                            db.collection(COLLECTION_FAVORITOS)
                                .document(userId)
                                .collection("lugares")
                                .document(placeId)
                                .set(favoriteData)
                                .await()

                            Log.d(TAG, "✅ Favorito de Firebase guardado exitosamente")
                            return@withContext Result.success(Unit)
                        } else {
                            return@withContext Result.failure(Exception("Lugar no encontrado en Firebase"))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al obtener lugar de Firebase: ${e.message}", e)
                        return@withContext Result.failure(e)
                    }
                } else {
                    // 🌍 Es un lugar de Google Places - usar código existente
                    Log.d(TAG, "🌍 Lugar de Google Places detectado: $placeId")

                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.PHOTO_METADATAS
                    )

                    Log.d(TAG, "🔍 Obteniendo detalles del lugar...")
                    val request = FetchPlaceRequest.newInstance(placeId, placeFields)
                    val response = placesClient.fetchPlace(request).await()
                    val place = response.place

                    Log.d(TAG, "✅ Detalles obtenidos: ${place.name}")
                    Log.d(TAG, "📸 Cantidad de fotos: ${place.photoMetadatas?.size ?: 0}")

                    // Intentar descargar y subir la foto
                    var photoUrl: String? = null

                    if (place.photoMetadatas?.isNotEmpty() == true) {
                        Log.d(TAG, "🖼️ Procesando foto del lugar...")

                        val bitmap = fetchPlacePhoto(place)

                        if (bitmap != null) {
                            Log.d(TAG, "🚀 Subiendo foto a Firebase Storage...")
                            photoUrl = uploadPhotoToStorage(bitmap, userId, placeId)

                            if (photoUrl != null) {
                                Log.d(TAG, "✅ Foto guardada exitosamente en: $photoUrl")
                            } else {
                                Log.w(TAG, "⚠️ No se pudo subir la foto a Storage")
                            }
                        } else {
                            Log.w(TAG, "⚠️ No se pudo descargar la foto de Places")
                        }
                    } else {
                        Log.w(TAG, "⚠️ Este lugar no tiene fotos disponibles")
                    }

                    // Guardar en Firestore
                    val favoriteData = hashMapOf(
                        "id" to (place.id ?: placeId),
                        "name" to (place.name ?: placeName),
                        "address" to place.address,
                        "photoUrl" to photoUrl,
                        "latitude" to (place.latLng?.latitude ?: 0.0),
                        "longitude" to (place.latLng?.longitude ?: 0.0),
                        "timestamp" to System.currentTimeMillis()
                    )

                    Log.d(TAG, "💾 Guardando en Firestore...")
                    Log.d(TAG, "📝 Datos a guardar: $favoriteData")

                    db.collection(COLLECTION_FAVORITOS)
                        .document(userId)
                        .collection("lugares")
                        .document(placeId)
                        .set(favoriteData)
                        .await()

                    Log.d(TAG, "✅ Favorito guardado exitosamente en Firestore")

                    if (photoUrl == null) {
                        Log.w(TAG, "⚠️ ADVERTENCIA: Favorito guardado pero SIN FOTO")
                    }

                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error CRÍTICO al guardar favorito: ${e.message}", e)
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /** Eliminar favorito **/
    suspend fun deleteFavorite(placeId: String) {
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext

                // Eliminar de Firestore
                db.collection(COLLECTION_FAVORITOS)
                    .document(userId)
                    .collection("lugares")
                    .document(placeId)
                    .delete()
                    .await()

                // Eliminar imagen de Firebase Storage
                try {
                    val storageRef = storage.reference
                        .child("$STORAGE_FAVORITES_PATH/$userId/$placeId.jpg")
                    storageRef.delete().await()
                    Log.d(TAG, "✅ Imagen eliminada de Storage")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ No se pudo eliminar la imagen de Storage: ${e.message}")
                }

                Log.d(TAG, "✅ Favorito eliminado")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al eliminar favorito: ${e.message}", e)
            }
        }
    }

    /** Buscar favoritos **/
    fun searchFavorites(query: String): Flow<List<FavoritePlace>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val collectionRef = db.collection(COLLECTION_FAVORITOS)
            .document(userId)
            .collection("lugares")

        val listener = collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val favoritos = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data?.let { data ->
                            FavoritePlace(
                                id = data["id"] as? String ?: doc.id,
                                name = data["name"] as? String ?: "",
                                address = data["address"] as? String,
                                photoUrl = data["photoUrl"] as? String,
                                latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                                longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                                timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                            )
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.filter { it.name.contains(query, ignoreCase = true) }

                trySend(favoritos)
            }

        awaitClose {
            listener.remove()
        }
    }

    /** Limpiar todos los favoritos **/
    suspend fun clearAllFavorites(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid
                    ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

                val snapshot = db.collection(COLLECTION_FAVORITOS)
                    .document(userId)
                    .collection("lugares")
                    .get()
                    .await()

                snapshot.documents.forEach { doc ->
                    // Eliminar documento de Firestore
                    doc.reference.delete().await()

                    // Eliminar imagen de Storage
                    try {
                        val storageRef = storage.reference
                            .child("$STORAGE_FAVORITES_PATH/$userId/${doc.id}.jpg")
                        storageRef.delete().await()
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ No se pudo eliminar imagen ${doc.id}: ${e.message}")
                    }
                }

                Log.d(TAG, "✅ Todos los favoritos eliminados")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al limpiar favoritos: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}