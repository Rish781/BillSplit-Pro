package com.billsplitpro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // NEW IMPORT
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
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // NEW: specific command to make app fill the ENTIRE screen
        enableEdgeToEdge() 
        
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
    val currentFilter by viewModel.selectedEventFilter.collectAsState()

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var eventNameInput by remember { mutableStateOf("") }
    
    var personCount by remember { mutableIntStateOf(1) }
    var selectedCurrency by remember { mutableStateOf("INR") }
    var currencyExpanded by remember { mutableStateOf(false) }
    val conversionRate = viewModel.rates[selectedCurrency] ?: 1.0

    val categories = listOf("Food", "Travel", "Home", "Fun", "Other")
    var selectedCategory by remember { mutableStateOf("Food") }
    
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var showChart by remember { mutableStateOf(false) }
    var eventFilterExpanded by remember { mutableStateOf(false) }

    val totalInINR = expensesList.sumOf { it.amount }
    val displayedTotal = totalInINR * conversionRate
    val perPersonAmount = if (personCount > 0) displayedTotal / personCount else 0.0

    val currencySymbol = when(selectedCurrency) { "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; else -> "₹" }

    // UPDATED: Added safeDrawingPadding() so content doesn't hide behind the camera notch
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding() 
            .padding(20.dp)
    ) {
        
        // --- TOP ROW ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Event:", color = Color.Gray, fontSize = 16.sp)
            
            Box {
                Button(
                    onClick = { eventFilterExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(currentFilter, color = Color.White)
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                }
                
                DropdownMenu(expanded = eventFilterExpanded, onDismissRequest = { eventFilterExpanded = false }) {
                    DropdownMenuItem(text = { Text("All Events") }, onClick = { viewModel.setEventFilter("All Events"); eventFilterExpanded = false })
                    Divider()
                    listOf("Goa Trip", "Office", "Home", "Weekend").forEach { event ->
                        DropdownMenuItem(text = { Text(event) }, onClick = { viewModel.setEventFilter(event); eventFilterExpanded = false })
                    }
                }
            }
        }

        // --- HEADER ---
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total ($currentFilter)", color = Color.White.copy(0.8f), fontSize = 14.sp)
                        Row {
                            IconButton(onClick = { showChart = true }) { Icon(Icons.Default.Info, contentDescription = "Stats", tint = Color.White) }
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
                                DropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                                    listOf("INR", "USD", "EUR", "GBP").forEach { currency ->
                                        DropdownMenuItem(text = { Text(currency) }, onClick = { selectedCurrency = currency; currencyExpanded = false })
                                    }
                                }
                            }
                        }
                    }
                    Text("$currencySymbol${String.format("%.2f", displayedTotal)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Per Person: $currencySymbol${String.format("%.2f", perPersonAmount)}", color = Color(0xFF69F0AE), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- SPLIT BUTTONS ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)).padding(8.dp)) {
                IconButton(onClick = { if (personCount > 1) personCount-- }, modifier = Modifier.size(32.dp).background(Color(0xFF2C2C2C), CircleShape)) { Text("-", color = Color.White, fontWeight = FontWeight.Bold) }
                Text("$personCount", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                IconButton(onClick = { personCount++ }, modifier = Modifier.size(32.dp).background(Color(0xFF8E2DE2), CircleShape)) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            }
            Button(
                onClick = {
                    val shareText = "🧾 *BillSplit Pro ($currentFilter)*\n💰 Total: $currencySymbol${String.format("%.2f", displayedTotal)}\n👥 Split by $personCount\n👉 Each: $currencySymbol${String.format("%.2f", perPersonAmount)}"
                    val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, shareText); type = "text/plain" }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Bill"))
                },
                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) { Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Share") }
        }

        // --- INPUTS ---
        OutlinedTextField(
            value = eventNameInput, onValueChange = { eventNameInput = it }, label = { Text("Event Name (e.g. Goa)") },
            placeholder = { Text("Leave empty for 'Default'") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF8E2DE2), unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E1E1E), unfocusedContainerColor = Color(0xFF1E1E1E)),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 8.dp), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF8E2DE2), unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E1E1E), unfocusedContainerColor = Color(0xFF1E1E1E))
            )
            OutlinedTextField(
                value = amount, onValueChange = { amount = it }, label = { Text("Cost") }, singleLine = true,
                modifier = Modifier.weight(0.6f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF8E2DE2), unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E1E1E), unfocusedContainerColor = Color(0xFF1E1E1E))
            )
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected, onClick = { selectedCategory = category }, label = { Text(category) },
                        leadingIcon = { Icon(getIconForCategory(category), null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF8E2DE2), selectedLabelColor = Color.White, containerColor = Color(0xFF1E1E1E), labelColor = Color.Gray)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val cost = amount.toDoubleOrNull()
                    if (name.isNotBlank() && cost != null) {
                        val finalEvent = if (eventNameInput.isNotBlank()) eventNameInput else if (currentFilter != "All Events") currentFilter else "Default"
                        viewModel.addExpense(name, cost, selectedCategory, finalEvent)
                        name = ""
                        amount = ""
                    }
                },
                modifier = Modifier.height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2))
            ) { Icon(Icons.Default.Add, null) }
        }

        // --- LIST ---
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expensesList) { expense ->
                ExpenseItem(expense = expense, conversionRate = conversionRate, currencySymbol = currencySymbol, onDelete = { expenseToDelete = expense })
            }
        }

        if (expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { expenseToDelete = null }, title = { Text("Delete?") }, text = { Text("Delete '${expenseToDelete?.name}'?") },
                confirmButton = { TextButton(onClick = { viewModel.removeExpense(expenseToDelete!!); expenseToDelete = null }) { Text("Delete", color = Color(0xFFEF5350)) } },
                dismissButton = { TextButton(onClick = { expenseToDelete = null }) { Text("Cancel", color = Color.White) } }, containerColor = Color(0xFF2C2C2C), titleContentColor = Color.White, textContentColor = Color.Gray
            )
        }

        if (showChart && expensesList.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showChart = false }, title = { Text("Stats for $currentFilter", color = Color.White) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val categoryTotals = expensesList.groupBy { it.type }.mapValues { entry -> entry.value.sumOf { it.amount } }
                        val chartColors = listOf(Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFD54F), Color(0xFFBA68C8))
                        PieChart(data = categoryTotals, colors = chartColors, modifier = Modifier.size(200.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        categoryTotals.keys.forEachIndexed { index, category ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Box(modifier = Modifier.size(12.dp).background(chartColors.getOrElse(index) { Color.Gray }, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category, color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showChart = false }) { Text("Close", color = Color(0xFF8E2DE2)) } }, containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun ExpenseItem(expense: Expense, conversionRate: Double, currencySymbol: String, onDelete: () -> Unit) {
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
                    Text("${expense.type} • ${expense.eventName} • ${formatDate(expense.date)}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$currencySymbol${String.format("%.2f", displayAmount)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350)) }
            }
        }
    }
}

fun getIconForCategory(category: String): ImageVector {
    return when (category) { "Food" -> Icons.Default.ShoppingCart; "Travel" -> Icons.Default.Info; "Home" -> Icons.Default.Home; "Fun" -> Icons.Default.Star; else -> Icons.Default.List }
}