package com.billsplitpro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.billsplitpro.ui.theme.BillSplitProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BillSplitProTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    BillSplitApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSplitApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val expensesList by viewModel.expenses.collectAsState(initial = emptyList())

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var personCount by remember { mutableIntStateOf(1) }
    
    // Currency States
    var selectedCurrency by remember { mutableStateOf("INR") }
    var currencyExpanded by remember { mutableStateOf(false) }
    val conversionRate = viewModel.rates[selectedCurrency] ?: 1.0

    // Categories
    val categories = listOf("Food", "Travel", "Home", "Fun", "Other")
    var selectedCategory by remember { mutableStateOf("Food") }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    // MATH: Calculate Total in INR first, then convert
    val totalInINR = expensesList.sumOf { it.amount }
    val displayedTotal = totalInINR * conversionRate
    val perPersonAmount = if (personCount > 0) displayedTotal / personCount else 0.0

    // Symbol helper
    val currencySymbol = when(selectedCurrency) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "₹"
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        
        // --- HEADER CARD ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))))
                    .padding(24.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    
                    // TITLE + CURRENCY DROPDOWN ROW
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Expenses", color = Color.White.copy(0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Small Currency Button
                        Box {
                            Button(
                                onClick = { currencyExpanded = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.2f)),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(selectedCurrency, fontSize = 10.sp, color = Color.White)
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                            }
                            
                            DropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false }
                            ) {
                                listOf("INR", "USD", "EUR", "GBP").forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            selectedCurrency = currency
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // DISPLAY TOTAL
                    Text(
                        text = "$currencySymbol${String.format("%.2f", displayedTotal)}", 
                        color = Color.White, 
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Per Person: $currencySymbol${String.format("%.2f", perPersonAmount)}", 
                        color = Color(0xFF69F0AE), 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- SPLIT & SHARE ROW ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)).padding(8.dp)) {
                IconButton(onClick = { if (personCount > 1) personCount-- }, modifier = Modifier.size(32.dp).background(Color(0xFF2C2C2C), CircleShape)) {
                    Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text("$personCount", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                IconButton(onClick = { personCount++ }, modifier = Modifier.size(32.dp).background(Color(0xFF8E2DE2), CircleShape)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Button(
                onClick = {
                    val shareText = "💰 Total: $currencySymbol${String.format("%.2f", displayedTotal)}\n👥 Split by $personCount\n👉 Each: $currencySymbol${String.format("%.2f", perPersonAmount)}"
                    val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, shareText); type = "text/plain" }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Bill"))
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }
        }

        // --- INPUT SECTION ---
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("What for?") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8E2DE2), unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E1E1E), unfocusedContainerColor = Color(0xFF1E1E1E)
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                label = { Text("Amount (INR)") },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8E2DE2), unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E1E), unfocusedContainerColor = Color(0xFF1E1E1E)
                )
            )
            
            Button(
                onClick = {
                    val cost = amount.toDoubleOrNull()
                    if (name.isNotBlank() && cost != null) {
                        viewModel.addExpense(name, cost, selectedCategory)
                        name = ""
                        amount = ""
                    }
                },
                modifier = Modifier.height(64.dp).aspectRatio(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        // --- CATEGORY SELECTOR ---
        LazyRow(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    leadingIcon = {
                        Icon(getIconForCategory(category), contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8E2DE2),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = Color(0xFF1E1E1E),
                        labelColor = Color.Gray
                    ),
                    border = FilterChipDefaults.filterChipBorder(borderColor = if(isSelected) Color.Transparent else Color.Gray)
                )
            }
        }

        // --- EXPENSE LIST ---
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expensesList) { expense ->
                // UPDATED: Now passing the conversion rate and symbol down to the item
                ExpenseItem(
                    expense = expense, 
                    conversionRate = conversionRate, 
                    currencySymbol = currencySymbol,
                    onDelete = { expenseToDelete = expense }
                )
            }
        }

        // --- DELETE DIALOG ---
        if (expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { expenseToDelete = null },
                title = { Text("Delete Expense?") },
                text = { Text("Delete '${expenseToDelete?.name}'?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.removeExpense(expenseToDelete!!); expenseToDelete = null }) {
                        Text("Delete", color = Color(0xFFEF5350))
                    }
                },
                dismissButton = { TextButton(onClick = { expenseToDelete = null }) { Text("Cancel", color = Color.White) } },
                containerColor = Color(0xFF2C2C2C), titleContentColor = Color.White, textContentColor = Color.Gray
            )
        }
    }
}

// UPDATED: Now accepts conversion rate and symbol
@Composable
fun ExpenseItem(expense: Expense, conversionRate: Double, currencySymbol: String, onDelete: () -> Unit) {
    // CALCULATE: Convert this specific item's price
    val displayAmount = expense.amount * conversionRate

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2C2C2C)), contentAlignment = Alignment.Center) {
                    Icon(getIconForCategory(expense.type), contentDescription = null, tint = Color.White.copy(0.8f))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(expense.name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(expense.type, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // SHOW: The converted amount with the correct symbol
                Text(
                    text = "$currencySymbol${String.format("%.2f", displayAmount)}", 
                    color = Color(0xFF4CAF50), 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350)) }
            }
        }
    }
}

fun getIconForCategory(category: String): ImageVector {
    return when (category) { "Food" -> Icons.Default.ShoppingCart; "Travel" -> Icons.Default.Info; "Home" -> Icons.Default.Home; "Fun" -> Icons.Default.Star; else -> Icons.Default.List }
}