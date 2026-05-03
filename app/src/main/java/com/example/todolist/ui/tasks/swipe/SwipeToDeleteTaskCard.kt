package com.example.todolist.ui.tasks.swipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.taskcard.TaskTabShape           // reuse the same shape used by TaskCard
import com.example.todolist.data.local.entity.Task

/* =============================================================
 *  PUBLIC API
 *  -----------------------------------------------------------
 *  Wrap any TaskCard with this composable to enable
 *  right-to-left swipe-to-delete. Example:
 *
 *      LazyColumn {
 *          items(tasks, key = { it.id }) { task ->
 *              SwipeToDeleteTaskCard(
 *                  task     = task,
 *                  onDelete = viewModel::deleteTask,
 *                  modifier = Modifier.animateItem()
 *              ) {
 *                  TaskCard(...)
 *              }
 *          }
 *      }
 * ============================================================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteTaskCard(
    task: Task,
    onDelete: (Task) -> Unit,
    modifier: Modifier = Modifier,
    cardShape: androidx.compose.ui.graphics.Shape = TaskTabShape(),
    content: @Composable () -> Unit
) {
    /* ---------- Visibility flag for slide-out animation ---------- */
    var dismissed by remember(task.id) { mutableStateOf(false) }

    /* ---------- SwipeToDismissBox state ---------- */
    val dismissState = rememberSwipeToDismissBoxState(
        // 50 % of the card's width is the threshold to commit the dismiss
        positionalThreshold = { distance -> distance * 0.5f },
        confirmValueChange  = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                dismissed = true
                true
            } else false
        }
    )

    /* ---------- Side-effect: actually call repository.delete() ---------- */
    LaunchedEffect(dismissed) {
        if (dismissed) {
            // Tiny delay lets the swipe animation finish before the row collapses
            kotlinx.coroutines.delay(180)
            onDelete(task)
        }
    }

    /* ---------- AnimatedVisibility wraps the whole row so it
                  shrinks gracefully out of the LazyColumn ---------- */
    AnimatedVisibility(
        visible = !dismissed,
        exit    = shrinkVertically(animationSpec = tween(220)) + fadeOut(tween(180)),
        enter   = fadeIn()
    ) {
        SwipeToDismissBox(
            state                       = dismissState,
            modifier                    = modifier,
            enableDismissFromStartToEnd = false,        // disable left-to-right
            enableDismissFromEndToStart = true,
            backgroundContent           = {
                DeleteBackground(
                    isActive = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart,
                    progress = dismissState.progress,
                    shape    = cardShape
                )
            }
        ) {
            // The card itself — apply the same shape so corners
            // animate cleanly during the swipe.
            Box(modifier = Modifier.clip(cardShape)) {
                content()
            }
        }
    }
}

/* =============================================================
 *  Red panel + trash icon revealed behind the card during swipe.
 *  Clipped to the same TaskTabShape so the background never spills
 *  past the card's rounded corners or top-left tab.
 * ============================================================= */
@Composable
private fun DeleteBackground(
    isActive: Boolean,
    progress: Float,
    shape: androidx.compose.ui.graphics.Shape
) {
    /* The icon scales up slightly as the swipe progresses */
    val iconScale by animateFloatAsState(
        targetValue   = if (isActive) 1.25f else 0.85f,
        animationSpec = tween(durationMillis = 200),
        label         = "trashIconScale"
    )

    /* Red intensifies as the user pulls further */
    val redIntensity = (progress.coerceIn(0f, 1f))
    val bgColor = Color(0xFFE53935).copy(alpha = 0.65f + redIntensity * 0.35f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(bgColor)
            .padding(end = 28.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector       = Icons.Default.Delete,
            contentDescription = "Delete task",
            tint              = Color.White,
            modifier = Modifier
                .size(28.dp)
                .scale(iconScale)
                .graphicsLayer { alpha = 0.4f + redIntensity * 0.6f }
        )
    }
}
