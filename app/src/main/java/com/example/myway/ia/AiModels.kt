package com.example.myway.ai

data class AiRequest(
    val ubicacion: String,
    val clima: String?,
    val estadoAnimo: String?,
    val tipoPlan: String?,
    val presupuesto: String?,
    val duracion: String?
)

data class AiRecommendation(
    val nombre: String,
    val tipo: String,
    val descripcion: String,
    val precioEstimado: String,
    val valoracion: Double,
    val ubicacion: String,
    val imagen: String
)
