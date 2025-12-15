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
import androidx.compose.material.icons.filled.DateRange
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
import com.example.bank.model.AvatarState
import com.example.bank.model.Discount
import com.example.bank.model.TransactionCategory
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.theme.BankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = AvatarStorage(applicationContext)
        val viewModel: MainViewModel by viewModels { MainViewModel.factory(storage) }
        setContent {
            BankTheme {
                MainScreen(viewModel)
            }
        }
    }
}

// --- НАВИГАЦИЯ (3 ВКЛАДКИ) ---
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Главная") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, null) },
                    label = { Text("Бонусы") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Вклад") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF2F4F8))) {
            when (selectedTab) {
                0 -> TamagotchiScreen(viewModel)
                1 -> DiscountsScreen(viewModel)
                2 -> InvestmentsScreen(viewModel)
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

    LaunchedEffect(errorMsg) {
        errorMsg?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.clearError() }
    }
    if (newLevel != null) { LevelUpDialog(level = newLevel!!, onDismiss = { viewModel.dismissLevelUpDialog() }) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Баланс
            Text("Ваш баланс", color = Color.Gray, fontSize = 14.sp)
            Text("${avatarState.balance.toInt()} ₽", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(8.dp))

            // Карточка Дома
            HouseCard(avatarState.houseLevel, avatarState.balance, viewModel.getNextHouseTarget())
            Spacer(modifier = Modifier.height(8.dp))

            // Карточка Аватара
            Card(
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().weight(1f) // Занимает всё свободное место
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lvl ${avatarState.level}", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { viewModel.resetProgress() }) {
                            Text("Сброс", color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    // НОВАЯ УМНАЯ ЛОГИКА ЭМОДЗИ
                    val emoji = getAvatarEmoji(avatarState)

                    Text(
                        text = emoji,
                        fontSize = 80.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // 5 Шкал
                    StatBar("⚡ Энергия", avatarState.energy, Color(0xFFFFC107), 100)
                    Spacer(modifier = Modifier.height(4.dp))
                    StatBar("📈 Рейтинг", avatarState.creditScore, Color(0xFF9C27B0), 850)
                    Spacer(modifier = Modifier.height(4.dp))
                    StatBar("💪 Сила", avatarState.strength, Color(0xFFEF5350), 100)
                    Spacer(modifier = Modifier.height(4.dp))
                    StatBar("🧠 Ум", avatarState.intellect, Color(0xFF42A5F5), 100)
                    Spacer(modifier = Modifier.height(4.dp))
                    StatBar("😊 Счастье", avatarState.mood, Color(0xFF66BB6A), 100)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки (2 ряда)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ActionButton("📚 Книги\n-2k", Color(0xFFE3F2FD)) { viewModel.onTransaction(TransactionCategory.EDUCATION, 2000.0, "Книги") }
                ActionButton("🏋️ Спорт\n-3k", Color(0xFFFFEBEE)) { viewModel.onTransaction(TransactionCategory.SPORT, 3000.0, "Спортзал") }
                ActionButton("🍔 Еда\n-500", Color(0xFFE8F5E9)) { viewModel.onTransaction(TransactionCategory.FOOD, 500.0, "Еда") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = { viewModel.onSleep() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) { Text("🛏️ Поспать (День++)", fontSize = 14.sp) }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.onSalary() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) { Text("💰 Работа (+15k)", fontSize = 14.sp) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // История
            Text("История:", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            LazyColumn(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                items(history) { transaction ->
                    val isIncome = transaction.contains("+")
                    Text(text = transaction, color = if (isIncome) Color(0xFF2E7D32) else Color.Black, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(4.dp), fontSize = 11.sp)
                }
            }
        }

        // Кнопка рандома
        FloatingActionButton(
            onClick = { viewModel.triggerRandomEvent() },
            containerColor = Color(0xFFFFD700),
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) { Text("🎲", fontSize = 24.sp) }
    }
}

// --- ЭКРАН 2: СКИДКИ ---
@Composable
fun DiscountsScreen(viewModel: MainViewModel) {
    val avatarState by viewModel.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Награды 🎁", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text("Тратьте деньги в категориях, чтобы открывать скидки!", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.discounts) { discount ->
                val spending = viewModel.getSpendingProgress(discount)
                DiscountCard(discount, viewModel.isDiscountUnlocked(discount), spending)
            }
        }
    }
}

// --- ЭКРАН 3: ИНВЕСТИЦИИ ---
@Composable
fun InvestmentsScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    val interestRate = viewModel.getCurrentInterestRate()
    val dailyProfit = state.depositBalance * (interestRate / 100.0)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Инвестиции 💼", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

        // Карточка Вклада
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("На вашем счете", color = Color.Gray)
                Text(text = "${state.depositBalance.toInt()} ₽", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ставка: ", fontSize = 16.sp)
                    Text(text = String.format("%.1f%%", interestRate), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Text(" / день", fontSize = 14.sp, color = Color.Gray)
                }
                Text("(Зависит от Кредитного Рейтинга)", fontSize = 10.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ожидаемый доход за ночь:")
                Text("+${dailyProfit.toInt()} ₽", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Управление активами", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Пополнить", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = { viewModel.investMoney(1000.0) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("+ 1k") }
                Button(onClick = { viewModel.investMoney(5000.0) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("+ 5k") }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Снять", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = { viewModel.withdrawMoney(1000.0) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))) { Text("- 1k") }
                Button(onClick = { viewModel.withdrawMoney(state.depositBalance) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) { Text("Всё") }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text("Совет: Деньги работают, пока вы спите!", fontSize = 12.sp, color = Color.Gray)
    }
}

// --- УМНАЯ ЛОГИКА ЭМОДЗИ ---
fun getAvatarEmoji(state: AvatarState): String {
    return when {
        // 1. Критические состояния
        state.energy < 10 -> "😵" // Обморок
        state.mood < 15 -> "😭"   // Депрессия
        state.energy < 30 -> "😴" // Сонный

        // 2. Комбинации
        state.strength > 80 && state.intellect > 80 -> "😎" // Терминатор
        state.balance > 100000 && state.mood > 80 -> "🤑"   // Олигарх

        // 3. Роли
        state.strength > 80 -> "🦍" // Качок
        state.intellect > 80 -> "👽" // Мозг
        state.hasGlasses -> "🤓"    // Умник

        // 4. Настроение
        state.mood > 80 -> "🤩"
        state.mood < 40 -> "😒"

        else -> "🙂"
    }
}

// --- КОМПОНЕНТЫ ---
@Composable
fun HouseCard(level: Int, currentBalance: Double, nextTarget: Double) {
    val (houseEmoji, houseTitle) = when(level) {
        1 -> "⛺" to "Палатка"
        2 -> "🏠" to "Домик"
        3 -> "🏡" to "Коттедж"
        4 -> "🏰" to "Замок"
        else -> "⛺" to "Палатка"
    }
    Card(elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = houseEmoji, fontSize = 30.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column { Text("Жилье", fontSize = 10.sp, color = Color.Gray); Text(houseTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (nextTarget > 0) {
                val progress = (currentBalance / nextTarget).coerceIn(0.0, 1.0).toFloat()
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)), color = Color(0xFFFF9800), trackColor = Color.White)
            } else { Text("👑 Макс. уровень!", fontSize = 10.sp, color = Color(0xFFE65100)) }
        }
    }
}

@Composable
fun StatBar(label: String, value: Int, color: Color, maxVal: Int) {
    val animatedProgress by animateFloatAsState(targetValue = value / maxVal.toFloat(), animationSpec = tween(800), label = "")
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, modifier = Modifier.width(75.dp), fontWeight = FontWeight.Bold)
        LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(5.dp)), color = color, trackColor = Color(0xFFEEEEEE))
        Text(text = "$value", fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp).width(30.dp))
    }
}

