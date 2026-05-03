package com.example.taskcard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------------------------
 *  TaskStatus  -  drives the colour palette + label of the tab
 * ------------------------------------------------------------------ */
enum class TaskStatus(val label: String, val background: Color, val tabColor: Color) {
    COMPLETED(
        label       = "Completed",
        background  = Color(0xFFD9F2E3),   // soft pastel green
        tabColor    = Color(0xFF34A853)
    ),
    RUNNING(
        label       = "Running",
        background  = Color(0xFFD8E6FB),   // soft pastel blue
        tabColor    = Color(0xFF1A73E8)
    ),
    PENDING(
        label       = "Pending",
        background  = Color(0xFFE6DCF7),   // soft pastel purple
        tabColor    = Color(0xFF7B5BD6)
    ),
    CANCELLED(
        label       = "Cancelled",
        background  = Color(0xFFFFE0E0),   // soft pastel red
        tabColor    = Color(0xFFD93025)
    )
}

/* ------------------------------------------------------------------
 *  TaskTabShape - a rounded rectangle whose top-left corner has a
 *  small "tab" cut-out where the status pill will sit.
 *  -----------------------------------------------------------------
 *      ___________
 *  ___|tab|       \
 * |               |
 * |               |
 * |_______________|
 * ------------------------------------------------------------------ */
class TaskTabShape(
    private val cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    private val tabWidth: androidx.compose.ui.unit.Dp     = 110.dp,
    private val tabHeight: androidx.compose.ui.unit.Dp    = 32.dp,
    private val tabRadius: androidx.compose.ui.unit.Dp    = 16.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        with(density) {
            val r   = cornerRadius.toPx()
            val tw  = tabWidth.toPx()
            val th  = tabHeight.toPx()
            val tr  = tabRadius.toPx()

            val path = Path().apply {
                /* Start just to the right of the tab, at the very top */
                moveTo(tw, 0f)

                /* Top edge → top-right corner */
                lineTo(size.width - r, 0f)
                arcTo(
                    rect            = Rect(size.width - 2 * r, 0f, size.width, 2 * r),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo     = false
                )

                /* Right edge → bottom-right corner */
                lineTo(size.width, size.height - r)
                arcTo(
                    rect            = Rect(size.width - 2 * r, size.height - 2 * r, size.width, size.height),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo     = false
                )

                /* Bottom edge → bottom-left corner */
                lineTo(r, size.height)
                arcTo(
                    rect            = Rect(0f, size.height - 2 * r, 2 * r, size.height),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo     = false
                )

                /* Left edge up to where the tab begins */
                lineTo(0f, th + r)
                arcTo(
                    rect            = Rect(0f, th, 2 * r, th + 2 * r),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo     = false
                )

                /* Across the bottom of the tab */
                lineTo(tw - tr, th)
                arcTo(
                    rect            = Rect(tw - 2 * tr, th - 2 * tr, tw, th),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo     = false
                )

                /* Tab right edge up to the top */
                lineTo(tw, 0f)
                close()
            }
            return Outline.Generic(path)
        }
    }
}

/* ------------------------------------------------------------------
 *  Dashed vertical line used for the time-indicator column
 * ------------------------------------------------------------------ */
