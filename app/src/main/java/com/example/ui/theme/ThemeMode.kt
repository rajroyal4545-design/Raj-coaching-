package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val titleEn: String, val titleHi: String) {
    SYSTEM("System Default", "सिस्टम अनुसार"),
    LIGHT("Light Mode", "लाइट मोड ☀️"),
    DARK("Dark Mode", "डार्क मोड 🌙")
}

enum class AppThemePalette(
    val titleEn: String,
    val titleHi: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val iconEmoji: String
) {
    ROYAL_VIOLET(
        titleEn = "Royal Violet",
        titleHi = "शाही वॉयलेट",
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFF8B5CF6),
        iconEmoji = "💜"
    ),
    EMERALD_PRO(
        titleEn = "Emerald Mint",
        titleHi = "एमराल्ड मिंट",
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF10B981),
        iconEmoji = "🌿"
    ),
    OCEAN_BLUE(
        titleEn = "Ocean Sapphire",
        titleHi = "ओशन सफायर",
        primaryColor = Color(0xFF0284C7),
        secondaryColor = Color(0xFF06B6D4),
        iconEmoji = "🌊"
    ),
    SUNSET_CORAL(
        titleEn = "Sunset Amber",
        titleHi = "सनसेट एम्बर",
        primaryColor = Color(0xFFEA580C),
        secondaryColor = Color(0xFFF59E0B),
        iconEmoji = "🌅"
    ),
    MIDNIGHT_NEON(
        titleEn = "Midnight Neon",
        titleHi = "मिडनाइट नीयन",
        primaryColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF14B8A6),
        iconEmoji = "🖤"
    )
}

