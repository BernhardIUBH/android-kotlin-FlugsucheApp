package com.bernhard.flugsucheapp

data class DataClassFlight(
    val id: String,
    val rawPrice: Double,
    val originName: String,
    val originIata: String,
    val destinationName: String,
    val destinationIata: String,
    val duration: Int,
    val departure: String,
    val arrival: String,
    val carrierName: String
)