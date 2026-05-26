package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, isIncome: Boolean, category: String, timestamp: Long, notes: String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) } // Default to Expense
    var notes by remember { mutableStateOf("") }
    
    // Lists of options based on transaction type
    val incomeCategories = listOf("Salary", "Freelance/Side Hustle", "Investment", "Gifts", "Other Income")
    val expenseCategories = listOf(
        "Groceries", "Dining Out", "Bills & Utilities", "Rent/Mortgage", 
        "Transportation", "Entertainment", "Shopping", "Healthcare", "Education", "Other Expense"
    )

    var selectedCategory by remember(isIncome) { 
        mutableStateOf(if (isIncome) incomeCategories.first() else expenseCategories.first()) 
    }

    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    val formattedDate = remember(timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        sdf.format(Date(timestamp))
    }

    // Toggle dropdown state
    var dropdownExpanded by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    // native date picker dialog launcher
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selCal = Calendar.getInstance()
            selCal.set(Calendar.YEAR, year)
            selCal.set(Calendar.MONTH, month)
            selCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            timestamp = selCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Segmented tab selecting: INCOME vs EXPENSE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isIncome = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!isIncome) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("toggle_expense")
                    ) {
                        Text("Expense", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isIncome = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isIncome) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("toggle_income")
                    ) {
                        Text("Income", fontWeight = FontWeight.Bold)
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.trim().isNotEmpty()) {
                            titleError = null
                        }
                    },
                    label = { Text("Title") },
                    placeholder = { Text("Rent, Salary, Groceries...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_title"),
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    singleLine = true
                )

                // Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        if (it.toDoubleOrNull() != null && it.toDouble() > 0) {
                            amountError = null
                        }
                    },
                    label = { Text("Amount ($)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_amount"),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    singleLine = true
                )

                // Category Selection Box (Dropdown)
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("dropdown_category_field")
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            val categories = if (isIncome) incomeCategories else expenseCategories
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("category_option_$cat")
                                )
                            }
                        }
                    }
                }

                // Date Picker trigger button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { datePickerDialog.show() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Transaction Date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = { datePickerDialog.show() },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.testTag("button_change_date")
                    ) {
                        Text("Change")
                    }
                }

                // Optional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Add transaction details...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.trim()
                    val parsedAmt = amountStr.toDoubleOrNull()
                    
                    var valid = true
                    if (finalTitle.isEmpty()) {
                        titleError = "Title is required"
                        valid = false
                    }
                    if (parsedAmt == null || parsedAmt <= 0) {
                        amountError = "Enter a valid amount greater than 0"
                        valid = false
                    }

                    if (valid && parsedAmt != null) {
                        onSave(finalTitle, parsedAmt, isIncome, selectedCategory, timestamp, notes)
                    }
                },
                modifier = Modifier.testTag("button_save_transaction")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("button_cancel_transaction")
            ) {
                Text("Cancel")
            }
        }
    )
}
