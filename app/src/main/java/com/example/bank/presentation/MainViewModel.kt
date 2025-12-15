package com.example.bank.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bank.data.AvatarStorage
import com.example.bank.logic.EvolutionEngine
import com.example.bank.model.AvatarState
import com.example.bank.model.Discount
import com.example.bank.model.TransactionCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random // <--- Проверь, что этот импорт есть!

class MainViewModel(private val storage: AvatarStorage) : ViewModel() {

    // --- СОСТОЯНИЕ ---
    private val _state = MutableStateFlow(storage.loadState())
    val state = _state.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history = _history.asStateFlow()

    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent = _errorEvent.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent = _levelUpEvent.asStateFlow()

    // --- СКИДКИ ---
    val discounts = listOf(
        Discount(1, "Вкусно и точка", "Кэшбэк 5% на бургеры", 2, 0xFFFFCC80, TransactionCategory.FOOD, 2000.0),
        Discount(2, "Читай-город", "Скидка 10% на книги", 3, 0xFF90CAF9, TransactionCategory.EDUCATION, 4000.0),
        Discount(3, "Спортмастер", "Скидка 20% на абонемент", 5, 0xFFEF9A9A, TransactionCategory.SPORT, 6000.0),
        Discount(4, "Пятерочка", "Повышенный кэшбэк 3%", 7, 0xFFA5D6A7, TransactionCategory.FOOD, 5000.0),
        Discount(5, "Premium Banking", "Бесплатное обслуживание", 10, 0xFFB39DDB, TransactionCategory.EDUCATION, 8000.0)
    )

    // --- ТРАНЗАКЦИИ ---
    fun onTransaction(category: TransactionCategory, amount: Double, description: String) {
        val currentBalance = _state.value.balance

        if (currentBalance < amount) {
            _errorEvent.value = "Недостаточно средств! Нужна зарплата."
            return
        }

        _state.update { currentState ->
            val oldLevel = currentState.level

            // Расчет статов
            var newState = EvolutionEngine.calculateNewState(currentState, category, amount)
            // Списание денег
            newState = newState.copy(balance = currentState.balance - amount)

            // Обновление трат по категориям
            newState = when(category) {
                TransactionCategory.FOOD -> newState.copy(spentOnFood = newState.spentOnFood + amount)
                TransactionCategory.SPORT -> newState.copy(spentOnSport = newState.spentOnSport + amount)
                TransactionCategory.EDUCATION -> newState.copy(spentOnEducation = newState.spentOnEducation + amount)
                else -> newState
            }

            // Проверка уровня
            if (newState.level > oldLevel) {
                val bonus = 5000.0
                newState = newState.copy(balance = newState.balance + bonus)
                _levelUpEvent.value = newState.level
                _history.update { list -> listOf("🎁 БОНУС УРОВНЯ (+${bonus.toInt()}₽)") + list }
            }

            storage.saveState(newState)
            newState
        }

        _history.update { list -> listOf("$description (-${amount.toInt()}₽)") + list }
    }

    // --- ЗАРПЛАТА ---
    fun onSalary() {
        val salary = 15000.0
        _state.update { currentState ->
            val newState = currentState.copy(balance = currentState.balance + salary)
            storage.saveState(newState)
            newState
        }
        _history.update { list -> listOf("💰 ЗАРПЛАТА (+${salary.toInt()}₽)") + list }
    }

    // --- СЛУЧАЙНЫЕ СОБЫТИЯ (ИСПРАВЛЕНО: ТУРБО РЕЖИМ) ---
    fun triggerRandomEvent() {
        val randomValue = Random.nextInt(0, 100)
        var eventDescription = ""

        // Мы обновляем состояние сразу внутри update, чтобы не потерять данные
        _state.update { currentState ->
            var newState = currentState.copy()

            when {
                // 1. Нашел деньги (Шанс 0-25%)
                randomValue < 25 -> {
                    val foundMoney = 1000.0
                    newState = newState.copy(balance = newState.balance + foundMoney)
                    eventDescription = "🍀 Нашел 1000₽ на улице!"
                }

                // 2. Мелкая неудача (Шанс 25-45%)
                randomValue < 45 -> {
                    val lostMoney = 500.0
                    // Не уходим в минус
                    if (newState.balance >= lostMoney) {
                        newState = newState.copy(balance = newState.balance - lostMoney, mood = (newState.mood - 10).coerceAtLeast(0))
                        eventDescription = "💸 Потерял 500₽ (дырявый карман)"
                    } else {
                        eventDescription = "😅 Чуть не потерял деньги, но карманы пусты"
                    }
                }

                // 3. Джекпот (Шанс 45-55%) - Подняли шанс до 10%!
                randomValue < 55 -> {
                    val jackpot = 10000.0
                    newState = newState.copy(balance = newState.balance + jackpot, mood = 100)
                    eventDescription = "🎰 ДЖЕКПОТ! Выиграл 10 000₽!"
                    _levelUpEvent.value = newState.level // Салют
                }

                // 4. Заболел (Шанс 55-75%)
                randomValue < 75 -> {
                    val medCost = 1500.0
                    if (newState.balance >= medCost) {
                        newState = newState.copy(
                            balance = newState.balance - medCost,
                            strength = (newState.strength - 15).coerceAtLeast(0)
                        )
                        eventDescription = "🤒 Заболел. Лекарства: -1500₽"
                    } else {
                        newState = newState.copy(strength = (newState.strength - 20).coerceAtLeast(0))
                        eventDescription = "🤒 Заболел, а денег на лекарства нет..."
                    }
                }

                // 5. Просто хорошее настроение (75-100%) - Вместо "Ничего не случилось"
                else -> {
                    newState = newState.copy(mood = (newState.mood + 10).coerceAtMost(100))
                    eventDescription = "☀️ Отличная погода! Настроение улучшилось."
                }
            }

            // Сохраняем сразу
            storage.saveState(newState)
            newState
        }

        // Обновляем историю и показываем уведомление
        if (eventDescription.isNotEmpty()) {
            _history.update { listOf(eventDescription) + it }
            _errorEvent.value = eventDescription // Используем канал ошибок для показа Toast
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ ---
    fun resetProgress() {
        val newState = AvatarState()
        storage.saveState(newState)
        _state.value = newState
        _history.value = emptyList()
    }

    fun getSpendingProgress(discount: Discount): Double {
        return when(discount.requiredCategory) {
            TransactionCategory.FOOD -> _state.value.spentOnFood
            TransactionCategory.SPORT -> _state.value.spentOnSport
            TransactionCategory.EDUCATION -> _state.value.spentOnEducation
            else -> 0.0
        }
    }

    fun isDiscountUnlocked(discount: Discount): Boolean {
        return getSpendingProgress(discount) >= discount.requiredAmount
    }

    fun clearError() { _errorEvent.value = null }
    fun dismissLevelUpDialog() { _levelUpEvent.value = null }

    companion object {
        fun factory(storage: AvatarStorage): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(storage) }
        }
    }
}