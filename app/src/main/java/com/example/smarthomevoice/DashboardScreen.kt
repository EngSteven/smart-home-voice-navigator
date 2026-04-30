package com.example.smarthomevoice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores base para nuestro estilo Dark/Neon
val DarkBackground = Color(0xFF121212)
val CardBackground = Color(0xFF1E1E1E)
val NeonCyan = Color(0xFF00E5FF) // Color de foco principal
val TextGray = Color(0xFFAAAAAA)

@Composable
fun DashboardScreen(
    focusedIndex: Int,
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
            text = "Smart Home",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
        )
        Text(
            text = "Esperando comando de voz...",
            color = NeonCyan,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Fila 1: Luces (0) y Ventilador (1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeviceCard(
                title = "Luces",
                subtitle = "Sala y Cuarto",
                isFocused = focusedIndex == 0,
                modifier = Modifier.weight(1f)
            )
            DeviceCard(
                title = "Ventilador",
                subtitle = "Modo confort",
                isFocused = focusedIndex == 1,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fila 2: TV (2) y Rutinas (3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeviceCard(
                title = "TV",
                subtitle = "Media center",
                isFocused = focusedIndex == 2,
                modifier = Modifier.weight(1f)
            )
            DeviceCard(
                title = "Rutinas",
                subtitle = "Estudio, Dormir",
                isFocused = focusedIndex == 3,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(text = "Navega con: 'up', 'down', 'left', 'right'", color = TextGray, fontSize = 14.sp)
        Text(text = "Selecciona con: 'yes'", color = TextGray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Muestra el último comando detectado
        Text(
            text = "Comando detectado: $lastCommand",
            color = if (lastCommand != "Ninguno") NeonCyan else TextGray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Botón principal de grabación
        //Button(
        //    onClick = onRecordClick,
        //    enabled = !isListening, // Deshabilita el botón mientras graba
        //    modifier = Modifier
        //        .fillMaxWidth(0.7f)
        //        .height(56.dp)
        //) {
        //    Text(
        //        text = if (isListening) "Escuchando..." else "Tocar para Hablar",
        //        fontSize = 18.sp
        //    )
        //}
        //Spacer(modifier = Modifier.height(24.dp))
    }
}

// Componente modular y reutilizable para cada tarjeta
@Composable
fun DeviceCard(
    title: String,
    subtitle: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f), // Mantiene la tarjeta cuadrada
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        // Aquí ocurre la magia reactiva: Si isFocused es true, pintamos el borde neón
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