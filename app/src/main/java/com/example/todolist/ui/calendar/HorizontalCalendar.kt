package com.example.todolist.ui.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todolist.ui.tasks.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/* =============================================================
 *  PUBLIC API
 *  -----------------------------------------------------------
 *  Two flavors:
 *
 *  1. HorizontalCalendar(viewModel)  →  one-line drop-in.
 *     Reads selectedDate from the VM and calls vm.selectDate()
 *     on click.
 *
 *  2. HorizontalCalendar(selected, onSelect, ...) →  stateless
 *     version for previews / when you don't want Hilt in scope.
 * ============================================================= */

@Composable
fun HorizontalCalendar(
    viewModel: TaskViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    daysBefore: Int = 30,
    daysAfter: Int = 90,
    accentColor: Color = Color(0xFF34A853)        // the "green oval" from the mock
) {
    val selected by viewModel.selectedDate.collectAsStateWithLifecycle()

    HorizontalCalendar(
        selectedDate = selected,
        onSelect     = viewModel::selectDate,
        modifier     = modifier,
        daysBefore   = daysBefore,
        daysAfter    = daysAfter,
        accentColor  = accentColor
    )
}

@Composable
fun HorizontalCalendar(
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    daysBefore: Int = 30,
    daysAfter: Int = 90,
    accentColor: Color = Color(0xFF34A853)
) {
    /* ----------  Build the date range once ---------- */
    val today = remember { LocalDate.now() }
    val dates: List<LocalDate> = remember(today, daysBefore, daysAfter) {
        val start = today.minusDays(daysBefore.toLong())
        val total = daysBefore + daysAfter + 1
        List(total) { start.plusDays(it.toLong()) }
    }

    /* ----------  Auto-scroll selected into view ---------- */
    val listState = rememberLazyListState()
    LaunchedEffect(selectedDate) {
        val idx = dates.indexOf(selectedDate).takeIf { it >= 0 } ?: return@LaunchedEffect
        // Center it: scroll so the selected item sits roughly in the middle.
        listState.animateScrollToItem(index = (idx - 2).coerceAtLeast(0))
    }

    Column(modifier = modifier.fillMaxWidth()) {

        /* ----------  Month / year header ---------- */
        Text(
            text     = selectedDate.format(monthYearFormatter),
            style    = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        /* ----------  Day strip ---------- */
        LazyRow(
            state               = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier            = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            items(items = dates, key = { it.toEpochDay() }) { date ->
                DayCell(
                    date        = date,
                    isSelected  = date == selectedDate,
                    isToday     = date == today,
                    accentColor = accentColor,
                    onClick     = { onSelect(date) }
                )
            }
        }
    }
}

/* =============================================================
 *  Single day cell — pill background when selected
 * ============================================================= */
@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val targetBg = if (isSelected) accentColor else Color.Transparent
    val bg by animateColorAsState(
        targetValue   = targetBg,
        animationSpec = tween(durationMillis = 180),
        label         = "dayCellBg"
    )

    val targetFg = when {
        isSelected -> Color.White
        isToday    -> accentColor
        else       -> Color(0xFF333333)
    }
    val fg by animateColorAsState(
        targetValue   = targetFg,
        animationSpec = tween(durationMillis = 180),
        label         = "dayCellFg"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(48.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(24.dp))     // green-oval shape
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                fontSize   = 11.sp,
                color      = fg.copy(alpha = if (isSelected) 0.9f else 0.65f)
            )
            Text(
                text       = date.dayOfMonth.toString(),
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = fg
            )
            // Tiny dot under "today" when it isn't the selected one
            if (isToday && !isSelected) {
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
            }
        }
    }
}

private val monthYearFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

/* =============================================================
 *  Preview — pure stateless variant, no Hilt needed
 * ============================================================= */
@Preview(showBackground = true)
@Composable
fun HorizontalCalendarPreview() {
    var date = LocalDate.now()
    HorizontalCalendar(
        selectedDate = date,
        onSelect     = { date = it }
    )
}
