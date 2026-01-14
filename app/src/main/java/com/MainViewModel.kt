package com.billsplitpro

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _expenses = mutableStateListOf<Expense>()
    val expenses: List<Expense> get() = _expenses

    fun addExpense(title: String, amount: Double, paidBy: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val expense = Expense(
            title = trimmed,
            amount = amount,
            paidBy = paidBy
        )
        // Add to top of the list
        _expenses.add(0, expense)
    }

    fun getTotal(): Double {
        return _expenses.sumOf { it.amount }
    }

    fun deleteExpense(expense: Expense) {
        _expenses.remove(expense)
    }
}