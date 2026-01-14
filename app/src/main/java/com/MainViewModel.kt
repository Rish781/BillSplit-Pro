package com.billsplitpro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.expenseDao()

    // This is now a "Live Stream" from the database
    val expenses: Flow<List<Expense>> = dao.getAllExpenses()

    fun addExpense(name: String, amount: Double) {
        viewModelScope.launch {
            dao.insert(Expense(name = name, amount = amount))
        }
    }

    fun removeExpense(expense: Expense) {
        viewModelScope.launch {
            dao.delete(expense)
        }
    }
}