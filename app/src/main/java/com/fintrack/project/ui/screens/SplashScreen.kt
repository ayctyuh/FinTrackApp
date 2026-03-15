package com.fintrack.project.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.R
import kotlinx.coroutines.delay

val SplashBlue = Color(0xFF1D4ED8)

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(1500)
        // Exit: slide lên trên + fade out
        offsetY.animateTo(
            targetValue = -80f,
            animationSpec = tween(600, easing = EaseInCubic)
        )
        alpha.animateTo(0f, animationSpec = tween(400))
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .offset(y = offsetY.value.dp)
                .graphicsLayer { this.alpha = alpha.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_fintrack_logo),
                contentDescription = "FinTrack Logo",
                modifier = Modifier.size(150.dp),
                colorFilter = ColorFilter.tint(Color.White) // ✅ Logo trắng trên nền xanh
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "FinTrack",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}