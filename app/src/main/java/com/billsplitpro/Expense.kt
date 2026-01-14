package com.billsplitpro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Unique ID for every item
    val name: String,
    val amount: Double
)