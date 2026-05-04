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
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
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

private val MintGreen       = Color(0xFFC1FF72)
private val DarkNavy        = Color(0xFF0F1A2E)
private val CalendarSurface = Color(0xFF1A2540)
private val MutedLabel      = Color(0xFF8A95AD)

@Composable
fun HorizontalCalendar(
    viewModel: TaskViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    daysBefore: Int = 30,
    daysAfter: Int = 90,
    accentColor: Color = MintGreen
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
    accentColor: Color = MintGreen
) {
    val today = remember { LocalDate.now() }
    val dates: List<LocalDate> = remember(today, daysBefore, daysAfter) {
        val start = today.minusDays(daysBefore.toLong())
        val total = daysBefore + daysAfter + 1
        List(total) { start.plusDays(it.toLong()) }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedDate) {
        val idx = dates.indexOf(selectedDate).takeIf { it >= 0 } ?: return@LaunchedEffect
        listState.animateScrollToItem(index = (idx - 2).coerceAtLeast(0))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text       = selectedDate.format(monthYearFormatter),
            color      = Color.White,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(start = 4.dp, bottom = 14.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(CalendarSurface)
        ) {
            LazyRow(
                state                 = listState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding        = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                modifier              = Modifier.fillMaxWidth()
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
}

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
        animationSpec = tween(durationMillis = 220),
        label         = "dayCellBg"
    )

    val targetNumber = if (isSelected) DarkNavy else Color.White
    val targetLabel  = if (isSelected) DarkNavy.copy(alpha = 0.75f) else MutedLabel
    val numberFg by animateColorAsState(
        targetValue   = targetNumber,
        animationSpec = tween(durationMillis = 220),
        label         = "dayCellNumber"
    )
    val labelFg by animateColorAsState(
        targetValue   = targetLabel,
        animationSpec = tween(durationMillis = 220),
        label         = "dayCellLabel"
    )

    val interaction = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(58.dp)
            .height(82.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .clickable(
                interactionSource = interaction,
                indication        = ripple(bounded = true, color = accentColor),
                onClick           = onClick
            )
            .padding(vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = date.dayOfMonth.toString(),
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = numberFg
            )
            Text(
                text       = date.dayOfWeek
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    .replaceFirstChar { it.uppercase() },
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = labelFg
            )
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

@Preview(showBackground = true, backgroundColor = 0xFF0F1A2E)
@Composable
fun HorizontalCalendarPreview() {
    var date = LocalDate.now()
    HorizontalCalendar(
        selectedDate = date,
        onSelect     = { date = it }
    )
}
