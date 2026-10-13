package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

fun getPaletteColorScheme(palette: AppThemePalette, isDark: Boolean): ColorScheme {
    return when (palette) {
        AppThemePalette.ROYAL_VIOLET -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF818CF8),
                onPrimary = Color(0xFF1E1B4B),
                primaryContainer = Color(0xFF312E81),
                onPrimaryContainer = Color(0xFFE0E7FF),
                secondary = Color(0xFFA78BFA),
                onSecondary = Color(0xFF2E1065),
                secondaryContainer = Color(0xFF4C1D95),
                onSecondaryContainer = Color(0xFFEDE9FE),
                tertiary = Color(0xFF38BDF8),
                background = Color(0xFF0F172A),
                surface = Color(0xFF1E293B),
                surfaceVariant = Color(0xFF334155),
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF8FAFC),
                onSurfaceVariant = Color(0xFFCBD5E1),
                outline = Color(0xFF475569),
                outlineVariant = Color(0xFF334155)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF4F46E5),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEEF2FF),
                onPrimaryContainer = Color(0xFF1E1B4B),
                secondary = Color(0xFF7C3AED),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFF5F3FF),
                onSecondaryContainer = Color(0xFF4C1D95),
                tertiary = Color(0xFF0284C7),
                background = Color(0xFFF8FAFC),
                surface = Color.White,
                surfaceVariant = Color(0xFFF1F5F9),
                onBackground = TextPrimary,
                onSurface = TextPrimary,
                onSurfaceVariant = TextSecondary,
                outline = SlateBorder,
                outlineVariant = Color(0xFFE2E8F0)
            )
        }

        AppThemePalette.EMERALD_PRO -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF34D399),
                onPrimary = Color(0xFF022C22),
                primaryContainer = Color(0xFF064E3B),
                onPrimaryContainer = Color(0xFFA7F3D0),
                secondary = Color(0xFF2DD4BF),
                onSecondary = Color(0xFF042F2E),
                secondaryContainer = Color(0xFF134E4A),
                onSecondaryContainer = Color(0xFF99F6E4),
                tertiary = Color(0xFF38BDF8),
                background = Color(0xFF061A14),
                surface = Color(0xFF0C2920),
                surfaceVariant = Color(0xFF133E31),
                onBackground = Color(0xFFECFDF5),
                onSurface = Color(0xFFECFDF5),
                onSurfaceVariant = Color(0xFFA7F3D0),
                outline = Color(0xFF1F5142),
                outlineVariant = Color(0xFF133E31)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF059669),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFECFDF5),
                onPrimaryContainer = Color(0xFF064E3B),
                secondary = Color(0xFF0D9488),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFF0FDFA),
                onSecondaryContainer = Color(0xFF134E4A),
                tertiary = Color(0xFF0284C7),
                background = Color(0xFFF8FAFC),
                surface = Color.White,
                surfaceVariant = Color(0xFFF0FDF4),
                onBackground = TextPrimary,
                onSurface = TextPrimary,
                onSurfaceVariant = TextSecondary,
                outline = Color(0xFFD1FAE5),
                outlineVariant = Color(0xFFE2E8F0)
            )
        }

        AppThemePalette.OCEAN_BLUE -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF38BDF8),
                onPrimary = Color(0xFF082F49),
                primaryContainer = Color(0xFF0C4A6E),
                onPrimaryContainer = Color(0xFFBAE6FD),
                secondary = Color(0xFF7DD3FC),
                onSecondary = Color(0xFF075985),
                secondaryContainer = Color(0xFF0369A1),
                onSecondaryContainer = Color(0xFFE0F2FE),
                tertiary = Color(0xFF818CF8),
                background = Color(0xFF081827),
                surface = Color(0xFF0F2942),
                surfaceVariant = Color(0xFF163B5D),
                onBackground = Color(0xFFF0F9FF),
                onSurface = Color(0xFFF0F9FF),
                onSurfaceVariant = Color(0xFFBAE6FD),
                outline = Color(0xFF234E78),
                outlineVariant = Color(0xFF163B5D)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF0284C7),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFF0F9FF),
                onPrimaryContainer = Color(0xFF0C4A6E),
                secondary = Color(0xFF0EA5E9),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE0F2FE),
                onSecondaryContainer = Color(0xFF0369A1),
                tertiary = Color(0xFF6366F1),
                background = Color(0xFFF8FAFC),
                surface = Color.White,
                surfaceVariant = Color(0xFFF0F9FF),
                onBackground = TextPrimary,
                onSurface = TextPrimary,
                onSurfaceVariant = TextSecondary,
                outline = Color(0xFFBAE6FD),
                outlineVariant = Color(0xFFE2E8F0)
            )
        }

        AppThemePalette.SUNSET_CORAL -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFFB923C),
                onPrimary = Color(0xFF431407),
                primaryContainer = Color(0xFF7C2D12),
                onPrimaryContainer = Color(0xFFFFEDD5),
                secondary = Color(0xFFFBBF24),
                onSecondary = Color(0xFF451A03),
                secondaryContainer = Color(0xFF78350F),
                onSecondaryContainer = Color(0xFFFEF3C7),
                tertiary = Color(0xFFFB7185),
                background = Color(0xFF1C130E),
                surface = Color(0xFF2C1E16),
                surfaceVariant = Color(0xFF3E2B20),
                onBackground = Color(0xFFFFF7ED),
                onSurface = Color(0xFFFFF7ED),
                onSurfaceVariant = Color(0xFFFFEDD5),
                outline = Color(0xFF563E30),
                outlineVariant = Color(0xFF3E2B20)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFEA580C),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFF7ED),
                onPrimaryContainer = Color(0xFF7C2D12),
                secondary = Color(0xFFF59E0B),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFEF3C7),
                onSecondaryContainer = Color(0xFF78350F),
                tertiary = Color(0xFFE11D48),
                background = Color(0xFFFAFAF9),
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF7ED),
                onBackground = TextPrimary,
                onSurface = TextPrimary,
                onSurfaceVariant = TextSecondary,
                outline = Color(0xFFFFEDD5),
                outlineVariant = Color(0xFFE7E5E4)
            )
        }

        AppThemePalette.MIDNIGHT_NEON -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF22D3EE),
                onPrimary = Color(0xFF083344),
                primaryContainer = Color(0xFF164E63),
                onPrimaryContainer = Color(0xFFCFFAFE),
                secondary = Color(0xFFA78BFA),
                onSecondary = Color(0xFF2E1065),
                secondaryContainer = Color(0xFF4C1D95),
                onSecondaryContainer = Color(0xFFEDE9FE),
                tertiary = Color(0xFF4ADE80),
                background = Color(0xFF050811),
                surface = Color(0xFF0D1527),
                surfaceVariant = Color(0xFF16223B),
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF8FAFC),
                onSurfaceVariant = Color(0xFFCBD5E1),
                outline = Color(0xFF22355A),
                outlineVariant = Color(0xFF16223B)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF0891B2),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFECFEFF),
                onPrimaryContainer = Color(0xFF164E63),
                secondary = Color(0xFF4F46E5),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFEEF2FF),
                onSecondaryContainer = Color(0xFF1E1B4B),
                tertiary = Color(0xFF059669),
                background = Color(0xFFF8FAFC),
                surface = Color.White,
                surfaceVariant = Color(0xFFECFEFF),
                onBackground = TextPrimary,
                onSurface = TextPrimary,
                onSurfaceVariant = TextSecondary,
                outline = Color(0xFFCFFAFE),
                outlineVariant = Color(0xFFE2E8F0)
            )
        }
    }
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    themePalette: AppThemePalette = AppThemePalette.ROYAL_VIOLET,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getPaletteColorScheme(themePalette, isDark)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
