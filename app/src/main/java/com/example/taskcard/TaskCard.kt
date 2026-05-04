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
import androidx.compose.ui.draw.alpha
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
 *  TaskStatus  -  drives the colour palette + label of the tab
 * ------------------------------------------------------------------ */
enum class TaskStatus(
    val label: String,
    val background: Color,
    val backgroundDeep: Color,
    val tabColor: Color,
    val onCard: Color
) {
    COMPLETED(
        label          = "Completed",
        background     = Color(0xFFD9F2E3),
        backgroundDeep = Color(0xFFC2E5D0),
        tabColor       = Color(0xFF34A853),
        onCard         = Color(0xFF0E1B14)
    ),
    RUNNING(
        label          = "Running",
        background     = Color(0xFFD8E6FB),
        backgroundDeep = Color(0xFFBFD2F1),
        tabColor       = Color(0xFF1A73E8),
        onCard         = Color(0xFF0E1A2E)
    ),
    PENDING(
        label          = "Pending",
        background     = Color(0xFFE6DCF7),
        backgroundDeep = Color(0xFFCFC1EC),
        tabColor       = Color(0xFF7B5BD6),
        onCard         = Color(0xFF1B1530)
    ),
    CANCELLED(
        label          = "Rejected",
        background     = Color(0xFFFFE0E0),
        backgroundDeep = Color(0xFFF4C5C5),
        tabColor       = Color(0xFFD93025),
        onCard         = Color(0xFF301212)
    )
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
 *  TaskTabShape - a rounded rectangle whose top-left corner has a
 *  small "tab" cut-out where the status pill will sit.
 * ------------------------------------------------------------------ */
class TaskTabShape(
    private val cornerRadius: androidx.compose.ui.unit.Dp = 22.dp,
    private val tabWidth: androidx.compose.ui.unit.Dp     = 118.dp,
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
    color: Color = Color.DarkGray.copy(alpha = 0.45f)
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
    borderColor: Color = Color.White
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
                        text       = "+${avatars.size - 4}",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(priority: TaskPriority, onCard: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(priority.color.copy(alpha = 0.22f))
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
            color      = onCard.copy(alpha = 0.85f),
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

    val cardAlpha by animateColorAsState(
        targetValue   = if (isCompleted) Color.White.copy(alpha = 0.78f) else Color.White,
        animationSpec = tween(220),
        label         = "cardAlpha"
    )

    var menuExpanded by remember { mutableStateOf(false) }

    val cardBrush = Brush.verticalGradient(
        colors = listOf(status.background, status.backgroundDeep)
    )
    val tabBrush = Brush.horizontalGradient(
        colors = listOf(status.tabColor, status.tabColor.copy(alpha = 0.82f))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp)
            .alpha(cardAlpha.alpha)
            .clip(shape)
            .background(cardBrush)
    ) {
        /* ---------- Status tab ---------- */
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .height(34.dp)
                .width(136.dp)
                .background(tabBrush),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text       = status.label,
                color      = Color.White,
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
                    color      = status.onCard.copy(alpha = 0.75f)
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
                    color      = status.onCard.copy(alpha = 0.75f)
                )
            }

            Spacer(Modifier.width(10.dp))

            /* Main content column */
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {

                /* Top row: title + 3-dot menu */
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text          = title,
                            fontSize      = 18.sp,
                            fontWeight    = FontWeight.ExtraBold,
                            color         = status.onCard,
                            maxLines      = 2,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text     = category,
                            fontSize = 12.sp,
                            color    = status.onCard.copy(alpha = 0.65f)
                        )
                    }

                    /* 3-dot menu anchor */
                    Box {
                        IconButton(
                            onClick  = { menuExpanded = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector       = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint              = status.onCard.copy(alpha = 0.7f)
                            )
                        }
                        DropdownMenu(
                            expanded         = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text    = { Text("Edit") },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text    = {
                                    Text(if (isCompleted) "Mark as Pending" else "Mark as Completed")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector       = if (isCompleted)
                                            Icons.Outlined.RadioButtonUnchecked
                                        else
                                            Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint              = if (isCompleted)
                                            Color.Unspecified
                                        else
                                            TaskStatus.COMPLETED.tabColor
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onToggleComplete()
                                }
                            )
                            DropdownMenuItem(
                                text    = { Text("Delete", color = Color(0xFFD93025)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector       = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint              = Color(0xFFD93025)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                /* Progress bar OR completed badge */
                if (isCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(status.tabColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint               = status.tabColor,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Completed",
                            color      = status.tabColor,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LinearProgressIndicator(
                        progress  = progress,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color     = status.tabColor,
                        trackColor = Color.White.copy(alpha = 0.65f)
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
                    PriorityChip(priority = priority, onCard = status.onCard)
                    Spacer(Modifier.weight(1f))
                    if (!isCompleted) {
                        Text(
                            text       = "${(progress * 100).toInt()}%",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = status.tabColor
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