@Composable
private fun DashedVerticalLine(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray.copy(alpha = 0.6f)
) {
    Canvas(modifier = modifier.width(2.dp)) {
        drawLine(
            color       = color,
            start       = Offset(size.width / 2, 0f),
            end         = Offset(size.width / 2, size.height),
            strokeWidth = 2f,
            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    }
}

/* ------------------------------------------------------------------
 *  Overlapping avatars row
 * ------------------------------------------------------------------ */
@Composable
private fun OverlappingAvatars(
    avatars: List<Painter>,
    size: androidx.compose.ui.unit.Dp = 28.dp,
    overlap: androidx.compose.ui.unit.Dp = 10.dp,
    borderColor: Color = Color.White
) {
    Box {
        avatars.take(4).forEachIndexed { i, painter ->
            Surface(
                modifier  = Modifier
                    .padding(start = (size - overlap) * i)
                    .size(size),
                shape     = CircleShape,
                color     = borderColor,
                shadowElevation = 1.dp,
                border    = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
            ) {
                androidx.compose.foundation.Image(
                    painter           = painter,
                    contentDescription = "Member avatar",
                    modifier          = Modifier
                        .size(size)
                        .clip(CircleShape)
                )
            }
        }
        if (avatars.size > 4) {
            Surface(
                modifier = Modifier
                    .padding(start = (size - overlap) * 4)
                    .size(size),
                shape    = CircleShape,
                color    = Color(0xFFEFEFEF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text     = "+${avatars.size - 4}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color    = Color.DarkGray
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------
 *  TaskCard - the public composable
 * ------------------------------------------------------------------ */
@Composable
fun TaskCard(
    title: String,
    category: String,
    startTime: String,
    endTime: String,
    progress: Float,                      // 0f .. 1f
    status: TaskStatus,
    avatars: List<Painter>,
    modifier: Modifier = Modifier,
    onMoreClick: () -> Unit = {}
) {
    val shape = TaskTabShape()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(shape)                        // ← TaskTabShape does ALL the shaping
            .background(status.background)
    ) {
        /* ---------- Status pill on the tab ----------
         *
         *  This is just a plain coloured rectangle pinned to the top-left.
         *  Because the parent is clipped by TaskTabShape, the rectangle is
         *  AUTO-SHAPED to the tab outline — including the rounded outer
         *  corner and the concave transition into the card body. No custom
         *  pill shape, no `::after` filler, no visible "lip" artifact.
         *
         *  The rectangle is intentionally drawn slightly wider than the tab
         *  (tabWidth + a few dp) so the concave curve on the right is fully
         *  filled with the accent colour rather than the pastel background.
         */
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .height(32.dp)                  // == TaskTabShape.tabHeight
                .width(126.dp)                  // tabWidth (110) + tabRadius (16) — fills the concave curve
                .background(status.tabColor),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text       = status.label,
                color      = Color.White,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(start = 16.dp)
            )
        }

        /* ---------- Card content ---------- */
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 44.dp, end = 16.dp, bottom = 16.dp)
        ) {

            /* ---------- Time indicator column ---------- */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    text       = startTime,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.DarkGray
                )
                DashedVerticalLine(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                )
                Text(
                    text       = endTime,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.DarkGray
                )
            }

            Spacer(Modifier.width(12.dp))

            /* ---------- Main content column ---------- */
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {

                /* Top row: title + 3-dot menu */
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = title,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.Black,
                            maxLines   = 2
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text     = category,
                            fontSize = 12.sp,
                            color    = Color.DarkGray.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick  = onMoreClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector       = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint              = Color.DarkGray
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                /* Progress bar */
                LinearProgressIndicator(
                    progress  = progress,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color     = status.tabColor,
                    trackColor = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(12.dp))

                /* Bottom row: avatars + percent text */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OverlappingAvatars(avatars = avatars)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text       = "${(progress * 100).toInt()}%",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = status.tabColor
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------
 *  Preview
 * ------------------------------------------------------------------ */
@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
fun TaskCardPreview() {
    // Replace these with real avatar resources in your project
    val sampleAvatar = painterResource(id = android.R.drawable.sym_def_app_icon)
    val members = List(5) { sampleAvatar }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TaskCard(
            title     = "UI/UX Wireframes",
            category  = "Design Sprint",
            startTime = "10 am",
            endTime   = "02 pm",
            progress  = 1.0f,
            status    = TaskStatus.COMPLETED,
            avatars   = members
        )

        TaskCard(
            title     = "Backend API Integration",
            category  = "Mobile Development",
            startTime = "11 am",
            endTime   = "03 pm",
            progress  = 0.6f,
            status    = TaskStatus.RUNNING,
            avatars   = members
        )

        TaskCard(
            title     = "User Research Interviews",
            category  = "Discovery Phase",
            startTime = "01 pm",
            endTime   = "05 pm",
            progress  = 0.2f,
            status    = TaskStatus.PENDING,
            avatars   = members
        )
    }
}
