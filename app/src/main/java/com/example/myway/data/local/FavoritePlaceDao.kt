package com.example.myway.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePlaceDao {

    @Query("SELECT * FROM favorite_places ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoritePlaceEntity>>

    @Query("SELECT * FROM favorite_places WHERE id = :placeId LIMIT 1")
    suspend fun getFavoriteById(placeId: String): FavoritePlaceEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_places WHERE id = :placeId)")
    suspend fun isFavorite(placeId: String): Boolean

    // ✅ CORREGIDO: "insertFavorite" en lugar de "insertFavoraite"
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(place: FavoritePlaceEntity)

    @Delete
    suspend fun deleteFavorite(place: FavoritePlaceEntity)

    @Query("DELETE FROM favorite_places WHERE id = :placeId")
    suspend fun deleteFavoriteById(placeId: String)

    @Query("SELECT * FROM favorite_places WHERE name LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchFavorites(query: String): Flow<List<FavoritePlaceEntity>>
}