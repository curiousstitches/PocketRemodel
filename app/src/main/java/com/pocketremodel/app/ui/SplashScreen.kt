package com.pocketremodel.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketremodel.app.ui.theme.BrandTeal
import kotlinx.coroutines.delay

/**
 * A premium loading screen: a glowing crest, the app name fading and rising into
 * place, a shimmering bar, then the whole screen dissolves into the app.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val contentAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }
    val rise = remember { Animatable(24f) }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.85f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        rise.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
        delay(1500)
        screenAlpha.animateTo(0f, tween(550, easing = LinearEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0B0F14), Color(0xFF111A22), Color(0xFF0B0F14)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = rise.value.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulse)
                        .alpha(0.35f * contentAlpha.value)
                        .background(BrandTeal, CircleShape)
                )
                Icon(
                    Icons.Filled.Chair, contentDescription = null, tint = Color.Black,
                    modifier = Modifier.size(56.dp).alpha(contentAlpha.value)
                )
            }

            Spacer(Modifier.height(28.dp))
            Text("Pocket Remodel", color = Color.White, fontSize = 30.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.alpha(contentAlpha.value))
            Spacer(Modifier.height(6.dp))
            Text("Redesign your world by talking to it", color = BrandTeal, fontSize = 14.sp,
                modifier = Modifier.alpha(contentAlpha.value))

            Spacer(Modifier.height(36.dp))
            ShimmerBar(alpha = contentAlpha.value)
        }
    }
}

@Composable
private fun ShimmerBar(alpha: Float) {
    val barWidth = 160.dp
    val shift by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = -60f, targetValue = 160f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "shift"
    )
    Box(
        modifier = Modifier
            .size(width = barWidth, height = 4.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(Color(0xFF1C2630))
    ) {
        Box(
            modifier = Modifier
                .offset(x = shift.dp)
                .size(width = 60.dp, height = 4.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color.Transparent, BrandTeal, Color.Transparent))
                )
        )
    }
}
