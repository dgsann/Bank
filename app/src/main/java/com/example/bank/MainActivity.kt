package com.example.bank

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.data.AvatarStorage
import com.example.bank.model.Discount
import com.example.bank.model.TransactionCategory
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.theme.BankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Создаем хранилище
        val storage = AvatarStorage(applicationContext)

        // 2. Создаем ViewModel
        val viewModel: MainViewModel by viewModels { MainViewModel.factory(storage) }

        setContent {
            BankTheme {
                MainScreen(viewModel)
            }
        }
    }
}

// --- НАВИГАЦИЯ ---
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Главная") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Бонусы") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF2F4F8))) {
            when (selectedTab) {
                0 -> TamagotchiScreen(viewModel)
                1 -> DiscountsScreen(viewModel)
            }
        }
    }
}

// --- ЭКРАН 1: ТАМАГОЧИ ---
@Composable
fun TamagotchiScreen(viewModel: MainViewModel) {
    val avatarState by viewModel.state.collectAsState()
    val history by viewModel.history.collectAsState()
    val errorMsg by viewModel.errorEvent.collectAsState()
    val newLevel by viewModel.levelUpEvent.collectAsState()
    val context = LocalContext.current

    // Toast ошибки/сообщения
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // Диалог уровня
    if (newLevel != null) {
        LevelUpDialog(level = newLevel!!, onDismiss = { viewModel.dismissLevelUpDialog() })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Баланс
            Text("Ваш баланс", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "${avatarState.balance.toInt()} ₽",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // НОВОЕ: Карточка Дома
            HouseCard(
                level = avatarState.houseLevel,
                currentBalance = avatarState.balance,
                nextTarget = viewModel.getNextHouseTarget()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Карточка Аватара
            Card(
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lvl ${avatarState.level}", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { viewModel.resetProgress() }) {
                            Text("Сброс", color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    // Эмодзи аватара
                    val emoji = when {
                        avatarState.strength > 80 -> "💪"
                        avatarState.intellect > 80 -> "🧠"
                        avatarState.hasGlasses -> "🤓"
                        avatarState.mood < 30 -> "😭"
                        else -> "🙂"
                    }
                    Text(emoji, fontSize = 80.sp, modifier = Modifier.padding(vertical = 12.dp))

                    // Статы
                    StatBar("Сила", avatarState.strength, Color(0xFFEF5350))
                    Spacer(modifier = Modifier.height(4.dp))
                    StatBar("Интеллект", avatarState.intellect, Color(0xFF42A5F5))
                    Spacer(modifier = Modifier.height(4.dp))
                    StatBar("Настроение", avatarState.mood, Color(0xFF66BB6A))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопки
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ActionButton("📚 Книги\n-2000₽", Color(0xFFE3F2FD)) {
                    viewModel.onTransaction(TransactionCategory.EDUCATION, 2000.0, "Книги")
                }
                ActionButton("🏋️ Спорт\n-3000₽", Color(0xFFFFEBEE)) {
                    viewModel.onTransaction(TransactionCategory.SPORT, 3000.0, "Спортзал")
                }
                ActionButton("🍔 Еда\n-500₽", Color(0xFFE8F5E9)) {
                    viewModel.onTransaction(TransactionCategory.FOOD, 500.0, "Еда")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onSalary() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("💰 Получить зарплату (+15 000 ₽)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // История
            Text("История:", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                items(history) { transaction ->
                    val isIncome = transaction.contains("+")
                    Text(
                        text = transaction,
                        color = if (isIncome) Color(0xFF2E7D32) else Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Кнопка Рандома
        FloatingActionButton(
            onClick = { viewModel.triggerRandomEvent() },
            containerColor = Color(0xFFFFD700),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text("🎲", fontSize = 24.sp)
        }
    }
}

// --- ЭКРАН 2: СКИДКИ ---
@Composable
fun DiscountsScreen(viewModel: MainViewModel) {
    val avatarState by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ваши Награды 🎁", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        Text(
            text = "Тратьте деньги в категориях, чтобы открывать скидки!",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(viewModel.discounts) { discount ->
                val spending = viewModel.getSpendingProgress(discount)
                val unlocked = viewModel.isDiscountUnlocked(discount)

                DiscountCard(
                    discount = discount,
                    isUnlocked = unlocked,
                    currentSpent = spending
                )
            }
        }
    }
}

// --- НОВЫЙ КОМПОНЕНТ: КАРТОЧКА ДОМА ---
@Composable
fun HouseCard(level: Int, currentBalance: Double, nextTarget: Double) {
    val (houseEmoji, houseTitle) = when(level) {
        1 -> "⛺" to "Туристическая палатка"
        2 -> "🏠" to "Деревянный домик"
        3 -> "🏡" to "Уютный коттедж"
        4 -> "🏰" to "Личный замок"
        else -> "⛺" to "Палатка"
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Оранжевый оттенок
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = houseEmoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Ваше жилище", fontSize = 12.sp, color = Color.Gray)
                    Text(houseTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (nextTarget > 0) {
                val progress = (currentBalance / nextTarget).coerceIn(0.0, 1.0).toFloat()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Следующее улучшение:", fontSize = 12.sp, color = Color.Gray)
                    Text("${nextTarget.toInt()} ₽", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFF9800),
                    trackColor = Color.White
                )
            } else {
                Text("👑 Максимальный уровень жилья!", fontSize = 12.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ ---
@Composable
fun DiscountCard(discount: Discount, isUnlocked: Boolean, currentSpent: Double) {
    val cardColor = if (isUnlocked) Color(discount.color) else Color.White
    val contentColor = Color.Black
    val borderStroke = if (isUnlocked) null else BorderStroke(1.dp, Color.LightGray)
    val progress = (currentSpent / discount.requiredAmount).coerceIn(0.0, 1.0).toFloat()

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = borderStroke,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = discount.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = contentColor)
                    Text(text = discount.description, fontSize = 14.sp, color = Color.Gray)
                }
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.ShoppingCart else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Color.Black else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isUnlocked) {
                val categoryName = when(discount.requiredCategory) {
                    TransactionCategory.FOOD -> "Еда"
                    TransactionCategory.SPORT -> "Спорт"
                    TransactionCategory.EDUCATION -> "Книги"
                    else -> "Траты"
                }
                Text("$categoryName: ${currentSpent.toInt()} / ${discount.requiredAmount.toInt()} ₽", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(discount.color),
                    trackColor = Color(0xFFEEEEEE),
                )
            } else {
                Text("✅ Активно", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun LevelUpDialog(level: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700)) },
        title = { Text("Новый Уровень!", textAlign = TextAlign.Center) },
        text = { Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Поздравляем! Ваш аватар достиг уровня $level", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎁 Вам начислен бонус: +5000 ₽", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        }},
        confirmButton = { Button(onClick = onDismiss) { Text("Круто!") } }
    )
}

@Composable
fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(width = 100.dp, height = 70.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, color = Color.Black, textAlign = TextAlign.Center, fontSize = 11.sp, lineHeight = 12.sp)
    }
}

@Composable
fun StatBar(label: String, value: Int, color: Color) {
    val animatedProgress by animateFloatAsState(targetValue = value / 100f, animationSpec = tween(durationMillis = 800), label = "")
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, modifier = Modifier.width(70.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = Color(0xFFEEEEEE),
        )
        Text(text = "$value", fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp).width(25.dp))
    }
}