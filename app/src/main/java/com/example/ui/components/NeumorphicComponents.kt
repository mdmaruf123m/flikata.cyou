package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Distinct Neumorphic Clay / Glass Soft Colors matching both Light and Dark themes
object SoftUITheme {
    val isDark: Boolean
        @Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.surface.let {
            (it.red * 0.299f + it.green * 0.587f + it.blue * 0.114f) < 0.5f
        }

    val BackgroundGradientTopLight = Color(0xFFF4F8FD)
    val BackgroundGradientBottomLight = Color(0xFFE8F1FC)
    val BackgroundGradientTopDark = Color(0xFF121418)
    val BackgroundGradientBottomDark = Color(0xFF181A20)

    val BackgroundGradientTop: Color
        @Composable get() = if (isDark) BackgroundGradientTopDark else BackgroundGradientTopLight

    val BackgroundGradientBottom: Color
        @Composable get() = if (isDark) BackgroundGradientBottomDark else BackgroundGradientBottomLight

    val SurfaceSoftLight = Color(0xFFE9F1FC)
    val SurfaceSoftDark = Color(0xFF1E2128)
    val SurfaceSoft: Color
        @Composable get() = if (isDark) SurfaceSoftDark else SurfaceSoftLight

    val SurfaceCardLight = Color(0xFFFFFFFF)
    val SurfaceCardDark = Color(0xFF242730)
    val SurfaceCard: Color
        @Composable get() = if (isDark) SurfaceCardDark else SurfaceCardLight

    // Outer shadow and Highlight for 3D Clay effect (clean and subtle, no harsh black band)
    val ShadowLightLight = Color(0xFFFFFFFF)
    val ShadowLightDark = Color(0x18FFFFFF)
    val ShadowLight: Color
        @Composable get() = if (isDark) ShadowLightDark else ShadowLightLight

    val ShadowDarkLight = Color(0x14000000)
    val ShadowDarkDark = Color(0x66000000)
    val ShadowDark: Color
        @Composable get() = if (isDark) ShadowDarkDark else ShadowDarkLight

    val BorderHighlightLight = Color(0xEEFFFFFF)
    val BorderHighlightDark = Color(0x22FFFFFF)
    val BorderHighlight: Color
        @Composable get() = if (isDark) BorderHighlightDark else BorderHighlightLight

    val BorderSubtleLight = Color(0x33A0C0E8)
    val BorderSubtleDark = Color(0x14FFFFFF)
    val BorderSubtle: Color
        @Composable get() = if (isDark) BorderSubtleDark else BorderSubtleLight

    // Vibrant Royal Blue Glow Gradient for primary buttons
    val ButtonBlueStart = Color(0xFF0075FF)
    val ButtonBlueEnd = Color(0xFF0052D4)
    val ButtonBlueGloss = Color(0x66FFFFFF)

    // Text & Icon Colors
    val TextDarkLight = Color(0xFF1E293B)
    val TextDarkDark = Color(0xFFF0F2F5)
    val TextDark: Color
        @Composable get() = if (isDark) TextDarkDark else TextDarkLight

    val TextSecondaryLight = Color(0xFF64748B)
    val TextSecondaryDark = Color(0xFF9EA3AE)
    val TextSecondary: Color
        @Composable get() = if (isDark) TextSecondaryDark else TextSecondaryLight

    val TextMutedLight = Color(0xFF94A3B8)
    val TextMutedDark = Color(0xFF6E7480)
    val TextMuted: Color
        @Composable get() = if (isDark) TextMutedDark else TextMutedLight

    val IconTintLight = Color(0xFF6B8AB2)
    val IconTintDark = Color(0xFF9EA3AE)
    val IconTint: Color
        @Composable get() = if (isDark) IconTintDark else IconTintLight

    val IconActiveBlue = Color(0xFF0075FF)
}

/**
 * High fidelity Neumorphic background with smooth ambient gradient & decorative 3D spheres
 */
