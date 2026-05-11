package com.example.smarthomevoice

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
 * Pantalla de detalle de un dispositivo individual que muestra su estado operativo actual.
 *
 * Adapta su diseño según la orientación del dispositivo (Portrait/Landscape) para maximizar
 * la visibilidad del indicador de estado y la lista de comandos disponibles mediante el
 * sistema de reconocimiento de voz continuo.
 *
 * @param deviceName Nombre del dispositivo actual (ej. "Luces Sala").
 * @param isDeviceOn Estado operativo del dispositivo (`true` para encendido, `false` para apagado).
 * @param isListening Estado actual del servicio de reconocimiento continuo de voz.
 * @param lastCommand Último comando procesado para mostrar retroalimentación visual al usuario.
 */
@Composable
fun DeviceScreen(
    deviceName: String,
    isDeviceOn: Boolean,
    isListening: Boolean,
    lastCommand: String
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Definición de la paleta de estado: Activo (Neón) o Inactivo (Gris oscuro)
    val statusColor = if (isDeviceOn) NeonCyan else Color.DarkGray
    val statusText = if (isDeviceOn) "ON" else "OFF"

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(title = deviceName)

            if (isLandscape) {
                // Disposición horizontal optimizada para pantallas anchas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    StatusIndicator(statusColor, statusText, size = 140.dp)
                    Spacer(modifier = Modifier.width(48.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        CommandHint(label = "Acción", commands = listOf("on", "off"))
                        CommandHint(label = "Salir", commands = listOf("no"))
                    }
                }
            } else {
                // Disposición vertical estándar para pantallas estrechas
                Spacer(modifier = Modifier.height(40.dp))

                StatusIndicator(statusColor, statusText, size = 220.dp)

                Spacer(modifier = Modifier.height(60.dp))

                CommandHint(label = "Acción", commands = listOf("on", "off"))
                CommandHint(label = "Salir", commands = listOf("no"))

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Componente visual circular que representa claramente el estado de un dispositivo.
 *
 * Escala su fuente internamente de manera proporcional al tamaño total del contenedor
 * para asegurar la legibilidad en diferentes resoluciones y disposiciones.
 *
 * @param statusColor Color que representa el estado del dispositivo (ej. Cyan para encendido).
 * @param statusText Texto corto que se muestra en el centro del indicador (ej. "ON", "OFF").
 * @param size Diámetro total del componente circular.
 */
@Composable
fun StatusIndicator(statusColor: Color, statusText: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .border(4.dp, statusColor, CircleShape)
            .background(statusColor.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            color = statusColor,
            // Ajuste dinámico del tamaño de la fuente basado en el diámetro del contenedor
            fontSize = (size.value * 0.12).sp,
            fontWeight = FontWeight.Bold
        )
    }
}