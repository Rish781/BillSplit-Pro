package com.billsplitpro

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.expenseDao()

    val expenses: Flow<List<Expense>> = dao.getAllExpenses()

    // NEW: Variable to hold live currency rates
    // We use a Map (e.g., "USD" -> 0.012, "EUR" -> 0.011)
    private val _rates = mutableStateMapOf<String, Double>()
    val rates: Map<String, Double> = _rates

    init {
        // 1. Set default INR rate
        _rates["INR"] = 1.0
        // 2. Fetch other currencies immediately
        fetchCurrencies()
    }

    private fun fetchCurrencies() {
        viewModelScope.launch {
            try {
                // Connect to internet and get data
                val response = CurrencyAPI.service.getRates()
                _rates.putAll(response.rates)
            } catch (e: Exception) {
                // If internet fails, we just silently stick with INR
                println("Error fetching rates: ${e.message}")
            }
        }
    }

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