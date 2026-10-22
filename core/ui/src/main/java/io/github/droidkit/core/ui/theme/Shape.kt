package io.github.droidkit.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Dark Instrument radii (v5 tokens: 8 small / 12 medium / 16 large / 20–24
 * hero). The mockup uses 10–12px on cards/inputs, 16px on dialogs, 18px top
 * corners on sheets, 14px on the hero card.
 */
val DroidKitShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // .res verdict pills, chips
    small = RoundedCornerShape(8.dp),        // .qic, .chip, small tiles
    medium = RoundedCornerShape(12.dp),      // .card, .field, icon tiles
    large = RoundedCornerShape(16.dp),       // .dialog, .preset
    extraLarge = RoundedCornerShape(24.dp),  // hero/bento surfaces
)
