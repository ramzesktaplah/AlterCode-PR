package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Jetpack Compose implementation of the RadialGlowButton component.
 * Features a multi-stop deep radial gradient background, animated shine sweep,
 * iridescent dual-tone border, press physics, and soft typography drop shadow.
 */
@Composable
fun RadialGlowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    minWidth: Dp = 160.dp,
    minHeight: Dp = 51.dp,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press scaling physics
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "radial_glow_button_scale"
    )

    // Interactive radial center shift matching CSS:
    // Normal: pos-x: 40%, pos-y: 140%
    // Pressed/Hover: pos-x: 0%, pos-y: 120%
    val posXFraction by animateFloatAsState(
        targetValue = if (isPressed) 0.15f else 0.40f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "radial_center_x"
    )
    val posYFraction by animateFloatAsState(
        targetValue = if (isPressed) 1.20f else 1.40f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "radial_center_y"
    )

    // Shimmer/shine sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "radial_glow_shine")
    val shineProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine_progress"
    )

    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )

    // Palette stops from CSS specifications:
    // Color 1: #000022 at 37.35%
    // Color 2: #1f3f6d at 61.36%
    // Color 3: #469396 at 78.42%
    // Color 4: #f1ffa5 at 93.52%
    // Color 5: hsl(250 80% 2.5%) -> #07031e at 100%
    val color1 = Color(0xFF000022)
    val color2 = Color(0xFF1F3F6D)
    val color3 = Color(0xFF469396)
    val color4 = Color(0xFFF1FFA5)
    val color5 = Color(0xFF07031E)

    // Border iridescent colors (180deg):
    // hsla(230, 75%, 90%, 0.7) -> Color(0xB3D6DEFB)
    // hsla(230, 50%, 90%, 0.25) -> Color(0x40D6DEFB)
    val borderColor1 = Color(0xB3D6DEFB)
    val borderColor2 = Color(0x40D6DEFB)

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                enabled = enabled,
                onClick = onClick
            )
            .drawWithCache {
                val width = size.width
                val height = size.height

                // Radial center and radius matching spread percentages
                val centerX = width * posXFraction
                val centerY = height * posYFraction
                val radius = maxOf(width * 1.35f, height * 1.75f)

                val radialBrush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to color1,
                        0.37f to color1,
                        0.61f to color2,
                        0.78f to color3,
                        0.93f to color4,
                        1.00f to color5
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                )

                // 180deg linear gradient border
                val borderBrush = Brush.verticalGradient(
                    colors = listOf(borderColor1, borderColor2)
                )

                // Moving shine beam
                val beamX = width * shineProgress
                val shineBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.22f * ambientPulse),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    start = Offset(beamX - 60f, 0f),
                    end = Offset(beamX + 60f, height)
                )

                onDrawBehind {
                    // 1. Draw base radial background
                    drawRect(brush = radialBrush)

                    // 2. Draw animated shine highlight
                    drawRect(brush = shineBrush, blendMode = BlendMode.Screen)

                    // 3. Draw dual-tone iridescent border
                    drawRect(
                        brush = borderBrush,
                        style = Stroke(width = 1.25.dp.toPx())
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProvideTextStyle(
                value = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xF2FFFFFF),
                    shadow = Shadow(
                        color = Color(0xF2000000),
                        blurRadius = 4f,
                        offset = Offset(0f, 1f)
                    )
                )
            ) {
                content()
            }
        }
    }
}
