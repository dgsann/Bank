package com.example.bank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.bank.model.Receipt
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.components.GlassCard
import com.example.bank.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptsScreen(viewModel: MainViewModel) {
    val receipts by viewModel.receipts.collectAsState()
    var pendingDelete by remember { mutableStateOf<Receipt?>(null) }
    var selectedReceipt by remember { mutableStateOf<Receipt?>(null) }
    val dayFmt = remember { SimpleDateFormat("d MMMM", Locale("ru")) }
    val keyFmt = remember { SimpleDateFormat("yyyy-DDD", Locale("ru")) }
    val grouped = remember(receipts) {
        receipts
            .sortedByDescending { it.dateMillis }
            .groupBy { keyFmt.format(Date(it.dateMillis)) }
            .map { (_, sameDay) -> dayFmt.format(Date(sameDay.first().dateMillis)) to sameDay }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Чеки", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Итого за месяц", fontSize = 12.sp, color = TextSecondary)
                Text(
                    "${viewModel.monthlySpent().toInt()} ₽",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        if (receipts.isEmpty()) {
            Text(
                "Чеков пока нет. Добавьте первый через «＋».",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn {
                grouped.forEach { (day, dayReceipts) ->
                    item {
                        Text(
                            day.uppercase(Locale("ru")),
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(dayReceipts) { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .pointerInput(r.id) {
                                    detectTapGestures(
                                        onLongPress = { pendingDelete = r },
                                        onTap = { selectedReceipt = r }
                                    )
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${r.category.emoji} ${r.store ?: r.category.displayName} · ${r.category.displayName}",
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "−${r.amount.toInt()} ₽",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { r ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Удалить чек?") },
            text = {
                Text("${r.category.emoji} ${r.store ?: r.category.displayName} — ${r.amount.toInt()} ₽")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteReceipt(r.id)
                    pendingDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Отмена") }
            }
        )
    }

    selectedReceipt?.let { r ->
        AlertDialog(
            onDismissRequest = { selectedReceipt = null },
            title = {
                Column {
                    Text(
                        r.store ?: r.category.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(Date(r.dateMillis)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (r.items.isNotEmpty()) {
                        r.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    item.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    "${item.price.toInt()} ₽",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ИТОГО", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(
                            "${r.amount.toInt()} ₽",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedReceipt = null }) {
                    Text("Закрыть", color = Color(0xFF1565C0))
                }
            },
            containerColor = Color.White
        )
    }
}
