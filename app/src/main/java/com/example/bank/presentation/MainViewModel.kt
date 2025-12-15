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
import kotlin.random.Random

class MainViewModel(private val storage: AvatarStorage) : ViewModel() {

    private val _state = MutableStateFlow(storage.loadState())
    val state = _state.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history = _history.asStateFlow()

    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent = _errorEvent.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent = _levelUpEvent.asStateFlow()

    val discounts = listOf(
        Discount(1, "Вкусно и точка", "Кэшбэк 5% на бургеры", 2, 0xFFFFCC80, TransactionCategory.FOOD, 2000.0),
        Discount(2, "Читай-город", "Скидка 10% на книги", 3, 0xFF90CAF9, TransactionCategory.EDUCATION, 4000.0),
        Discount(3, "Спортмастер", "Скидка 20% на абонемент", 5, 0xFFEF9A9A, TransactionCategory.SPORT, 6000.0),
        Discount(4, "Пятерочка", "Повышенный кэшбэк 3%", 7, 0xFFA5D6A7, TransactionCategory.FOOD, 5000.0),
        Discount(5, "Premium Banking", "Бесплатное обслуживание", 10, 0xFFB39DDB, TransactionCategory.EDUCATION, 8000.0)
    )

    // --- ЛОГИКА ДОМА (НОВОЕ) ---
    // Проверяем баланс и обновляем уровень дома
    private fun checkHouseUpgrade(currentState: AvatarState): AvatarState {
        val balance = currentState.balance
        var newHouseLevel = 1 // 1 - Палатка

        if (balance >= 100000) newHouseLevel = 4 // Замок
        else if (balance >= 50000) newHouseLevel = 3 // Коттедж
        else if (balance >= 20000) newHouseLevel = 2 // Деревянный дом

        // Если уровень дома изменился в большую сторону - можно добавить салют, но пока просто меняем
        return currentState.copy(houseLevel = newHouseLevel)
    }

    // Возвращает цель для следующего уровня дома (для прогресс-бара)
    fun getNextHouseTarget(): Double {
        val balance = _state.value.balance
        return when {
            balance < 20000 -> 20000.0
            balance < 50000 -> 50000.0
            balance < 100000 -> 100000.0
            else -> 0.0 // Максимальный уровень
        }
    }

    // --- ТРАНЗАКЦИИ ---
    fun onTransaction(category: TransactionCategory, amount: Double, description: String) {
        if (_state.value.balance < amount) {
            _errorEvent.value = "Недостаточно средств!"
            return
        }

        _state.update { currentState ->
            val oldLevel = currentState.level
            var newState = EvolutionEngine.calculateNewState(currentState, category, amount)
            newState = newState.copy(balance = currentState.balance - amount)

            // Обновляем категории
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

            // ПРОВЕРКА ДОМА (Даунгрейд, если потратил накопления)
            newState = checkHouseUpgrade(newState)

            storage.saveState(newState)
            newState
        }
        _history.update { list -> listOf("$description (-${amount.toInt()}₽)") + list }
    }

    // --- ЗАРПЛАТА ---
    fun onSalary() {
        _state.update {
            var newState = it.copy(balance = it.balance + 15000.0)
            // ПРОВЕРКА ДОМА (Апгрейд)
            newState = checkHouseUpgrade(newState)
            storage.saveState(newState)
            newState
        }
        _history.update { listOf("💰 ЗАРПЛАТА (+15000₽)") + it }
    }

    // --- РАНДОМ ---
    fun triggerRandomEvent() {
        val randomValue = Random.nextInt(0, 100)
        var msg = ""

        _state.update { currentState ->
            var newState = currentState.copy()
            when {
                randomValue < 25 -> {
                    newState = newState.copy(balance = newState.balance + 1000.0)
                    msg = "🍀 Нашел 1000₽!"
                }
                randomValue < 45 -> {
                    val lost = 500.0
                    if(newState.balance >= lost) {
                        newState = newState.copy(balance = newState.balance - lost, mood = (newState.mood-10).coerceAtLeast(0))
                        msg = "💸 Потерял 500₽"
                    } else msg = "😅 Почти потерял деньги"
                }
                randomValue < 55 -> {
                    newState = newState.copy(balance = newState.balance + 10000.0, mood = 100)
                    msg = "🎰 ДЖЕКПОТ! +10 000₽"
                    _levelUpEvent.value = newState.level
                }
                randomValue < 75 -> {
                    val cost = 1500.0
                    if (newState.balance >= cost) {
                        newState = newState.copy(balance = newState.balance - cost, strength = (newState.strength-15).coerceAtLeast(0))
                        msg = "🤒 Лекарства -1500₽"
                    } else {
                        newState = newState.copy(strength = (newState.strength-20).coerceAtLeast(0))
                        msg = "🤒 Заболел..."
                    }
                }
                else -> {
                    newState = newState.copy(mood = (newState.mood+10).coerceAtMost(100))
                    msg = "☀️ Хороший день!"
                }
            }
            // ПРОВЕРКА ДОМА
            newState = checkHouseUpgrade(newState)
            storage.saveState(newState)
            newState
        }
        if (msg.isNotEmpty()) {
            _history.update { listOf(msg) + it }
            _errorEvent.value = msg
        }
    }

    // --- Вспомогательные ---
    fun resetProgress() {
        val newState = AvatarState()
        storage.saveState(newState)
        _state.value = newState
        _history.value = emptyList()
    }
    fun getSpendingProgress(d: Discount) = when(d.requiredCategory) {
        TransactionCategory.FOOD -> _state.value.spentOnFood
        TransactionCategory.SPORT -> _state.value.spentOnSport
        TransactionCategory.EDUCATION -> _state.value.spentOnEducation
        else -> 0.0
    }
    fun isDiscountUnlocked(d: Discount) = getSpendingProgress(d) >= d.requiredAmount
    fun clearError() { _errorEvent.value = null }
    fun dismissLevelUpDialog() { _levelUpEvent.value = null }

    companion object {
        fun factory(storage: AvatarStorage): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(storage) }
        }
    }
}