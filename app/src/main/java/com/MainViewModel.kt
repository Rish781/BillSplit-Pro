package com.billsplitpro // <--- This must match the folder!

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val expenses = mutableStateListOf<Expense>()

    fun addExpense(name: String, amount: Double) {
        expenses.add(Expense(name, amount))
    }

    fun removeExpense(expense: Expense) {
        expenses.remove(expense)
    }

    fun getTotal(): Double {
        return expenses.sumOf { it.amount }
    }
}