package com.example.practicakotlin

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface VideojuegoDao {

    @Insert
    suspend fun insertar(videojuego: Videojuego)

    @Query("SELECT * FROM Videojuego")
    suspend fun obtenerTodos(): List<Videojuego>

    @Update
    suspend fun actualizar(videojuego: Videojuego)

    @Delete
    suspend fun eliminar(videojuego: Videojuego)
}