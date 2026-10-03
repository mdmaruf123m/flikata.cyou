package com.example.ui.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

data class BackgroundColorItem(
    val id: String,
    val name: String,
    val color: Color
)

data class BackgroundGradientItem(
    val id: String,
    val name: String,
    val colors: List<Color>
) {
    val brush: Brush get() = Brush.linearGradient(colors)
}

object PostBackgroundHelper {
    // Rich list of Single Solid Colors (উপরের লাইনে শুধুই সিঙ্গেল কালার)
    val singleColors = listOf(
        BackgroundColorItem("solid_black", "Black", Color(0xFF121212)),
        BackgroundColorItem("solid_red", "Red", Color(0xFFE53935)),
        BackgroundColorItem("solid_crimson", "Crimson", Color(0xFFC2185B)),
        BackgroundColorItem("solid_pink", "Pink", Color(0xFFE91E63)),
        BackgroundColorItem("solid_purple", "Purple", Color(0xFF7B1FA2)),
        BackgroundColorItem("solid_deep_purple", "Deep Purple", Color(0xFF512DA8)),
        BackgroundColorItem("solid_indigo", "Indigo", Color(0xFF303F9F)),
        BackgroundColorItem("solid_blue", "Blue", Color(0xFF1976D2)),
        BackgroundColorItem("solid_light_blue", "Light Blue", Color(0xFF0288D1)),
        BackgroundColorItem("solid_cyan", "Cyan", Color(0xFF0097A7)),
        BackgroundColorItem("solid_teal", "Teal", Color(0xFF00796B)),
        BackgroundColorItem("solid_green", "Green", Color(0xFF2E7D32)),
        BackgroundColorItem("solid_light_green", "Light Green", Color(0xFF689F38)),
        BackgroundColorItem("solid_amber", "Amber", Color(0xFFFFA000)),
        BackgroundColorItem("solid_orange", "Orange", Color(0xFFF57C00)),
        BackgroundColorItem("solid_deep_orange", "Deep Orange", Color(0xFFE64A19)),
        BackgroundColorItem("solid_brown", "Brown", Color(0xFF5D4037)),
        BackgroundColorItem("solid_grey", "Slate Grey", Color(0xFF455A64)),
        BackgroundColorItem("solid_midnight", "Midnight Navy", Color(0xFF0D1B2A)),
        BackgroundColorItem("solid_emerald", "Emerald", Color(0xFF0A5C36)),
        BackgroundColorItem("solid_gold", "Dark Gold", Color(0xFFB8860B)),
        BackgroundColorItem("solid_ruby", "Ruby", Color(0xFF8B0000))
    )

    // Rich list of Multi-color Gradients (নিচের লাইনে গ্রেডিয়েন্ট কালার)
    val gradientColors = listOf(
        BackgroundGradientItem("gradient_sunset", "Sunset Fire", listOf(Color(0xFFFF512F), Color(0xFFDD2476))),
        BackgroundGradientItem("gradient_ocean_blue", "Ocean Blue", listOf(Color(0xFF2196F3), Color(0xFF002244))),
        BackgroundGradientItem("gradient_neon_purple", "Neon Purple", listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
        BackgroundGradientItem("gradient_lush_emerald", "Lush Emerald", listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
        BackgroundGradientItem("gradient_fire_amber", "Fire Amber", listOf(Color(0xFFFF8008), Color(0xFFFFC837))),
        BackgroundGradientItem("gradient_royal_violet", "Royal Violet", listOf(Color(0xFF654EA3), Color(0xFFEAAFC8))),
        BackgroundGradientItem("gradient_deep_space", "Deep Space", listOf(Color(0xFF000428), Color(0xFF004E92))),
        BackgroundGradientItem("gradient_mystic_teal", "Mystic Teal", listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))),
        BackgroundGradientItem("gradient_cherry_blossom", "Cherry Blossom", listOf(Color(0xFFEB3349), Color(0xFFF45C43))),
        BackgroundGradientItem("gradient_midnight_city", "Midnight City", listOf(Color(0xFF232526), Color(0xFF414345))),
        BackgroundGradientItem("gradient_cyberpunk", "Cyberpunk Magenta", listOf(Color(0xFFFF007F), Color(0xFF7928CA))),
        BackgroundGradientItem("gradient_sun_glory", "Sun Glory", listOf(Color(0xFFFF9900), Color(0xFFFF5E62))),
        BackgroundGradientItem("gradient_cool_blues", "Cool Blues", listOf(Color(0xFF2193B0), Color(0xFF6DD5ED))),
        BackgroundGradientItem("gradient_aurora", "Aurora Green", listOf(Color(0xFF00F260), Color(0xFF0575E6))),
        BackgroundGradientItem("gradient_cosmic_fusion", "Cosmic Fusion", listOf(Color(0xFFC33764), Color(0xFF1D2671))),
        BackgroundGradientItem("gradient_peach_glow", "Peach Glow", listOf(Color(0xFFED4264), Color(0xFFFFEDBC))),
        BackgroundGradientItem("gradient_dark_violet", "Dark Violet", listOf(Color(0xFF4776E6), Color(0xFF8E54E9))),
        BackgroundGradientItem("gradient_hyper_blue", "Flikata Blue", listOf(Color(0xFF1877F2), Color(0xFF003B95)))
    )

    fun getBrush(styleId: String?): Brush {
        if (styleId.isNullOrBlank()) {
            return Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF003B95)))
        }

        // Check if it matches a Single Solid Color
        val single = singleColors.find { it.id == styleId }
        if (single != null) {
            return Brush.linearGradient(listOf(single.color, single.color))
        }

        // Check if it matches a Gradient
        val gradient = gradientColors.find { it.id == styleId }
        if (gradient != null) {
            return gradient.brush
        }

        // Backward compatibility mappings
        return when (styleId) {
            "orange" -> Brush.linearGradient(listOf(Color(0xFFFF512F), Color(0xFFDD2476)))
            "gradient_blue" -> Brush.linearGradient(listOf(Color(0xFF2196F3), Color(0xFF002244)))
            "gradient_purple" -> Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
            "gradient_teal" -> Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))
            "black", "solid_black" -> Brush.linearGradient(listOf(Color(0xFF121212), Color(0xFF121212)))
            "teal" -> Brush.linearGradient(listOf(Color(0xFF00796B), Color(0xFF00796B)))
            "crimson" -> Brush.linearGradient(listOf(Color(0xFFC2185B), Color(0xFFC2185B)))
            else -> Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF003B95)))
        }
    }
}
