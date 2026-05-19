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

@Composable
fun DiscountsScreen(viewModel: MainViewModel) {
    val receipts by viewModel.receipts.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Скидки", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Подтягиваются из партнёрской сети банка",
            fontSize = 10.sp,
            color = TextTertiary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
}
