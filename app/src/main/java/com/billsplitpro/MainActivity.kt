package com.billsplitpro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212) 
                ) {
                    BillSplitApp()
                }
            }
        }
    }
}

@Composable
fun BillSplitApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current

    // NEW: We "Collect" the live data from the database
    val expensesList by viewModel.expenses.collectAsState(initial = emptyList())

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var personCount by remember { mutableIntStateOf(1) }

    // Calculate total from the live list
    val totalAmount = expensesList.sumOf { it.amount }
    val perPersonAmount = if (personCount > 0) totalAmount / personCount else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // --- HEADER CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                        )
                    )
                    .padding(24.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Expenses", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text("₹$totalAmount", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Per Person", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(
                        text = "₹${String.format("%.2f", perPersonAmount)}",
                        color = Color(0xFF69F0AE),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- SPLIT COUNTER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Split Among", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (personCount > 1) personCount-- }, modifier = Modifier.background(Color(0xFF2C2C2C), CircleShape)) {
                    Text("-", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text("$personCount", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = { personCount++ }, modifier = Modifier.background(Color(0xFF8E2DE2), CircleShape)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                }
            }
        }

        // --- SHARE BUTTON ---
        Button(
            onClick = {
                val shareText = """
                    🧾 *BillSplit Pro Report*
                    ------------------------
                    💰 Total: ₹$totalAmount
                    👥 People: $personCount
                    👉 *Each Pays: ₹${String.format("%.2f", perPersonAmount)}*
                    ------------------------
                    Sent via BillSplit Pro
                """.trimIndent()

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share Bill via")
                context.startActivity(shareIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // --- INPUT SECTION ---
        Text("Add New Expense", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("What for?") }, singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8E2DE2), unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF8E2DE2), unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedContainerColor = Color(0xFF1E1E1E), focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1.5f).padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            OutlinedTextField(
                value = amount, onValueChange = { amount = it }, label = { Text("₹") }, singleLine = true,
                shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8E2DE2), unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF8E2DE2), unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedContainerColor = Color(0xFF1E1E1E), focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                val cost = amount.toDoubleOrNull()
                if (name.isNotBlank() && cost != null) {
                    viewModel.addExpense(name, cost)
                    name = ""
                    amount = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add to List", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Updated to use the live 'expensesList'
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(expensesList) { expense ->
                ExpenseItem(expense = expense, onDelete = { viewModel.removeExpense(expense) })
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2C2C2C)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = expense.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Expense", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "₹${expense.amount}", color = Color(0xFF4CAF50), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350)) }
            }
        }
    }
}