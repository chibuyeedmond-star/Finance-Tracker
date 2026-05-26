package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun DailyExpenseChart(
    dailySpendMap: Map<Int, Double>,
    selectedMonthYear: String,
    modifier: Modifier = Modifier
) {
    // Determine number of days to display (default to 31)
    val totalDays = 31
    val maxSpend = dailySpendMap.values.maxOrNull() ?: 10.0
    val maxVal = if (maxSpend == 0.0) 10.0 else maxSpend * 1.15 // Add 15% head room
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val textLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Animation factor to draw growth
    var animateFactor by remember { mutableStateOf(0f) }
    LaunchedEffect(selectedMonthYear) {
        animateFactor = 0f
        animateFactor = 1f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = animateFactor,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Daily Expenses View",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Total daily spending trend for $selectedMonthYear",
            style = MaterialTheme.typography.bodySmall,
            color = textLabelColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                val bottomAxisHeight = 24.dp.toPx()
                val graphHeight = height - bottomAxisHeight
                val barWidth = (width / totalDays) * 0.7f
                val spacing = (width / totalDays) * 0.3f
                
                // Draw 3 horizontal grid lines representing 25%, 50%, 75%, 100% of Max
                val gridLines = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                gridLines.forEach { percentage ->
                    val yLine = graphHeight - (graphHeight * percentage)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, yLine),
                        end = Offset(width, yLine),
                        strokeWidth = 1.0f.dp.toPx()
                    )
                }

                // Draw standard bottom baseline
                drawLine(
                    color = gridColor,
                    start = Offset(0f, graphHeight),
                    end = Offset(width, graphHeight),
                    strokeWidth = 1.5f.dp.toPx()
                )

                // Plot actual bars side by side
                for (day in 1..totalDays) {
                    val amount = dailySpendMap[day] ?: 0.0
                    val barHeight = ((amount / maxVal) * graphHeight).toFloat() * animatedProgress
                    
                    val xOffset = (day - 1) * (barWidth + spacing) + (spacing / 2f)
                    val yOffset = graphHeight - barHeight

                    if (amount > 0.0) {
                        // Highlight high spending triggers (above 60% of max spend) with Tertiary color
                        val barColor = if (amount > maxSpend * 0.6) tertiaryColor else primaryColor
                        
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(xOffset, yOffset),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    } else {
                        // Draw a very subtle circle indicating 0 spending
                        val dotRadius = 1.5f.dp.toPx()
                        drawCircle(
                            color = gridColor.copy(alpha = 0.6f),
                            radius = dotRadius,
                            center = Offset(xOffset + barWidth/2f, graphHeight - 4.dp.toPx())
                        )
                    }
                }
            }
        }

        // Horizontal Tick label names for Days 1, 10, 20, 31
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Day 1", style = MaterialTheme.typography.bodySmall, color = textLabelColor, fontSize = 10.sp)
            Text("Day 10", style = MaterialTheme.typography.bodySmall, color = textLabelColor, fontSize = 10.sp)
            Text("Day 20", style = MaterialTheme.typography.bodySmall, color = textLabelColor, fontSize = 10.sp)
            Text("Day 31", style = MaterialTheme.typography.bodySmall, color = textLabelColor, fontSize = 10.sp)
        }
    }
}

@Composable
fun CategoryDistributionView(
    categorySpendMap: Map<String, Double>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    val textLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val sortedCategories = categorySpendMap.toList().sortedByDescending { it.second }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Category Shares",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Proportional allocation of expenses this month",
            style = MaterialTheme.typography.bodySmall,
            color = textLabelColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (sortedCategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses recorded this month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textLabelColor
                )
            }
        } else {
            sortedCategories.forEachIndexed { index, (category, amount) ->
                val percentage = if (totalExpense > 0.0) (amount / totalExpense) else 0.0
                
                // Color list corresponding to top categories
                val barColor = when (index % 4) {
                    0 -> MaterialTheme.colorScheme.primary
                    1 -> MaterialTheme.colorScheme.secondary
                    2 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                }

                CategoryProgressRow(
                    categoryName = category,
                    amount = amount,
                    percentage = percentage,
                    barColor = barColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryProgressRow(
    categoryName: String,
    amount: Double,
    percentage: Double,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    var animateFactor by remember { mutableStateOf(0f) }
    LaunchedEffect(categoryName) {
        animateFactor = 0f
        animateFactor = percentage.toFloat()
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = animateFactor,
        animationSpec = tween(durationMillis = 600),
        label = "progressAnim"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color dot indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(barColor, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = String.format(Locale.getDefault(), "$%.2f (%.1f%%)", amount, percentage * 100),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                    .background(barColor, RoundedCornerShape(4.dp))
            )
        }
    }
}
