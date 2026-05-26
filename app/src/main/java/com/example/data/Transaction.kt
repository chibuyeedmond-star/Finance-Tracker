package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
