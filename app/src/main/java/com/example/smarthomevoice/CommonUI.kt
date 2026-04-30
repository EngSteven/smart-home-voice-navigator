package com.example.smarthomevoice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun VoiceCommandStatus(isListening: Boolean, lastCommand: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(scale)
                        .background(NeonCyan, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isListening) "ESCUCHANDO..." else "MICRÓFONO DESACTIVADO",
                color = if (isListening) NeonCyan else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        
        if (lastCommand != "Ninguno") {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comando: ",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = lastCommand.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AppHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(40.dp)
                .height(2.dp)
                .background(NeonCyan)
        )
    }
}

@Composable
fun CommandHint(label: String, commands: List<String>, highlightColor: Color = NeonCyan) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$label: ",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
        commands.forEachIndexed { index, cmd ->
            Text(
                text = "'$cmd'",
                color = highlightColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .background(highlightColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, highlightColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            if (index < commands.size - 1) {
                Text(text = " ", fontSize = 12.sp)
            }
        }
    }
}
