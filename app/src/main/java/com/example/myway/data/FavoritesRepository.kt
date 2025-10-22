package com.example.myway.data

import android.content.Context
import android.util.Log
import com.example.myway.BuildConfig
import com.example.myway.data.models.FavoritePlace // ← Import del modelo
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritesRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val placesClient = if (!Places.isInitialized()) {
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
        Places.createClient(context)
    } else {
        Places.createClient(context)
    }

    companion object {
        private const val COLLECTION_FAVORITOS = "favoritos"
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

    /** Guardar lugar en favoritos **/
    suspend fun saveFavorite(placeId: String, placeName: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid
                    ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

                // Obtener datos del lugar desde Google Places
                val placeFields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.PHOTO_METADATAS
                )
                val request = FetchPlaceRequest.newInstance(placeId, placeFields)
                val response = placesClient.fetchPlace(request).await()
                val place = response.place

                // Descargar foto (opcional)
                val photoUrl = place.photoMetadatas?.firstOrNull()?.let { photoMetadata ->
                    try {
                        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(400)
                            .setMaxHeight(400)
                            .build()
                        val photoResponse = placesClient.fetchPhoto(photoRequest).await()
                        val bitmap = photoResponse.bitmap

                        val filename = "place_${placeId}.jpg"
                        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it)
                        }
                        context.filesDir.absolutePath + "/" + filename
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al descargar foto: ${e.message}", e)
                        null
                    }
                }

                val favoriteData = hashMapOf(
                    "id" to (place.id ?: placeId),
                    "name" to (place.name ?: placeName),
                    "address" to place.address,
                    "photoUrl" to photoUrl,
                    "latitude" to (place.latLng?.latitude ?: 0.0),
                    "longitude" to (place.latLng?.longitude ?: 0.0),
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection(COLLECTION_FAVORITOS)
                    .document(userId)
                    .collection("lugares")
                    .document(placeId)
                    .set(favoriteData)
                    .await()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /** Eliminar favorito **/
    suspend fun deleteFavorite(placeId: String) {
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext
                db.collection(COLLECTION_FAVORITOS)
                    .document(userId)
                    .collection("lugares")
                    .document(placeId)
                    .delete()
                    .await()

                // Eliminar foto local
                val filename = "place_${placeId}.jpg"
                context.deleteFile(filename)
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
                    doc.reference.delete().await()
                    context.deleteFile("place_${doc.id}.jpg")
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
