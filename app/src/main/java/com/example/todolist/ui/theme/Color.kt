package com.example.todolist.ui.theme

import androidx.compose.ui.graphics.Color

/* =============================================================
 *  PALETTE — premium dark theme.
 *
 *  Surfaces: deep navy/charcoal.
 *  Accents : Mint, Cyan, Lavender — used as both the chip color
 *            on a card AND as the accent (filled progress bar,
 *            FAB, primary button).
 *  Text    : near-white primary, soft gray secondary.
 * ============================================================= */

/* ---------- Surfaces ---------- */
val BgPrimary        = Color(0xFF111827)   // app canvas
val BgElevated       = Color(0xFF1F2937)   // sheets, dialogs
val BgInput          = Color(0xFF1F2937)   // text-field fill (unfocused)
val BgInputFocused   = Color(0xFF26303F)   // text-field fill (focused)
val BgChip           = Color(0xFF1F2937)   // unselected status chip
val BgCard           = Color(0xFF1A2231)   // task card background

/* ---------- Borders / dividers ---------- */
val BorderSubtle     = Color(0xFF2B3445)   // 1dp outline on inputs / chips
val BorderFocused    = Color(0xFF3B475D)
val DividerOnDark    = Color(0x33FFFFFF)

/* ---------- Accent: Mint (Completed / primary CTA) ---------- */
val MintGreen        = Color(0xFFC1FF72)
val MintGreenDim     = Color(0xFF9BD050)
val OnMint           = Color(0xFF0B1220)

/* ---------- Accent: Cyan (Rejected / Pending) ---------- */
val Cyan             = Color(0xFF00E5FF)
val CyanDim          = Color(0xFF00B8CC)
val OnCyan           = Color(0xFF0B1220)

/* ---------- Accent: Lavender (Running) ---------- */
val Lavender         = Color(0xFFE1BEE7)
val LavenderDim      = Color(0xFFB89BBE)
val OnLavender       = Color(0xFF0B1220)

/* ---------- Accent: Coral (Cancelled / destructive) ---------- */
val Coral            = Color(0xFFFF8A8A)
val CoralDim         = Color(0xFFD66E6E)

/* ---------- Text on dark ---------- */
val TextPrimary      = Color(0xFFF9FAFB)
val TextSecondary    = Color(0xFFB8C0CC)
val TextTertiary     = Color(0xFF8B95A4)
val TextOnAccent     = Color(0xFF0B1220)

/* ---------- Status palette helper ---------- */
data class StatusPalette(val accent: Color, val onAccent: Color, val label: String)

object StatusPalettes {
    val Completed = StatusPalette(MintGreen, OnMint,     "Completed")
    val Running   = StatusPalette(Lavender, OnLavender,  "Running")
    val Pending   = StatusPalette(Cyan,     OnCyan,      "Pending")
    val Cancelled = StatusPalette(Coral,    Color.White, "Rejected")
}
