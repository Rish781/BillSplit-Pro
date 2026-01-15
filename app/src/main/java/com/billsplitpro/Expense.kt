package com.billsplitpro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val type: String, // Category (Food, Travel)
    val eventName: String = "Default" // NEW: Event Name (Goa, Office)
)