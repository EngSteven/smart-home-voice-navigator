package com.example.smarthomevoice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeviceScreen(
    deviceName: String,
    isDeviceOn: Boolean,
    isListening: Boolean,
    lastCommand: String,
    onRecordClick: () -> Unit
) {
    // Si está encendido usamos nuestro Cyan Neón, si no, un gris apagado
    val statusColor = if (isDeviceOn) NeonCyan else Color.DarkGray
    val statusText = if (isDeviceOn) "ENCENDIDO" else "APAGADO"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado
        Text(
            text = "Control de Dispositivo",
            color = TextGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )
        Text(
            text = deviceName,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        // Círculo indicador de estado (foco visual principal)
        Box(
            modifier = Modifier
                .size(200.dp)
                .border(4.dp, statusColor, CircleShape)
                .background(statusColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Instrucciones contextuales
        Text(text = "Comandos disponibles: 'on', 'off'", color = TextGray, fontSize = 14.sp)
        Text(text = "Di 'no' para regresar", color = TextGray, fontSize = 14.sp)

        Spacer(modifier = Modifier.weight(1f))

        // Sección de interacción por voz (idéntica al Dashboard)
        Text(
            text = "Comando detectado: $lastCommand",
            color = if (lastCommand != "Ninguno") NeonCyan else TextGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = onRecordClick,
            enabled = !isListening,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Text(if (isListening) "Escuchando..." else "Tocar para Hablar", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}