@Composable
fun DiscountCard(discount: Discount, isUnlocked: Boolean, currentSpent: Double) {
    val cardColor = if (isUnlocked) Color(discount.color) else Color.White
    val progress = (currentSpent / discount.requiredAmount).coerceIn(0.0, 1.0).toFloat()
    Card(colors = CardDefaults.cardColors(containerColor = cardColor), border = if(isUnlocked) null else BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) { Text(discount.title, fontWeight = FontWeight.Bold); Text(discount.description, fontSize = 12.sp, color = Color.Gray) }
                Icon(if(isUnlocked) Icons.Default.ShoppingCart else Icons.Default.Lock, null, tint = if(isUnlocked) Color.Black else Color.Gray)
            }
            if(!isUnlocked) {
                val catName = when(discount.requiredCategory) { TransactionCategory.FOOD -> "Еда"; TransactionCategory.SPORT -> "Спорт"; TransactionCategory.EDUCATION -> "Книги"; else -> "Траты" }
                Text("$catName: ${currentSpent.toInt()} / ${discount.requiredAmount.toInt()} ₽", fontSize = 12.sp, color = Color.Gray)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top=4.dp).height(4.dp), color = Color(discount.color))
            } else { Text("✅ Активно", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)) }
        }
    }
}

@Composable
fun LevelUpDialog(level: Int, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { Button(onClick = onDismiss) { Text("OK") } }, title = { Text("Новый уровень: $level") }, text = { Text("Бонус: +5000₽") })
}

@Composable
fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), shape = RoundedCornerShape(12.dp), modifier = Modifier.width(100.dp).height(60.dp), contentPadding = PaddingValues(0.dp)) {
        Text(text, color = Color.Black, textAlign = TextAlign.Center, fontSize = 11.sp, lineHeight = 12.sp)
    }
}