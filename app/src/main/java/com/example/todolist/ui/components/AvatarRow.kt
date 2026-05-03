package com.example.todolist.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/* =============================================================
 *  AvatarRow — overlapping circular profile images via Coil.
 *
 *  Drop-in replacement for the painterResource-based
 *  OverlappingAvatars previously used in TaskCard.
 *
 *  build.gradle.kts:
 *      implementation("io.coil-kt:coil-compose:2.6.0")
 *
 *  AndroidManifest.xml:
 *      <uses-permission android:name="android.permission.INTERNET" />
 *
 *  Usage:
 *      AvatarRow(
 *          avatarUrls = listOf(
 *              "https://i.pravatar.cc/64?img=12",
 *              "https://i.pravatar.cc/64?img=33",
 *              "https://i.pravatar.cc/64?img=47",
 *              "https://i.pravatar.cc/64?img=51",
 *              "https://i.pravatar.cc/64?img=58"
 *          )
 *      )
 * ============================================================= */

@Composable
fun AvatarRow(
    avatarUrls: List<String>,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 28.dp,
    overlap: Dp = 10.dp,
    borderColor: Color = Color.White,
    borderWidth: Dp = 2.dp,
    maxVisible: Int = 4,
    placeholderColor: Color = Color(0xFFEFEFEF),
    overflowChipColor: Color = Color(0xFFEFEFEF),
    overflowTextColor: Color = Color(0xFF334155)
) {
    Box(modifier = modifier) {

        /* ---------- Visible avatars ---------- */
        avatarUrls.take(maxVisible).forEachIndexed { i, url ->
            Surface(
                modifier        = Modifier
                    .padding(start = (avatarSize - overlap) * i)
                    .size(avatarSize),
                shape           = CircleShape,
                color           = borderColor,                       // border ring
                shadowElevation = 1.dp,
                border          = BorderStroke(borderWidth, borderColor)
            ) {
                AsyncImage(
                    model              = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Member avatar",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(placeholderColor)
                )
            }
        }

        /* ---------- Overflow "+N" chip ---------- */
        if (avatarUrls.size > maxVisible) {
            Surface(
                modifier = Modifier
                    .padding(start = (avatarSize - overlap) * maxVisible)
                    .size(avatarSize),
                shape    = CircleShape,
                color    = overflowChipColor,
                border   = BorderStroke(borderWidth, borderColor)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text       = "+${avatarUrls.size - maxVisible}",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = overflowTextColor
                    )
                }
            }
        }
    }
}
