package com.example.myway.data.models

data class FavoritePlace(
    val id: String,
    val name: String,
    val address: String? = null,
    val photoUrl: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)