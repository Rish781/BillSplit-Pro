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
    val categories = listOf("Food", "Travel", "Home", "Fun", "Other")
    var selectedCategory by remember { mutableStateOf("Food") }
    
    // NEW: State to track which item we are about to delete
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val totalAmount = expensesList.sumOf { it.amount }
    val perPersonAmount = if (personCount > 0) totalAmount / personCount else 0.0

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
                    Text("Total Expenses", color = Color.White.copy(0.8f), fontSize = 14.sp)
                    Text("₹$totalAmount", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Per Person: ₹${String.format("%.2f", perPersonAmount)}", color = Color(0xFF69F0AE), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- SPLIT & SHARE ROW ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Split Counter
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)).padding(8.dp)) {
                IconButton(onClick = { if (personCount > 1) personCount-- }, modifier = Modifier.size(32.dp).background(Color(0xFF2C2C2C), CircleShape)) {
                    Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text("$personCount", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                IconButton(onClick = { personCount++ }, modifier = Modifier.size(32.dp).background(Color(0xFF8E2DE2), CircleShape)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // Share Button
            Button(
                onClick = {
                    val shareText = "💰 Total: ₹$totalAmount\n👥 Split by $personCount\n👉 Each: ₹${String.format("%.2f", perPersonAmount)}"
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
                label = { Text("Amount (₹)") },
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
                // CLICKING DELETE NOW TRIGGERS THE POPUP
                ExpenseItem(expense = expense, onDelete = { expenseToDelete = expense })
            }
        }

        // --- DELETE CONFIRMATION DIALOG ---
        if (expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { expenseToDelete = null },
                title = { Text(text = "Delete Expense?") },
                text = { Text("Are you sure you want to delete '${expenseToDelete?.name}'? This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.removeExpense(expenseToDelete!!)
                            expenseToDelete = null // Close dialog
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { expenseToDelete = null }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF2C2C2C),
                titleContentColor = Color.White,
                textContentColor = Color.Gray
            )
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2C2C2C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(getIconForCategory(expense.type), contentDescription = null, tint = Color.White.copy(0.8f))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(expense.name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(expense.type, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹${expense.amount}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                }
            }
        }
    }
}

fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.ShoppingCart
        "Travel" -> Icons.Default.Info
        "Home" -> Icons.Default.Home
        "Fun" -> Icons.Default.Star
        else -> Icons.Default.List
    }
}