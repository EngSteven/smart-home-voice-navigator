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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isLandscape) Arrangement.Top else Arrangement.Center
        ) {
            // Tarjeta de Alerta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(3.dp, WarningOrange)
            ) {
                Column(
                    modifier = Modifier.padding(if (isLandscape) 16.dp else 24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACCIÓN SENSIBLE",
                        color = WarningOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = actionName,
                        color = Color.White,
                        fontSize = if (isLandscape) 22.sp else 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = actionDescription,
                        color = TextGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 32.dp))

                    // Opciones visuales
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CommandHint(label = "Confirmar", commands = listOf("yes"), highlightColor = WarningOrange)
                        CommandHint(label = "Cancelar", commands = listOf("no"), highlightColor = TextGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
