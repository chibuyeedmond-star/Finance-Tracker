package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun MarginDashboardHub(
    income: Double,
    expense: Double,
    margin: Double,
    modifier: Modifier = Modifier
) {
    val isSurplus = margin >= 0.0
    val marginPercent = if (income > 0.0) (margin / income) * 100 else 0.0
    
    // Gradient and theme colors
    val surplusColor = Color(0xFF2E7D32) // Emerald Green
    val deficitColor = Color(0xFFC62828) // Deep Crimson
    val accentColor = if (isSurplus) surplusColor else deficitColor

    // Smooth gradient backgrounds for beautiful Material card aesthetics
    val brushBg = Brush.verticalGradient(
        colors = if (isSurplus) {
            listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
        } else {
            listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
        }
    )

    val darkThemeBrush = Brush.verticalGradient(
        colors = if (isSurplus) {
            listOf(Color(0xFF1B5E20).copy(alpha = 0.25f), Color(0xFF1B5E20).copy(alpha = 0.1f))
        } else {
            listOf(Color(0xFFB71C1C).copy(alpha = 0.25f), Color(0xFFB71C1C).copy(alpha = 0.1f))
        }
    )

    val contentColor = if (isSurplus) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    val isDark = (MaterialTheme.colorScheme.surface.red + MaterialTheme.colorScheme.surface.green + MaterialTheme.colorScheme.surface.blue) < 1.5f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.Transparent else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) darkThemeBrush else brushBg)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Month Margin",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else contentColor.copy(alpha = 0.8f)
                )
                
                // Status pill badge
                Surface(
                    color = if (isDark) accentColor.copy(alpha = 0.3f) else accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isSurplus) "Surplus" else "Deficit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else contentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Margin Figure
            Text(
                text = String.format(Locale.getDefault(), "$%.2f", margin),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else contentColor
            )

            // Margin percent details
            if (isSurplus && income > 0.0) {
                Text(
                    text = String.format(Locale.getDefault(), "You saved %.1f%% of your earnings", marginPercent),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else contentColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            } else if (!isSurplus && income > 0.0) {
                Text(
                    text = String.format(Locale.getDefault(), "Overspent by %.1f%% relative to earnings", -marginPercent),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else contentColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "No earnings recorded this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else contentColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split metrics inside card
            Divider(color = if (isDark) Color.White.copy(alpha = 0.1f) else contentColor.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Income Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL EARNINGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "+$%.2f", income),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF66BB6A) else Color(0xFF2E7D32)
                    )
                }

                // Divider Line
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(if (isDark) Color.White.copy(alpha = 0.1f) else contentColor.copy(alpha = 0.15f))
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Total Expense Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL SPENDING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "-$%.2f", expense),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}
