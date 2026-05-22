package com.example.bank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.example.bank.model.ReceiptCategory
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.theme.BorderColor
import com.example.bank.ui.theme.TextSecondary
import com.example.bank.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddReceiptScreen(viewModel: MainViewModel, onSaved: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(ReceiptCategory.PRODUCTS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Новый чек", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { new -> amountText = new.filter { it.isDigit() || it == '.' } },
            label = { Text("Сумма, ₽") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text("Категория", fontSize = 11.sp, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ReceiptCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selected == cat,
                    onClick = { selected = cat },
                    label = { Text("${cat.emoji} ${cat.displayName}", fontSize = 12.sp) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = store,
            onValueChange = { store = it },
            label = { Text("Магазин (необязательно)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text("Дата: сегодня", fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                viewModel.addReceipt(
                    amount = amount,
                    category = selected,
                    store = store,
                    dateMillis = System.currentTimeMillis()
                )
                if (amount > 0.0) {
                    amountText = ""
                    store = ""
                    selected = ReceiptCategory.PRODUCTS
                    onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Сохранить чек", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(20.dp))
        Text("АВТОМАТИЧЕСКИЙ ИМПОРТ", fontSize = 10.sp, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {},
            enabled = false,
            colors = ButtonDefaults.buttonColors(disabledContainerColor = BorderColor),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) { Text("📧 Импорт с почты · скоро", color = TextTertiary) }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.startScanning() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) { Text("📷 Скан фото чека", color = Color.White) }
    }
}
