package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TableBoxColorScheme = darkColorScheme(
    primary = ImperialGold,
    onPrimary = LoungeObsidian,
    primaryContainer = Color(0xFF3E3016),
    onPrimaryContainer = Color(0xFFFFE5B4),
    secondary = KyotoSakura,
    onSecondary = LoungeObsidian,
    secondaryContainer = Color(0xFF42212E),
    onSecondaryContainer = Color(0xFFFFD8E2),
    tertiary = JadeEmerald,
    onTertiary = LoungeObsidian,
    tertiaryContainer = Color(0xFF15382C),
    onTertiaryContainer = Color(0xFFBDF5DE),
    background = LoungeObsidian,
    onBackground = IvoryText,
    surface = LoungeSurface,
    onSurface = IvoryText,
    surfaceVariant = LoungeSurfaceElevated,
    onSurfaceVariant = MutedText,
    outline = LoungeOutline,
    outlineVariant = Color(0xFF2F2B3E),
    error = LanternVermilion,
    onError = Color.White,
    errorContainer = Color(0xFF4A161B),
    onErrorContainer = Color(0xFFFFD6D9)
)

private val TableBoxShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TableBoxTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TableBoxColorScheme,
        typography = Typography,
        shapes = TableBoxShapes,
        content = content
    )
}
