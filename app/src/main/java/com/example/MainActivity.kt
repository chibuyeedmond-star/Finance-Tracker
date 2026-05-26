package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.TransactionRepository
import com.example.ui.ExpenseTrackerApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Instantiate our Room database and repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repos = TransactionRepository(database.transactionDao())

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        ExpenseTrackerApp(
          repository = repos,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
