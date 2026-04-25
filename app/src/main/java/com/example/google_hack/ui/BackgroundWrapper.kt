package com.example.google_hack.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.google_hack.R

@Composable
fun BackgroundWrapper(
    isTransitioning: Boolean = false,
    content: @Composable () -> Unit
) {
    // Animation states
    val scale by animateFloatAsState(
        targetValue = if (isTransitioning) 6f else 1.5f, // Made bigger
        animationSpec = tween(durationMillis = 1000),
        label = "LogoScale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (isTransitioning) 0f else 0.15f,
        animationSpec = tween(durationMillis = 1000),
        label = "LogoAlpha"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isTransitioning) 0f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "ContentAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Shared Background Logo
        Image(
            painter = painterResource(id = R.drawable.tempo_logo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .alpha(logoAlpha),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )
        
        // Screen Content
        Box(modifier = Modifier
            .fillMaxSize()
            .alpha(contentAlpha)
        ) {
            content()
        }
    }
}
