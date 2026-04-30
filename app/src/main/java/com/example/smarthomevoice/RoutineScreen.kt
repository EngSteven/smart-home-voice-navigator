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

@Composable
fun RoutineScreen(
    focusedRoutineIndex: Int,
    isRoutineRunning: Boolean,
    isListening: Boolean,
    lastCommand: String,
    onRecordClick: () -> Unit
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

            // Lista de rutinas
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

            CommandHint(label = "Navega con", commands = listOf("up", "down"))
            CommandHint(label = "Acción", commands = listOf("go", "stop"))
            CommandHint(label = "Salir", commands = listOf("no"))

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RoutineItem(title: String, subtitle: String, isFocused: Boolean, isRunning: Boolean) {
    val RunGreen = Color(0xFF00FF00)
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
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = if (isFocused) Color.White else TextGray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = TextGray, fontSize = 12.sp)
            }
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
