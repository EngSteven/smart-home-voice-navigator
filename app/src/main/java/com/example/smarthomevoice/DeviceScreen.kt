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

@Composable
fun DeviceScreen(
    deviceName: String,
    isDeviceOn: Boolean,
    isListening: Boolean,
    lastCommand: String,
    onRecordClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Si está encendido usamos nuestro Cyan Neón, si no, un gris apagado
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
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
            fontSize = (size.value * 0.12).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
