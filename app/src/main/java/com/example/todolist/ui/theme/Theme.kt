package com.example.todolist.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/* =============================================================
 *  TodoAppTheme
 *  -----------------------------------------------------------
 *  Single dark-navy theme for the whole app. We register the
 *  pastel accents into Material 3's color slots so default
 *  components (FAB, ripple, focused borders) stay on-brand.
 *
 *  Status / nav bars are painted to match DarkNavy so the
 *  edge-to-edge canvas looks seamless. Light icons on dark.
 * ============================================================= */

private val NavyColorScheme = darkColorScheme(
    /* Primary = Completed-Green accent (used by FAB, focus rings) */
    primary              = CompletedGreenAccent,
    onPrimary            = Color.White,
    primaryContainer     = CompletedGreen,
    onPrimaryContainer   = TextOnLightPrimary,

    /* Secondary = Running-Purple accent */
    secondary            = RunningPurpleAccent,
    onSecondary          = Color.White,
    secondaryContainer   = RunningPurple,
    onSecondaryContainer = TextOnLightPrimary,

    /* Tertiary = Rejected/Blue accent */
    tertiary             = RejectedBlueAccent,
    onTertiary           = Color.White,
    tertiaryContainer    = RejectedBlue,
    onTertiaryContainer  = TextOnLightPrimary,

    /* The canvas */
    background           = DarkNavy,
    onBackground         = TextOnDarkPrimary,

    /* Raised surfaces (sheets, dialogs) */
    surface              = DarkNavyElevated,
    onSurface            = TextOnDarkPrimary,
    surfaceVariant       = NavyOverlay,
    onSurfaceVariant     = TextOnDarkSecondary,

    /* Errors */
    error                = CancelledRedAccent,
    onError              = Color.White,
    errorContainer       = CancelledRed,
    onErrorContainer     = TextOnLightPrimary,

    outline              = DarkNavyMuted,
    outlineVariant       = DividerOnDark
)

@Composable
fun TodoAppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Paint the system bars to match the dark canvas.
            window.statusBarColor     = DarkNavy.toArgb()
            window.navigationBarColor = DarkNavy.toArgb()

            // Light icons on the dark bars.
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars     = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = NavyColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
