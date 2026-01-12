package com.billsplitpro

data class Expense(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val amount: Double,
    val paidBy: String
)