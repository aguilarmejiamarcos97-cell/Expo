package com.example.practicakotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Room: se crea la base de datos y se obtiene el DAO
        val baseDeDatos = Room.databaseBuilder(
            this,
            TiendaBaseDeDatos::class.java,
            "tienda_db"
        ).build()
        val videojuegoDao = baseDeDatos.videojuegoDao()

        setContent {
            PantallaTienda(videojuegoDao)
        }
    }
}

// ---------------------------------------------------------
// PANTALLA PRINCIPAL
// De arriba a abajo: mensaje, elegido, filtro y lista
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTienda(videojuegoDao: VideojuegoDao) {
    val viewModel = remember { TiendaViewModel(videojuegoDao) }
    val soloConPrecio = remember { mutableStateOf(false) }

    // SCAFFOLD: barra de arriba + botón flotante + contenido
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(" Mi tienda") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6650A4),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.agregarVideojuego() }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3EEFA))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            CajaMensaje(viewModel.mensaje.value)

            PanelElegido(viewModel)

            BotonesFiltro(
                soloConPrecio = soloConPrecio.value,
                alCambiar = { nuevoValor -> soloConPrecio.value = nuevoValor }
            )

            // La lista ocupa el espacio que sobra y se puede deslizar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp) // espacio para que el botón + no tape nada
            ) {
                ListaVideojuegos(viewModel, soloConPrecio.value)
            }
        }
    }
}


// 1. MENSAJE

@Composable
fun CajaMensaje(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFD1C4E9), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(mensaje, fontWeight = FontWeight.Bold)
    }
}


// 2. PANEL DEL VIDEOJUEGO ELEGIDO
@Composable
fun PanelElegido(viewModel: TiendaViewModel) {

    // NULL SAFETY: ?.let (solo se ejecuta si hay uno elegido)
    viewModel.videojuegoElegido.value?.let { elegido ->
        // DATA CLASS: desestructuración (el id no se muestra, por eso el _)
        val (_, nombre, precio) = elegido

        // Lo que escribes en el campo; se reinicia al elegir otro juego
        val textoPrecio = remember(elegido.id) { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text("Elegido: " + nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Precio: " + (precio ?: "Sin precio"))

            OutlinedTextField(
                value = textoPrecio.value,
                onValueChange = { textoPrecio.value = it },
                label = { Text("Nuevo precio (vacío = sin precio)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    // toDoubleOrNull() da null si el texto está vacío o no es número
                    onClick = { viewModel.cambiarPrecio(textoPrecio.value.toDoubleOrNull()) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Guardar precio")
                }
                Button(
                    onClick = { viewModel.eliminarElegido() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Eliminar")
                }
            }
        }
    }

    if (viewModel.videojuegoElegido.value == null) {
        Text(
            " Elige un videojuego de la lista",
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}


// 3. BOTONES DEL FILTRO (el activo se pone morado)
// EVENTO DE UI: el click avisa a la pantalla el nuevo valor

@Composable
fun BotonesFiltro(soloConPrecio: Boolean, alCambiar: (Boolean) -> Unit) {
    val morado = Color(0xFF6650A4)
    val gris = Color(0xFFB0A8C0)

    val colorTodos = if (soloConPrecio) gris else morado
    val colorConPrecio = if (soloConPrecio) morado else gris

    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { alCambiar(false) },
            colors = ButtonDefaults.buttonColors(containerColor = colorTodos)
        ) {
            Text("Todos")
        }
        Button(
            onClick = { alCambiar(true) },
            colors = ButtonDefaults.buttonColors(containerColor = colorConPrecio)
        ) {
            Text("Con precio")
        }
    }
}


// 4. LISTA DE VIDEOJUEGOS

@Composable
fun ListaVideojuegos(viewModel: TiendaViewModel, soloConPrecio: Boolean) {
    if (viewModel.listaVideojuegos.value.isEmpty()) {
        Text("No hay videojuegos, presiona + para agregar", color = Color.Gray)
    }

    for (videojuego in viewModel.listaVideojuegos.value) {
        if (soloConPrecio == false || videojuego.precio != null) {
            TarjetaVideojuego(videojuego, viewModel)
        }
    }
}


// 5. UNA TARJETA (nombre, precio y botón Elegir)

@Composable
fun TarjetaVideojuego(videojuego: Videojuego, viewModel: TiendaViewModel) {
    // ¿Este es el que está elegido? (NULL SAFETY: ?.)
    val estaElegido = viewModel.videojuegoElegido.value?.id == videojuego.id

    val colorFondo = if (estaElegido) Color(0xFFD1C4E9) else Color.White

    // NULL SAFETY: ?. y ?:
    val precioTexto = videojuego.precio?.toString() ?: "Sin precio"
    val colorPrecio = if (videojuego.precio == null) Color.Gray else Color(0xFF2E7D32)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(colorFondo, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(videojuego.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(precioTexto, color = colorPrecio)
        }

        // EVENTO DE UI que le avisa al ViewModel
        Button(onClick = { viewModel.elegirVideojuego(videojuego) }) {
            Text("Elegir")
        }
    }
}