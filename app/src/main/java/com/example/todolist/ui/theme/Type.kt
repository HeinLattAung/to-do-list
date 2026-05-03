package com.example.todolist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.todolist.R

/* =============================================================
 *  TYPOGRAPHY — clean, modern, slightly compact.
 *
 *  Recommended pairing (in order of fit for this design):
 *      1. Inter             — neutral, highly legible
 *      2. Plus Jakarta Sans — friendly, slightly rounded
 *      3. Manrope           — geometric, modern
 *
 *  HOW TO USE A REAL FONT:
 *  --------------------------------------------------------
 *  1. Drop the .ttf files into  app/src/main/res/font/
 *       inter_regular.ttf
 *       inter_medium.ttf
 *       inter_semibold.ttf
 *       inter_bold.ttf
 *  2. Uncomment the InterFamily block below.
 *  3. Replace `DisplayFamily = FontFamily.SansSerif`
 *           with `DisplayFamily = InterFamily`.
 *
 *  Until then the styles fall back to the system sans-serif,
 *  so the app compiles cleanly out of the box.
 * ============================================================= */

/* ---- Custom font example (uncomment + add files to res/font/) ----

private val InterFamily = FontFamily(
    Font(R.font.inter_regular,  FontWeight.Normal),
    Font(R.font.inter_medium,   FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold,     FontWeight.Bold)
)
*/

private val DisplayFamily: FontFamily = FontFamily.SansSerif   // ← swap to InterFamily
private val BodyFamily:    FontFamily = FontFamily.SansSerif

val AppTypography = Typography(

    /* ----- Display: hero numbers & big screen titles ----- */
    displayLarge  = TextStyle(
        fontFamily    = DisplayFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 36.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily    = DisplayFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = (-0.4).sp
    ),

    /* ----- Headline: "My Tasks" header on the home screen ----- */
    headlineLarge  = TextStyle(
        fontFamily    = DisplayFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = DisplayFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp
    ),
    headlineSmall  = TextStyle(
        fontFamily    = DisplayFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 18.sp,
        lineHeight    = 24.sp
    ),

    /* ----- Title: card titles, sheet headers ----- */
    titleLarge  = TextStyle(
        fontFamily    = DisplayFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 18.sp,
        lineHeight    = 22.sp
    ),
    titleMedium = TextStyle(           // task title weight matches the design
        fontFamily    = DisplayFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 22.sp
    ),
    titleSmall  = TextStyle(
        fontFamily    = DisplayFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp
    ),

    /* ----- Body: descriptions, list items ----- */
    bodyLarge  = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall  = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp
    ),

    /* ----- Label: chips, buttons, time strings ----- */
    labelLarge  = TextStyle(
        fontFamily    = BodyFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = BodyFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall  = TextStyle(
        fontFamily    = BodyFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.3.sp
    )
)
