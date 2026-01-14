package com.billsplitpro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.expenseDao()

    val expenses: Flow<List<Expense>> = dao.getAllExpenses()

    // UPDATED: Now accepts a 'type'
    fun addExpense(name: String, amount: Double, type: String) {
        viewModelScope.launch {
            dao.insert(Expense(name = name, amount = amount, type = type))
        }
    }

    fun removeExpense(expense: Expense) {
        viewModelScope.launch {
            dao.delete(expense)
        }
    }
}