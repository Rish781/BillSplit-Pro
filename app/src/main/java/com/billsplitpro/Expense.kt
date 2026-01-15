package com.billsplitpro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val type: String,
    val eventName: String,
    val date: Long = System.currentTimeMillis() // NEW: Auto-saves current time
)