package com.example.practicakotlin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TiendaViewModel(private val videojuegoDao: VideojuegoDao) : ViewModel() {

    val listaVideojuegos = mutableStateOf<List<Videojuego>>(listOf())
    val mensaje = mutableStateOf("Bienvenido a la tienda")

    // Puede ser null mientras no se elija nada
    val videojuegoElegido = mutableStateOf<Videojuego?>(null)

    init {
        cargarVideojuegos()
    }

    fun cargarVideojuegos() {
        viewModelScope.launch {
            listaVideojuegos.value = videojuegoDao.obtenerTodos()
        }
    }

    fun agregarVideojuego() {
        viewModelScope.launch {
            // Busca el número más alto que ya aparece en los nombres ("Juego 16" -> 16)
            var ultimoNumero = 0
            for (videojuego in listaVideojuegos.value) {
                val numero = videojuego.nombre.removePrefix("Juego ").toIntOrNull() ?: 0
                if (numero > ultimoNumero) {
                    ultimoNumero = numero
                }
            }

            val nuevo = Videojuego(0, "Juego " + (ultimoNumero + 1), null)
            videojuegoDao.insertar(nuevo)
            listaVideojuegos.value = videojuegoDao.obtenerTodos()
            mensaje.value = "Se agregó " + nuevo.nombre
        }
    }


    fun elegirVideojuego(videojuego: Videojuego) {
        videojuegoElegido.value = videojuego
        mensaje.value = "Elegiste " + videojuego.nombre
    }

    fun cambiarPrecio(nuevoPrecio: Double?) {
        val elegido = videojuegoElegido.value
        if (elegido != null) {
            viewModelScope.launch {
                val actualizado = elegido.copy(precio = nuevoPrecio)
                videojuegoDao.actualizar(actualizado)
                videojuegoElegido.value = actualizado
                listaVideojuegos.value = videojuegoDao.obtenerTodos()

                if (nuevoPrecio == null) {
                    mensaje.value = elegido.nombre + " quedó sin precio"
                } else {
                    mensaje.value = "Nuevo precio para " + elegido.nombre
                }
            }
        }
    }

    fun eliminarElegido() {
        val elegido = videojuegoElegido.value
        if (elegido != null) {
            viewModelScope.launch {
                videojuegoDao.eliminar(elegido)
                videojuegoElegido.value = null
                listaVideojuegos.value = videojuegoDao.obtenerTodos()
                mensaje.value = "Se eliminó " + elegido.nombre
            }
        }
    }
}