package com.example.taskcard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------------------------
 *  Premium dark card palette
 * ------------------------------------------------------------------ */
private val CardTop      = Color(0xFF1F2937)   // subtle elevation top
private val CardBottom   = Color(0xFF161E2C)   // slightly deeper bottom
private val CardOnPrimary   = Color(0xFFF9FAFB) // near-white title
private val CardOnSecondary = Color(0xFFB8C0CC) // muted gray body
private val CardOnTertiary  = Color(0xFF8B95A4) // faint label
private val CardDivider     = Color(0x33FFFFFF) // dashed line
private val CardOnAccent    = Color(0xFF0B1220) // dark text on light accent
private val CardDangerRed   = Color(0xFFFF6B6B)

/* ------------------------------------------------------------------
 *  TaskStatus  -  drives the accent color used by the tab,
 *  progress bar, and completion badge.
 * ------------------------------------------------------------------ */
enum class TaskStatus(
    val label: String,
    val accent: Color
) {
    COMPLETED("Completed", Color(0xFFC1FF72)), // Mint Green
    RUNNING  ("Running",   Color(0xFFE1BEE7)), // Lavender
    PENDING  ("Pending",   Color(0xFF00E5FF)), // Cyan
    CANCELLED("Rejected",  Color(0xFFFF8A8A))  // Coral
}

/* ------------------------------------------------------------------
 *  TaskPriority - LOW / MEDIUM / HIGH using mint / lavender / cyan
 * ------------------------------------------------------------------ */
enum class TaskPriority(val label: String, val color: Color) {
    LOW   ("Low",    Color(0xFFC1FF72)),
    MEDIUM("Medium", Color(0xFFE1BEE7)),
    HIGH  ("High",   Color(0xFF00E5FF))
}

/* ------------------------------------------------------------------
 *  TaskTabShape - rounded rectangle with a "tab" cut-out at top-left
 * ------------------------------------------------------------------ */
class TaskTabShape(
    private val cornerRadius: androidx.compose.ui.unit.Dp = 22.dp,
    private val tabWidth: androidx.compose.ui.unit.Dp     = 122.dp,
    private val tabHeight: androidx.compose.ui.unit.Dp    = 34.dp,
    private val tabRadius: androidx.compose.ui.unit.Dp    = 18.dp
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
                moveTo(tw, 0f)
                lineTo(size.width - r, 0f)
                arcTo(
                    rect              = Rect(size.width - 2 * r, 0f, size.width, 2 * r),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo       = false
                )
                lineTo(size.width, size.height - r)
                arcTo(
                    rect              = Rect(size.width - 2 * r, size.height - 2 * r, size.width, size.height),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo       = false
                )
                lineTo(r, size.height)
                arcTo(
                    rect              = Rect(0f, size.height - 2 * r, 2 * r, size.height),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo       = false
                )
                lineTo(0f, th + r)
                arcTo(
                    rect              = Rect(0f, th, 2 * r, th + 2 * r),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo       = false
                )
                lineTo(tw - tr, th)
                arcTo(
                    rect              = Rect(tw - 2 * tr, th - 2 * tr, tw, th),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo       = false
                )
                lineTo(tw, 0f)
                close()
            }
            return Outline.Generic(path)
        }
    }
}

/* ------------------------------------------------------------------
 *  Helpers
 * ------------------------------------------------------------------ */
