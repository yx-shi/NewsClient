package com.example.newsclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.Year

/**
 * 简易日期选择器组件
 * 支持年/月/日的滚动选择
 * 允许模糊搜索（只选择部分日期字段）
 */
@Composable
fun DatePicker(
    onDateSelected: (year: Int?, month: Int?, day: Int?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    // 获取当前年份，用于生成年份列表
    val currentYear = LocalDate.now().year

    // 生成年份列表（当前年份前后10年）
    val years = (currentYear - 10..currentYear).toList().reversed()

    // 生成月份列表
    val months = (1..12).toList()

    // 生成日期列表，根据选定的年月确定天数
    val days = remember(selectedYear, selectedMonth) {
        when {
            selectedYear != null && selectedMonth != null -> {
                val daysInMonth = if (selectedMonth in 1..12) {
                    getDaysInMonth(selectedYear!!, selectedMonth!!)
                } else {
                    31 // 默认最大天数
                }
                (1..daysInMonth).toList()
            }
            else -> (1..31).toList() // 默认显示1-31
        }
    }

    // 当前选择的日期描述
    val selectedDateDescription = when {
        selectedYear != null && selectedMonth != null && selectedDay != null ->
            "${selectedYear}年${selectedMonth}月${selectedDay}日"
        selectedYear != null && selectedMonth != null ->
            "${selectedYear}年${selectedMonth}月"
        selectedYear != null ->
            "${selectedYear}年"
        else -> "未选择日期"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "选择日期",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 显示当前选择的日期
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedYear != null)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = selectedDateDescription,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (selectedYear != null)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 年份选择器
            DatePickerColumn(
                title = "年",
                items = years.map { it.toString() },
                selectedItem = selectedYear?.toString(),
                onItemSelected = { yearStr ->
                    selectedYear = yearStr?.toIntOrNull()
                    // 验证日期是否有效，调整日期如果需要
                    validateAndAdjustDate(
                        selectedYear,
                        selectedMonth,
                        selectedDay
                    )?.let { (_, _, day) ->
                        selectedDay = day
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // 月份选择器
            DatePickerColumn(
                title = "月",
                items = months.map { it.toString() },
                selectedItem = selectedMonth?.toString(),
                onItemSelected = { monthStr ->
                    selectedMonth = monthStr?.toIntOrNull()
                    // 验证日期是否有效，调整日期如果需要
                    validateAndAdjustDate(
                        selectedYear,
                        selectedMonth,
                        selectedDay
                    )?.let { (_, _, day) ->
                        selectedDay = day
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // 日选择器
            DatePickerColumn(
                title = "日",
                items = days.map { it.toString() },
                selectedItem = selectedDay?.toString(),
                onItemSelected = { dayStr ->
                    selectedDay = dayStr?.toIntOrNull()
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 重置按钮
            TextButton(
                onClick = {
                    selectedYear = null
                    selectedMonth = null
                    selectedDay = null
                }
            ) {
                Text("重置", color = MaterialTheme.colorScheme.primary)
            }

            // 确定和取消按钮
            Row {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        onDateSelected(selectedYear, selectedMonth, selectedDay)
                        onDismiss()
                    }
                ) {
                    Text("确定")
                }
            }
        }
    }
}

/**
 * 单个日期字段选择器列
 */
@Composable
private fun DatePickerColumn(
    title: String,
    items: List<String>,
    selectedItem: String?,
    onItemSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // 初始滚动到所选项附近
    LaunchedEffect(selectedItem) {
        selectedItem?.let { selected ->
            val index = items.indexOf(selected)
            if (index >= 0) {
                listState.scrollToItem(index.coerceAtMost(items.size - 1))
            }
        }
    }

    Column(
        modifier = modifier.height(220.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ),
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp
        ) {
            Box {
                // 中间选中项的指示器
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(40.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {}

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 空选项，表示未选择
                    item {
                        DatePickerItem(
                            text = "不限",
                            isSelected = selectedItem == null,
                            onClick = { onItemSelected(null) }
                        )
                    }

                    // 实际选项
                    items(items) { item ->
                        DatePickerItem(
                            text = item,
                            isSelected = item == selectedItem,
                            onClick = { onItemSelected(item) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 日期选择器中的单个项
 */
@Composable
private fun DatePickerItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 4.dp)
            .then(
                if (isSelected) Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isSelected) 18.sp else 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            onClick = onClick
        ) {}
    }
}

/**
 * 格式化选定的日期为字符串
 * 支持模糊日期（只有年、只有年月等）
 */
fun formatSelectedDate(year: Int?, month: Int?, day: Int?): String? {
    return when {
        year != null && month != null && day != null -> {
            // 完整日期: YYYY-MM-DD
            String.format("%04d-%02d-%02d", year, month, day)
        }
        year != null && month != null -> {
            // 年月: YYYY-MM
            String.format("%04d-%02d", year, month)
        }
        year != null -> {
            // 只有年: YYYY
            String.format("%04d", year)
        }
        else -> null // 所有字段都为null，表示不筛选
    }
}

/**
 * 将选定的日期转换为日期范围（startDate 和 endDate）
 * 用于服务器API调用
 */
fun formatDateRange(year: Int?, month: Int?, day: Int?): Pair<String, String>? {
    return when {
        year != null && month != null && day != null -> {
            // 完整日期：同一天的开始和结束
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)
            Pair(dateStr, dateStr)
        }
        year != null && month != null -> {
            // 年月：该月份的第一天到最后一天
            val startDate = String.format("%04d-%02d-01", year, month)
            val daysInMonth = getDaysInMonth(year, month)
            val endDate = String.format("%04d-%02d-%02d", year, month, daysInMonth)
            Pair(startDate, endDate)
        }
        year != null -> {
            // 只有年：该年的第一天到最后一天
            val startDate = String.format("%04d-01-01", year)
            val endDate = String.format("%04d-12-31", year)
            Pair(startDate, endDate)
        }
        else -> null // 所有字段都为null，表示不筛选
    }
}

/**
 * 获取指定年月的天数（移动到公共位置）
 */
private fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        2 -> if (Year.isLeap(year.toLong())) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

/**
 * 验证并调整日期，确保选定的日期是有效的
 * 如果日期无效（例如2月30日），调整为该月的最后一天
 */
private fun validateAndAdjustDate(year: Int?, month: Int?, day: Int?): Triple<Int?, Int?, Int?>? {
    if (year == null || month == null || day == null) {
        return null
    }

    val maxDay = getDaysInMonth(year, month)
    val adjustedDay = if (day > maxDay) maxDay else day

    return Triple(year, month, adjustedDay)
}

/**
 * 格式化日期显示文本（用于UI显示）
 */
fun formatDateForDisplay(year: Int?, month: Int?, day: Int?): String {
    return when {
        year != null && month != null && day != null ->
            "${year}年${month}月${day}日"
        year != null && month != null ->
            "${year}年${month}月"
        year != null ->
            "${year}年"
        else -> "不限时间"
    }
}
