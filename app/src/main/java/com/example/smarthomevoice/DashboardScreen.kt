package com.example.smarthomevoice

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Paleta de colores base para el tema visual Dark/Neon de la aplicación. */
val DarkBackground = Color(0xFF121212)
val CardBackground = Color(0xFF1E1E1E)
val NeonCyan = Color(0xFF00E5FF)
val TextGray = Color(0xFFAAAAAA)

/**
 * Pantalla principal del panel de control (Dashboard) que muestra los dispositivos conectados.
 *
 * Implementa una cuadrícula responsiva que ajusta las proporciones de las tarjetas de
 * dispositivos dependiendo de la orientación de la pantalla. Soporta navegación espacial
 * por voz indicando visualmente el dispositivo actualmente enfocado.
 *
 * @param focusedIndex Índice del dispositivo actualmente seleccionado mediante comandos de voz.
 * @param isListening Estado actual del servicio de reconocimiento continuo de voz.
 * @param lastCommand Último comando procesado para retroalimentación visual.
 */
@Composable
fun DashboardScreen(
    focusedIndex: Int,
    isListening: Boolean,
    lastCommand: String
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        VoiceCommandStatus(isListening = isListening, lastCommand = lastCommand)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(title = "Smart Home")

            // Ajuste dinámico de las proporciones de las tarjetas según la orientación del dispositivo
            val cardModifier = if (isLandscape) Modifier.height(120.dp) else Modifier.aspectRatio(1f)

            // Fila 1: Luces (Índice 0) y Ventilador (Índice 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DeviceCard(
                    title = "Luces",
                    subtitle = "Sala y Cuarto",
                    isFocused = focusedIndex == 0,
                    modifier = Modifier.weight(1f).then(cardModifier)
                )
                DeviceCard(
                    title = "Ventilador",
                    subtitle = "Modo confort",
                    isFocused = focusedIndex == 1,
                    modifier = Modifier.weight(1f).then(cardModifier)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fila 2: TV (Índice 2) y Rutinas (Índice 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DeviceCard(
                    title = "TV",
                    subtitle = "Media center",
                    isFocused = focusedIndex == 2,
                    modifier = Modifier.weight(1f).then(cardModifier)
                )
                DeviceCard(
                    title = "Rutinas",
                    subtitle = "Estudio, Dormir",
                    isFocused = focusedIndex == 3,
                    modifier = Modifier.weight(1f).then(cardModifier)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guía de comandos de voz para la navegación de la interfaz
            CommandHint(label = "Navega con", commands = listOf("up", "down", "left", "right"))
            CommandHint(label = "Selecciona con", commands = listOf("yes"))

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Tarjeta visual que representa un dispositivo o categoría interactiva en el panel de control.
 *
 * Responde a cambios de estado de enfoque (`isFocused`) alterando el color del borde y
 * del título principal para proveer retroalimentación visual clara durante la navegación.
 *
 * @param title Nombre principal del dispositivo o grupo.
 * @param subtitle Descripción breve o estado actual del dispositivo.
 * @param isFocused Indica si este componente tiene el foco actual de navegación.
 * @param modifier Modificador opcional para ajustar la disposición y tamaño.
 */
@Composable
fun DeviceCard(
    title: String,
    subtitle: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = if (isFocused) BorderStroke(3.dp, NeonCyan) else BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = if (isFocused) NeonCyan else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}