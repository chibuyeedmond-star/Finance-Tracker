package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Transaction
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MonthlySummary(
    val monthYear: String, // e.g., "May 2026"
    val income: Double,
    val expenses: Double,
    val margin: Double
)

data class UiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalMargin: Double = 0.0,
    val selectedMonth: String = "",
    val availableMonths: List<String> = emptyList(),
    // Data filtered for selectedMonth
    val filteredTransactions: List<Transaction> = emptyList(),
    val filteredIncome: Double = 0.0,
    val filteredExpense: Double = 0.0,
    val filteredMargin: Double = 0.0,
    val categorySpendMap: Map<String, Double> = emptyMap(),
    val dailySpendMap: Map<Int, Double> = emptyMap(), // Day of month -> Expense Amount
    val monthlySummaries: List<MonthlySummary> = emptyList()
)

class ExpenseViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _selectedMonth = MutableStateFlow<String>("")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    val uiState: StateFlow<UiState> = combine(
        repository.allTransactions,
        _selectedMonth
    ) { txList, selMonth ->
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        
        // Define all months represented in the database (or current month if empty)
        val allMonths = txList.map { sdf.format(Date(it.timestamp)) }.distinct()
        val currentMonthStr = sdf.format(Date())
        val finalAvailableMonths = if (allMonths.isEmpty()) listOf(currentMonthStr) else allMonths
        
        // Determine selected month
        val activeMonth = when {
            selMonth.isNotEmpty() && finalAvailableMonths.contains(selMonth) -> selMonth
            finalAvailableMonths.contains(currentMonthStr) -> currentMonthStr
            finalAvailableMonths.isNotEmpty() -> finalAvailableMonths.first()
            else -> currentMonthStr
        }

        // Global Statistics
        val totalIncome = txList.filter { it.isIncome }.sumOf { it.amount }
        val totalExpense = txList.filter { !it.isIncome }.sumOf { it.amount }
        val totalMargin = totalIncome - totalExpense

        // Filtering for active month
        val filteredList = txList.filter { sdf.format(Date(it.timestamp)) == activeMonth }
        val filteredIncome = filteredList.filter { it.isIncome }.sumOf { it.amount }
        val filteredExpense = filteredList.filter { !it.isIncome }.sumOf { it.amount }
        val filteredMargin = filteredIncome - filteredExpense

        // Group expense transactions in current active month by Category
        val catSpend = filteredList.filter { !it.isIncome }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Daily expense map for active month chart
        val calendar = Calendar.getInstance()
        val daySpend = filteredList.filter { !it.isIncome }
            .groupBy { 
                calendar.timeInMillis = it.timestamp
                calendar.get(Calendar.DAY_OF_MONTH)
            }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Monthly Summaries list (all record timeline)
        val monthlySummaryMap = txList.groupBy { sdf.format(Date(it.timestamp)) }
        val summariesList = monthlySummaryMap.map { (mYear, list) ->
            val inc = list.filter { it.isIncome }.sumOf { it.amount }
            val exp = list.filter { !it.isIncome }.sumOf { it.amount }
            MonthlySummary(
                monthYear = mYear,
                income = inc,
                expenses = exp,
                margin = inc - exp
            )
        }.sortedWith { a, b ->
            // Sort by Date descending
            try {
                val dateA = sdf.parse(a.monthYear) ?: Date(0)
                val dateB = sdf.parse(b.monthYear) ?: Date(0)
                dateB.compareTo(dateA)
            } catch (e: Exception) {
                0
            }
        }

        UiState(
            transactions = txList,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalMargin = totalMargin,
            selectedMonth = activeMonth,
            availableMonths = finalAvailableMonths,
            filteredTransactions = filteredList,
            filteredIncome = filteredIncome,
            filteredExpense = filteredExpense,
            filteredMargin = filteredMargin,
            categorySpendMap = catSpend,
            dailySpendMap = daySpend,
            monthlySummaries = summariesList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    fun selectMonth(monthYear: String) {
        _selectedMonth.value = monthYear
    }

    fun addTransaction(title: String, amount: Double, isIncome: Boolean, category: String, timestamp: Long, notes: String = "") {
        viewModelScope.launch {
            repository.insert(
                Transaction(
                    title = title.trim(),
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    timestamp = timestamp,
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.update(transaction)
        }
    }
}

class ExpenseViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