@Composable
fun SoftNeumorphicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SoftUITheme.BackgroundGradientTop,
                        SoftUITheme.BackgroundGradientBottom
                    )
                )
            )
    ) {
        // Ambient 3D blurred geometric background circles matching screenshot
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-left soft cyan-blue ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x334A90E2), Color(0x054A90E2), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(0f, 0f)
            )
            drawCircle(
                color = Color(0x55FFFFFF),
                radius = w * 0.35f,
                center = Offset(0f, 0f),
                style = Stroke(width = 3.dp.toPx())
            )

            // Top-right soft bubble
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3060A5FA), Color(0x0560A5FA), Color.Transparent),
                    center = Offset(w, h * 0.22f),
                    radius = w * 0.35f
                ),
                radius = w * 0.35f,
                center = Offset(w, h * 0.22f)
            )
            drawCircle(
                color = Color(0x66FFFFFF),
                radius = w * 0.22f,
                center = Offset(w, h * 0.22f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Center small ambient floating ball
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x88FFFFFF), Color(0x3393C5FD), Color.Transparent),
                    center = Offset(w * 0.52f, h * 0.94f),
                    radius = 20.dp.toPx()
                ),
                radius = 16.dp.toPx(),
                center = Offset(w * 0.52f, h * 0.94f)
            )

            // Bottom-left big soft bubble
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4093C5FD), Color(0x0893C5FD), Color.Transparent),
                    center = Offset(w * 0.05f, h * 0.96f),
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = Offset(w * 0.05f, h * 0.96f)
            )
            drawCircle(
                color = Color(0x77FFFFFF),
                radius = w * 0.38f,
                center = Offset(w * 0.05f, h * 0.96f),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Bottom-right big soft bubble
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4093C5FD), Color(0x0893C5FD), Color.Transparent),
                    center = Offset(w * 0.95f, h * 0.98f),
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = Offset(w * 0.95f, h * 0.98f)
            )
            drawCircle(
                color = Color(0x77FFFFFF),
                radius = w * 0.38f,
                center = Offset(w * 0.95f, h * 0.98f),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        content()
    }
}

/**
 * Soft 3D extruded pill / card container with dual light & dark shadow layers
 */
@Composable
fun SoftNeumorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    elevation: Dp = 4.dp,
    backgroundColor: Color = Color.Unspecified,
    borderWidth: Dp = 1.2.dp,
    borderColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    val actualBg = if (backgroundColor != Color.Unspecified) backgroundColor else SoftUITheme.SurfaceCard
    val actualBorder = if (borderColor != Color.Unspecified) borderColor else SoftUITheme.BorderHighlight

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = SoftUITheme.ShadowDark,
                spotColor = SoftUITheme.ShadowDark
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SoftUITheme.ShadowLight.copy(alpha = if (SoftUITheme.isDark) 0.15f else 0.85f),
                        actualBg.copy(alpha = 0.95f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(200f, 200f)
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        actualBorder,
                        if (SoftUITheme.isDark) Color(0x11FFFFFF) else Color(0x22FFFFFF),
                        if (SoftUITheme.isDark) Color(0x08FFFFFF) else Color(0x33A0C0E8)
                    )
                ),
                shape = shape
            )
            .then(clickModifier)
    ) {
        content()
    }
}

/**
 * Soft 3D Neumorphic Input Field with leading icon, placeholder, and smooth rounded pill
 */
@Composable
fun SoftNeumorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    var passwordVisible = remember { androidx.compose.runtime.mutableStateOf(false) }

    SoftNeumorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF1F6FD)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = SoftUITheme.IconTint,
                        modifier = Modifier.size(22.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = SoftUITheme.TextMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = singleLine,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        textStyle = TextStyle(
                            color = SoftUITheme.TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(SoftUITheme.IconActiveBlue),
                        visualTransformation = if (isPassword && !passwordVisible.value) {
                            PasswordVisualTransformation()
                        } else VisualTransformation.None,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isPassword) {
                    IconButton(
                        onClick = { passwordVisible.value = !passwordVisible.value },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = SoftUITheme.IconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Primary Glossy Blue Action Button with Arrow forward circle icon
 */
@Composable
fun SoftPrimaryBlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    trailingIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(29.dp),
                ambientColor = Color(0x880066FF),
                spotColor = Color(0xAA0052D4)
            )
            .clip(RoundedCornerShape(29.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        SoftUITheme.ButtonBlueStart,
                        SoftUITheme.ButtonBlueEnd
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x99FFFFFF),
                        Color(0x22FFFFFF),
                        Color(0x11000000)
                    )
                ),
                shape = RoundedCornerShape(29.dp)
            )
            .clickable(
                enabled = !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner top glossy reflection highlight
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .align(Alignment.TopCenter)
        ) {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x44FFFFFF), Color(0x00FFFFFF))
                ),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(29.dp.toPx(), 29.dp.toPx())
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(26.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Right circular arrow container
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Secondary Soft Neumorphic Outline Pill Button (e.g. "Create New Account ->")
 */
@Composable
fun SoftSecondaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    SoftNeumorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = 6.dp,
        borderWidth = 1.5.dp,
        borderColor = Color(0x994A90E2),
        backgroundColor = Color(0xFFF1F6FD),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = SoftUITheme.IconActiveBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                }
                Text(
                    text = text,
                    color = SoftUITheme.IconActiveBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right circular arrow container
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x1F0075FF))
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = SoftUITheme.IconActiveBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
