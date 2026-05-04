package com.example.todolist.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/* =============================================================
 *  TodoAppTheme
 *  -----------------------------------------------------------
 *  Single dark theme for the whole app. Mint is the primary
 *  accent (CTAs, FAB, focus); Lavender + Cyan back the other
 *  status families. System bars match the canvas for a clean
 *  edge-to-edge look.
 * ============================================================= */

private val DarkColors = darkColorScheme(
    /* Primary = Mint Green — used by FAB, primary button, selection ring */
    primary              = MintGreen,
    onPrimary            = OnMint,
    primaryContainer     = MintGreenDim,
    onPrimaryContainer   = OnMint,

    /* Secondary = Lavender (Running family) */
    secondary            = Lavender,
    onSecondary          = OnLavender,
    secondaryContainer   = LavenderDim,
    onSecondaryContainer = OnLavender,

    /* Tertiary = Cyan (Pending / Rejected) */
    tertiary             = Cyan,
    onTertiary           = OnCyan,
    tertiaryContainer    = CyanDim,
    onTertiaryContainer  = OnCyan,

    /* Canvas */
    background           = BgPrimary,
    onBackground         = TextPrimary,

    /* Raised surfaces — sheets, dialogs, cards */
    surface              = BgElevated,
    onSurface            = TextPrimary,
    surfaceVariant       = BgInput,
    onSurfaceVariant     = TextSecondary,

    error                = Coral,
    onError              = TextPrimary,
    errorContainer       = CoralDim,
    onErrorContainer     = TextPrimary,

    outline              = BorderSubtle,
    outlineVariant       = DividerOnDark
)

@Composable
fun TodoAppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = BgPrimary.toArgb()
            window.navigationBarColor = BgPrimary.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars     = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColors,
        typography  = AppTypography,
        content     = content
    )
}
