package com.billsplitpro

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.expenseDao()

    // 1. Holds the currently selected event filter (Default = "All")
    val selectedEventFilter = MutableStateFlow("All Events")

    // 2. We combine the Database List with the Filter
    // If filter is "All Events", show everything. Otherwise, only show matches.
    val expenses: Flow<List<Expense>> = dao.getAllExpenses()
        .combine(selectedEventFilter) { list, filter ->
            if (filter == "All Events") list else list.filter { it.eventName == filter }
        }

    // 3. Currency Rates Logic (Same as before)
    private val _rates = mutableStateMapOf<String, Double>()
    val rates: Map<String, Double> = _rates

    init {
        _rates["INR"] = 1.0
        fetchCurrencies()
    }

    private fun fetchCurrencies() {
        viewModelScope.launch {
            try {
                val response = CurrencyAPI.service.getRates()
                _rates.putAll(response.rates)
            } catch (e: Exception) {
                println("Error fetching rates: ${e.message}")
            }
        }
    }

    // UPDATED: Now requires 'eventName'
    fun addExpense(name: String, amount: Double, type: String, event: String) {
        viewModelScope.launch {
            dao.insert(Expense(name = name, amount = amount, type = type, eventName = event))
        }
    }

    fun removeExpense(expense: Expense) {
        viewModelScope.launch {
            dao.delete(expense)
        }
    }
    
    fun setEventFilter(event: String) {
        selectedEventFilter.value = event
    }
}