@Composable
private fun DashedVerticalLine(
    modifier: Modifier = Modifier,
    color: Color = CardDivider
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

@Composable
private fun OverlappingAvatars(
    avatars: List<Painter>,
    size: androidx.compose.ui.unit.Dp = 26.dp,
    overlap: androidx.compose.ui.unit.Dp = 9.dp,
    borderColor: Color = CardTop
) {
    if (avatars.isEmpty()) return
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
                    painter            = painter,
                    contentDescription = "Member avatar",
                    modifier           = Modifier
                        .size(size)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
private fun PriorityChip(priority: TaskPriority) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(priority.color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(priority.color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = priority.label,
            color      = priority.color,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
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
    progress: Float,
    status: TaskStatus,
    avatars: List<Painter>,
    modifier: Modifier = Modifier,
    priority: TaskPriority = TaskPriority.MEDIUM,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleComplete: () -> Unit = {}
) {
    val shape       = TaskTabShape()
    val isCompleted = status == TaskStatus.COMPLETED

    val titleAlpha by animateColorAsState(
        targetValue   = if (isCompleted) CardOnPrimary.copy(alpha = 0.55f) else CardOnPrimary,
        animationSpec = tween(220),
        label         = "titleColor"
    )

    var menuExpanded by remember { mutableStateOf(false) }

    val cardBrush = Brush.verticalGradient(colors = listOf(CardTop, CardBottom))
    val tabBrush  = Brush.horizontalGradient(
        colors = listOf(status.accent, status.accent.copy(alpha = 0.85f))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(shape)
            .background(cardBrush)
    ) {
        /* ---------- Status tab (uses status.accent — Mint for Completed) ---------- */
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .height(34.dp)
                .width(140.dp)
                .background(tabBrush),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text       = status.label,
                color      = CardOnAccent,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(start = 18.dp)
            )
        }

        /* ---------- Card content ---------- */
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 46.dp, end = 12.dp, bottom = 14.dp)
        ) {

            /* Time indicator column */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(58.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    text       = startTime,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = CardOnTertiary
                )
                DashedVerticalLine(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                )
                Text(
                    text       = endTime,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = CardOnTertiary
                )
            }

            Spacer(Modifier.width(10.dp))

            /* Main content column */
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {

                /* Top row: title + 3-dot menu */
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text           = title,
                            fontSize       = 18.sp,
                            fontWeight     = FontWeight.ExtraBold,
                            color          = titleAlpha,
                            maxLines       = 2,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text     = category,
                            fontSize = 12.sp,
                            color    = CardOnSecondary.copy(alpha = 0.7f)
                        )
                    }

                    /* 3-dot menu anchor */
                    Box {
                        IconButton(
                            onClick  = { menuExpanded = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint               = CardOnSecondary
                            )
                        }
                        DropdownMenu(
                            expanded         = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text        = { Text("Edit") },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                                onClick     = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text        = {
                                    Text(if (isCompleted) "Mark as Pending" else "Mark as Done")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector        = if (isCompleted)
                                            Icons.Outlined.RadioButtonUnchecked
                                        else
                                            Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint               = if (isCompleted)
                                            Color.Unspecified
                                        else
                                            TaskStatus.COMPLETED.accent
                                    )
                                },
                                onClick     = {
                                    menuExpanded = false
                                    onToggleComplete()
                                }
                            )
                            DropdownMenuItem(
                                text        = { Text("Delete", color = CardDangerRed) },
                                leadingIcon = {
                                    Icon(
                                        imageVector        = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint               = CardDangerRed
                                    )
                                },
                                onClick     = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                /* Progress bar OR completion badge */
                if (isCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(status.accent.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint               = status.accent,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Done",
                            color      = status.accent,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LinearProgressIndicator(
                        progress   = { progress },
                        modifier   = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color      = status.accent,
                        trackColor = Color.White.copy(alpha = 0.10f),
                        drawStopIndicator = {} // suppress the M3 stop dot for a clean line
                    )
                }

                Spacer(Modifier.height(10.dp))

                /* Bottom row: avatars + priority + percent */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (avatars.isNotEmpty()) {
                        OverlappingAvatars(avatars = avatars)
                        Spacer(Modifier.width(10.dp))
                    }
                    PriorityChip(priority = priority)
                    Spacer(Modifier.weight(1f))
                    if (!isCompleted) {
                        Text(
                            text       = "${(progress * 100).toInt()}%",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = status.accent
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------
 *  Preview
 * ------------------------------------------------------------------ */
@Preview(showBackground = true, backgroundColor = 0xFF111827)
@Composable
fun TaskCardPreview() {
    val sampleAvatar = painterResource(id = android.R.drawable.sym_def_app_icon)
    val members = List(3) { sampleAvatar }

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
            avatars   = members,
            priority  = TaskPriority.LOW
        )
        TaskCard(
            title     = "Backend API Integration",
            category  = "Mobile Development",
            startTime = "11 am",
            endTime   = "03 pm",
            progress  = 0.6f,
            status    = TaskStatus.RUNNING,
            avatars   = members,
            priority  = TaskPriority.HIGH
        )
        TaskCard(
            title     = "User Research Interviews",
            category  = "Discovery Phase",
            startTime = "01 pm",
            endTime   = "05 pm",
            progress  = 0.2f,
            status    = TaskStatus.PENDING,
            avatars   = emptyList(),
            priority  = TaskPriority.MEDIUM
        )
    }
}
