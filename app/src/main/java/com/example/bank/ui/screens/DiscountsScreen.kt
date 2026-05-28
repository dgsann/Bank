package com.example.bank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.components.GlassCard
import com.example.bank.ui.theme.Accent
import com.example.bank.ui.theme.BorderColor
import com.example.bank.ui.theme.TextSecondary
import com.example.bank.ui.theme.TextTertiary

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.bank.model.Achievement
import com.example.bank.model.ReceiptCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscountsScreen(viewModel: MainViewModel) {
    val receipts by viewModel.receipts.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    var showAddAchievement by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Скидки и цели", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddAchievement = true },
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Поставить цель", fontSize = 12.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (achievements.isNotEmpty()) {
                item { Text("ВАШИ ДОСТИЖЕНИЯ", fontSize = 10.sp, color = TextSecondary) }
                items(achievements) { ach ->
                    AchievementItem(ach)
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor) }
            }

            item { Text("ДОСТУПНЫЕ СКИДКИ", fontSize = 10.sp, color = TextSecondary) }
            items(viewModel.discounts) { d ->
                val spent = remember(receipts) { viewModel.spentInCategory(d.category) }
                val unlocked = spent >= d.requiredAmount
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(d.title, fontWeight = FontWeight.Bold)
                        Text(if (unlocked) "✓" else "🔒", color = if (unlocked) Accent else TextSecondary)
                    }
                    Text(
                        d.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )
                    if (unlocked) {
                        Button(
                            onClick = { viewModel.activateDiscount(d) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Активировать промокод") }
                    } else {
                        Text(
                            "${d.category.displayName}: ${spent.toInt()} / ${d.requiredAmount.toInt()} ₽",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        LinearProgressIndicator(
                            progress = {
                                (spent / d.requiredAmount).coerceIn(0.0, 1.0).toFloat()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Accent,
                            trackColor = BorderColor
                        )
                    }
                }
            }
        }
    }

    if (showAddAchievement) {
        AddAchievementDialog(
            onDismiss = { showAddAchievement = false },
            onConfirm = { title, cat, days ->
                viewModel.addAchievement(title, cat, days)
                showAddAchievement = false
            }
        )
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(achievement.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(achievement.description, fontSize = 12.sp, color = TextSecondary)
            }
            Text(
                when {
                    achievement.isCompleted -> "✅ Готово"
                    achievement.isFailed -> "❌ Провалено"
                    else -> "⏳ В процессе"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    achievement.isCompleted -> Accent
                    achievement.isFailed -> Color.Red
                    else -> TextSecondary
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAchievementDialog(onDismiss: () -> Unit, onConfirm: (String, ReceiptCategory?, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf<ReceiptCategory?>(null) }
    var daysText by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая цель") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название (напр. Трезвость)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Категория для ограничения:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReceiptCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCat == cat,
                            onClick = { selectedCat = if (selectedCat == cat) null else cat },
                            label = { Text("${cat.emoji} ${cat.displayName}", fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it.filter { c -> c.isDigit() } },
                    label = { Text("Срок (дней)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onConfirm(title, selectedCat, daysText.toIntOrNull() ?: 1)
                }
            }) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
