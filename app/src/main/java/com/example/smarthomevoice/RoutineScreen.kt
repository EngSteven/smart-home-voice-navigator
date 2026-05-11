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

/**
 * Pantalla de gestión y ejecución de rutinas domóticas preconfiguradas.
 *
 * Permite al usuario navegar verticalmente por una lista de agrupaciones de comandos
 * (rutinas) utilizando el sistema de reconocimiento de voz. Proporciona retroalimentación
 * visual clara sobre qué rutina está enfocada y cuál se encuentra actualmente en ejecución.
 *
 * @param focusedRoutineIndex Índice de la lista que corresponde a la rutina actualmente resaltada.
 * @param isRoutineRunning Indica si la rutina enfocada está actualmente en proceso de ejecución.
 * @param isListening Estado actual del servicio de reconocimiento de voz continuo.
 * @param lastCommand Último comando procesado para visualización en el componente de estado.
 */
@Composable
fun RoutineScreen(
    focusedRoutineIndex: Int,
    isRoutineRunning: Boolean,
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
            AppHeader(title = "Modo Rutinas")

            RoutineItem(
                title = "Modo Estudio",
                subtitle = "Luces blancas, música suave",
                isFocused = focusedRoutineIndex == 0,
                isRunning = isRoutineRunning && focusedRoutineIndex == 0
            )
            Spacer(modifier = Modifier.height(12.dp))

            RoutineItem(
                title = "Modo Noche",
                subtitle = "Apagar todo, activar alarma",
                isFocused = focusedRoutineIndex == 1,
                isRunning = isRoutineRunning && focusedRoutineIndex == 1
            )
            Spacer(modifier = Modifier.height(12.dp))

            RoutineItem(
                title = "Bienvenida a Casa",
                subtitle = "Luces cálidas, TV encendida",
                isFocused = focusedRoutineIndex == 2,
                isRunning = isRoutineRunning && focusedRoutineIndex == 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Guía contextual de navegación y control de ejecución
            CommandHint(label = "Navega con", commands = listOf("up", "down"))
            CommandHint(label = "Acción", commands = listOf("go", "stop"))
            CommandHint(label = "Salir", commands = listOf("no"))

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Componente visual que representa una rutina específica dentro de la lista.
 *
 * Implementa un sistema de jerarquía visual de tres estados mediante el color del borde:
 * 1. Ejecución (Verde): Máxima prioridad, indica que las acciones se están despachando.
 * 2. Enfocado (Cyan): Indica que la rutina está seleccionada y lista para recibir comandos.
 * 3. Inactivo (Gris oscuro): Estado por defecto cuando no tiene el foco.
 *
 * @param title Nombre descriptivo de la rutina (ej. "Modo Estudio").
 * @param subtitle Lista de acciones o dispositivos afectados por la rutina.
 * @param isFocused Define si la rutina es el objetivo actual de los comandos de voz.
 * @param isRunning Define si la rutina se está ejecutando activamente en el sistema.
 */
@Composable
fun RoutineItem(title: String, subtitle: String, isFocused: Boolean, isRunning: Boolean) {
    val RunGreen = Color(0xFF00FF00)

    // Cálculo de la prioridad del color del borde según el estado operativo
    val borderColor = when {
        isRunning -> RunGreen
        isFocused -> NeonCyan
        else -> Color.DarkGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(if (isFocused) 3.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isFocused) Color.White else TextGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            // Etiqueta de estado dinámica visible únicamente cuando la rutina está en foco
            if (isFocused) {
                Text(
                    text = if (isRunning) "EJECUTANDO" else "EN ESPERA",
                    color = if (isRunning) RunGreen else NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}