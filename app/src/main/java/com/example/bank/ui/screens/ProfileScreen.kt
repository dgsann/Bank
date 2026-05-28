package com.example.bank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.components.GlassCard
import com.example.bank.ui.theme.DangerColor
import com.example.bank.ui.theme.TextSecondary

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val budget by viewModel.budget.collectAsState()
    val receipts by viewModel.receipts.collectAsState() // подписка: держит monthlySpent() актуальным
    var income by remember(budget.monthlyIncome) {
        mutableStateOf(if (budget.monthlyIncome > 0) budget.monthlyIncome.toInt().toString() else "")
    }
    var goal by remember(budget.savingsGoal) {
        mutableStateOf(if (budget.savingsGoal > 0) budget.savingsGoal.toInt().toString() else "")
    }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Профиль", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = income,
            onValueChange = { income = it.filter { c -> c.isDigit() } },
            label = { Text("Месячный доход, ₽") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it.filter { c -> c.isDigit() } },
            label = { Text("Цель накоплений в руб") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                viewModel.setMonthlyIncome(income.toDoubleOrNull() ?: 0.0)
                viewModel.setSavingsGoal(goal.toDoubleOrNull() ?: 0.0)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Сохранить", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(16.dp))
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Чеков всего", fontSize = 12.sp, color = TextSecondary)
                Text("${receipts.size}", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Потрачено за месяц", fontSize = 12.sp, color = TextSecondary)
                Text("${viewModel.monthlySpent().toInt()} ₽", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { showResetConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) { Text("Сбросить все данные") }

        Spacer(Modifier.height(16.dp))
        Text(
            "Финансовый компаньон · v2.0",
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Сбросить все данные?") },
            text = { Text("Будут удалены все чеки и настройки бюджета. Действие необратимо.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetData()
                    showResetConfirm = false
                }) { Text("Сбросить") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Отмена") }
            }
        )
    }
}
