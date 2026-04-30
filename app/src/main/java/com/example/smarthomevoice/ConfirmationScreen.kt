package com.example.smarthomevoice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color de advertencia para acciones sensibles
val WarningOrange = Color(0xFFFF5722)

@Composable
fun ConfirmationScreen(
    actionName: String,
    actionDescription: String,
    isListening: Boolean,
    lastCommand: String,
    onRecordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Tarjeta de Alerta
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(3.dp, WarningOrange)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACCIÓN SENSIBLE",
                    color = WarningOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = actionName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = actionDescription,
                    color = TextGray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Opciones visuales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "Di 'YES'", color = WarningOrange, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = "Di 'NO'", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Interacción por voz
        Text(
            text = "Comando detectado: $lastCommand",
            color = if (lastCommand != "Ninguno") WarningOrange else TextGray,
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