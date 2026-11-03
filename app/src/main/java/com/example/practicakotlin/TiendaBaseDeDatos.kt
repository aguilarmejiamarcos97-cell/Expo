package com.example.practicakotlin

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Videojuego::class], version = 1)
abstract class TiendaBaseDeDatos : RoomDatabase() {
    abstract fun videojuegoDao(): VideojuegoDao
}