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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.components.AvatarRing
import com.example.bank.ui.components.GlassCard
import com.example.bank.ui.components.StatBar
import com.example.bank.ui.theme.Accent
import com.example.bank.ui.theme.AccentGrowth
import com.example.bank.ui.theme.AccentHealth
import com.example.bank.ui.theme.BorderColor
import com.example.bank.ui.theme.DangerColor
import com.example.bank.ui.theme.TextSecondary

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val stats by viewModel.stats.collectAsState()
    val budget by viewModel.budget.collectAsState()
    viewModel.receipts.collectAsState().value // подписка: перерисовка при изменении чеков
    val spent = viewModel.monthlySpent()
    val income = budget.monthlyIncome
    val saved = income - spent
    val budgetProgress = if (income > 0) (spent / income).coerceIn(0.0, 1.0).toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarRing(financialHealth = stats.financialHealth)
        Text("Финансовое здоровье", fontSize = 12.sp, color = TextSecondary)
        Text(
            "${stats.financialHealth}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Accent
        )
        if (income <= 0.0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Укажите месячный доход в Профиле",
                fontSize = 12.sp,
                color = AccentHealth
            )
        }
        Spacer(Modifier.height(16.dp))

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Потрачено / Доход", fontSize = 12.sp, color = TextSecondary)
                Text(
                    "${spent.toInt()} / ${income.toInt()} ₽",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { budgetProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (spent > income && income > 0) DangerColor else Accent,
                trackColor = BorderColor
            )
        }
        Spacer(Modifier.height(10.dp))

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Накоплено за месяц", fontSize = 12.sp, color = TextSecondary)
                Text(
                    "${if (saved >= 0) "+" else ""}${saved.toInt()} ₽",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (saved >= 0) Accent else DangerColor
                )
            }
            if (budget.savingsGoal > 0) {
                val goalPct = (saved / budget.savingsGoal).coerceIn(0.0, 1.0)
                Text(
                    "Цель: ${budget.savingsGoal.toInt()} ₽  ·  ${(goalPct * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        GlassCard {
            Text(
                "ОБРАЗ ЖИЗНИ",
                fontSize = 10.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            StatBar("💪 Здоровье", stats.health, AccentHealth)
            StatBar("🧠 Развитие", stats.growth, AccentGrowth)
            StatBar("😊 Настроение", stats.mood, Accent)
        }
    }
}
