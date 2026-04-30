package com.example.smarthomevoice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Modo Rutinas",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
        )

        // Lista de rutinas
        RoutineItem(
            title = "Modo Estudio",
            subtitle = "Luces blancas, música suave",
            isFocused = focusedRoutineIndex == 0,
            isRunning = isRoutineRunning && focusedRoutineIndex == 0
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoutineItem(
            title = "Modo Noche",
            subtitle = "Apagar todo, activar alarma",
            isFocused = focusedRoutineIndex == 1,
            isRunning = isRoutineRunning && focusedRoutineIndex == 1
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoutineItem(
            title = "Bienvenida a Casa",
            subtitle = "Luces cálidas, TV encendida",
            isFocused = focusedRoutineIndex == 2,
            isRunning = isRoutineRunning && focusedRoutineIndex == 2
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(text = "Navega con: 'up', 'down'", color = TextGray, fontSize = 14.sp)
        Text(text = "Controla con: 'go', 'stop' | 'no' para salir", color = TextGray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Interacción por voz
        Text(
            text = "Comando detectado: $lastCommand",
            color = if (lastCommand != "Ninguno") NeonCyan else TextGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        //Button(
        //    onClick = onRecordClick,
        //    enabled = !isListening,
        //    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
        //) {
        //    Text(if (isListening) "Escuchando..." else "Tocar para Hablar", fontSize = 18.sp)
        //}
        //Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RoutineItem(title: String, subtitle: String, isFocused: Boolean, isRunning: Boolean) {
    // Si está corriendo, lo pintamos de un verde neón, si solo está en foco, cyan neón.
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
                Text(text = title, color = if (isFocused) Color.White else TextGray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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