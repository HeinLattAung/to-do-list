package com.example.todolist.ui.theme

import androidx.compose.ui.graphics.Color

/* =============================================================
 *  PALETTE — single source of truth for the whole app.
 *
 *  Dark-navy canvas with three pastel card families:
 *    Completed = Light green
 *    Running   = Light purple
 *    Rejected  = Sky blue
 *
 *  Each family has a *card background* (soft pastel) and an
 *  *accent* (deeper saturated tone) used by the status pill,
 *  progress bar and percentage label.
 * ============================================================= */

/* ---------- Surface / canvas ---------- */
val DarkNavy             = Color(0xFF0F172A)   // app background
val DarkNavyElevated     = Color(0xFF1E293B)   // raised surfaces (sheets, dialogs)
val DarkNavyMuted        = Color(0xFF334155)   // borders, dividers on dark
val NavyOverlay          = Color(0xFF111B33)   // soft lift behind sections

/* ---------- Status: Completed (green) ---------- */
val CompletedGreen       = Color(0xFFD9E9A4)   // card background
val CompletedGreenAccent = Color(0xFFB5C96D)   // pill / progress / %

/* ---------- Status: Running (purple) ---------- */
val RunningPurple        = Color(0xFFD6C8E8)
val RunningPurpleAccent  = Color(0xFFB19DCD)

/* ---------- Status: Rejected / Pending (blue) ---------- */
val RejectedBlue         = Color(0xFF63D3F2)
val RejectedBlueAccent   = Color(0xFF4ABBD9)

/* ---------- Status: Cancelled (red) — kept for completeness ---------- */
val CancelledRed         = Color(0xFFFFBDBD)
val CancelledRedAccent   = Color(0xFFE05656)

/* ---------- Text on dark canvas ---------- */
val TextOnDarkPrimary    = Color(0xFFF8FAFC)   // headlines on navy
val TextOnDarkSecondary  = Color(0xFFCBD5E1)   // body on navy
val TextOnDarkTertiary   = Color(0xFF94A3B8)   // muted labels (e.g. day-of-week)

/* ---------- Text on pastel cards (dark navy reads well on light pastels) ---------- */
val TextOnLightPrimary   = Color(0xFF0F172A)
val TextOnLightSecondary = Color(0xFF334155)

/* ---------- Misc ---------- */
val DividerOnDark        = Color(0x33FFFFFF)   // 20% white
val DashedTimeline       = Color(0xFFCBD5E1)   // dashed line in time column
val AvatarBorder         = Color.White

/* =============================================================
 *  StatusPalette helper — pairs background, accent, label so the
 *  card composable can call task.status.palette() and pull all
 *  three at once.
 * ============================================================= */
data class StatusPalette(val background: Color, val accent: Color, val label: String)

object StatusPalettes {
    val Completed = StatusPalette(CompletedGreen, CompletedGreenAccent, "Completed")
    val Running   = StatusPalette(RunningPurple,  RunningPurpleAccent,  "Running")
    val Pending   = StatusPalette(RejectedBlue,   RejectedBlueAccent,   "Pending")
    val Cancelled = StatusPalette(CancelledRed,   CancelledRedAccent,   "Rejected")
}
