package com.fintrack.project.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    // Logo + chữ: slide từ dưới lên
    val logoOffsetY = remember { Animatable(60f) }
    val logoAlpha = remember { Animatable(0f) }

    // Buttons: fade in sau
    val buttonsAlpha = remember { Animatable(0f) }
    val buttonsOffsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        // Logo + tên slide lên
        logoOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(700, easing = EaseOutCubic)
        )
        logoAlpha.animateTo(1f, animationSpec = tween(500))

        // Buttons xuất hiện sau
        delay(200)
        buttonsOffsetY.animateTo(0f, animationSpec = tween(600, easing = EaseOutCubic))
        buttonsAlpha.animateTo(1f, animationSpec = tween(500))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo + Title
            Column(
                modifier = Modifier
                    .offset(y = logoOffsetY.value.dp)
                    .graphicsLayer { alpha = logoAlpha.value },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_fintrack_logo),
                    contentDescription = "FinTrack Logo",
                    modifier = Modifier.size(150.dp),
                    // ✅ Tint xanh cho khớp với chữ
                    colorFilter = ColorFilter.tint(Color(0xFF1D4ED8))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "FinTrack",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8)
                )
            }

            // Buttons
            Column(
                modifier = Modifier
                    .offset(y = buttonsOffsetY.value.dp)
                    .graphicsLayer { alpha = buttonsAlpha.value }
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                Button(
                    onClick = onSignupClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4E8D4)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Đăng ký", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(onClick = onForgotPasswordClick) {
                    Text(
                        "Quên mật khẩu?",
                        fontSize = 14.sp,
                        color = Color(0xFF1D4ED8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}