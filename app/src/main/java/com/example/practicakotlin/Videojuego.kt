package com.example.practicakotlin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Videojuego(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val nombre: String,
    val precio: Double?
)