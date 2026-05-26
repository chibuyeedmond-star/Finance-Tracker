package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Transaction
import com.example.data.TransactionRepository
import com.example.ui.components.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(
    repository: TransactionRepository,
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(repository))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var activeTabState by remember { mutableStateOf(0) } // 0: Tracker, 1: Categories, 2: Timeline Summary
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Expense Tracker",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("fab_add_transaction")
                    .navigationBarsPadding() // avoid overlapping on gesture line
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Material 3 Tabs Selection Bar
            TabRow(
                selectedTabIndex = activeTabState,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                modifier = Modifier.fillMaxWidth().testTag("app_tabs")
            ) {
                Tab(
                    selected = activeTabState == 0,
                    onClick = { activeTabState = 0 },
                    text = { Text("Tracker", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.height(48.dp).testTag("tab_tracker")
                )
                Tab(
                    selected = activeTabState == 1,
                    onClick = { activeTabState = 1 },
                    text = { Text("Categories", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.height(48.dp).testTag("tab_categories")
                )
                Tab(
                    selected = activeTabState == 2,
                    onClick = { activeTabState = 2 },
                    text = { Text("Summaries", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.height(48.dp).testTag("tab_summaries")
                )
            }

            // Tab Content Window with custom transition
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTabState) {
                    0 -> {
                        // 1. DAILY TRACKER TAB
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                        ) {
                            // Month Selector Row Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { monthDropdownExpanded = true }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "FILTERING PERIOD",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = uiState.selectedMonth,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Box {
                                            TextButton(onClick = { monthDropdownExpanded = true }) {
                                                Text("Change Month")
                                            }

                                            DropdownMenu(
                                                expanded = monthDropdownExpanded,
                                                onDismissRequest = { monthDropdownExpanded = false }
                                            ) {
                                                uiState.availableMonths.forEach { mYear ->
                                                    DropdownMenuItem(
                                                        text = { Text(mYear, fontWeight = FontWeight.Medium) },
                                                        onClick = {
                                                            viewModel.selectMonth(mYear)
                                                            monthDropdownExpanded = false
                                                        },
                                                        modifier = Modifier.testTag("month_option_$mYear")
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Safe margin KPI Hub
                            item {
                                MarginDashboardHub(
                                    income = uiState.filteredIncome,
                                    expense = uiState.filteredExpense,
                                    margin = uiState.filteredMargin,
                                    modifier = Modifier.testTag("kpi_dashboard_hub")
                                )
                            }

                            // Visual Trend Chart Card
                            item {
                                DailyExpenseChart(
                                    dailySpendMap = uiState.dailySpendMap,
                                    selectedMonthYear = uiState.selectedMonth,
                                    modifier = Modifier.testTag("daily_trend_chart")
                                )
                            }

                            // Header title for Ledger
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Transactions Ledger",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${uiState.filteredTransactions.size} items",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Transactions List State
                            if (uiState.filteredTransactions.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 24.dp, bottom = 48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Search",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Text(
                                                text = "No recorded transactions for this month.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = "Tap the '+' button below to add your first check!",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(
                                    items = uiState.filteredTransactions,
                                    key = { it.id }
                                ) { transaction ->
                                    TransactionRow(
                                        transaction = transaction,
                                        onDeleteClick = { viewModel.deleteTransaction(transaction) }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // 2. CATEGORIES BREAKDOWN TAB
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "CATEGORIES ANALYSIS PERIOD",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = uiState.selectedMonth,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                CategoryDistributionView(
                                    categorySpendMap = uiState.categorySpendMap,
                                    totalExpense = uiState.filteredExpense,
                                    modifier = Modifier.testTag("category_distribution_panel")
                                )
                            }
                        }
                    }

                    2 -> {
                        // 3. MONTHLY TIMELINE SUMMARIES TAB
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                        ) {
                            item {
                                Text(
                                    text = "Monthly Spending Timeline",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "Chronological log showing historical income and outcome balances.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (uiState.monthlySummaries.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No history log available yet. Start logging!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            } else {
                                items(
                                    items = uiState.monthlySummaries,
                                    key = { it.monthYear }
                                ) { summary ->
                                    val isSurplus = summary.margin >= 0.0
                                    val accentColor = if (isSurplus) Color(0xFF2E7D32) else Color(0xFFC62828)

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                // Select this month and jump back to dashboard!
                                                viewModel.selectMonth(summary.monthYear)
                                                activeTabState = 0
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = summary.monthYear,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(accentColor)
                                                    )
                                                    Text(
                                                        text = if (isSurplus) "Surplus" else "Deficit",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = accentColor
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "INFLOW",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = String.format(Locale.getDefault(), "$%.2f", summary.income),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF2E7D32)
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = "OUTFLOW",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = String.format(Locale.getDefault(), "$%.2f", summary.expenses),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFC62828)
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "NET MARGIN",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = String.format(Locale.getDefault(), "$%.2f", summary.margin),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = accentColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to add a transaction record
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, isIncome, category, timestamp, notes ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    timestamp = timestamp,
                    notes = notes
                )
                showAddDialog = false
            }
        )
    }
}
