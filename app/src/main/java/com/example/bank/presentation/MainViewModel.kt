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

    // СПИСОК СКИДОК
    val discounts = listOf(
        Discount(1, "Вкусно и точка", "Кэшбэк 5% на бургеры", 2, 0xFFFFCC80, TransactionCategory.FOOD, 2000.0),
        Discount(2, "Читай-город", "Скидка 10% на книги", 3, 0xFF90CAF9, TransactionCategory.EDUCATION, 4000.0),
        Discount(3, "Спортмастер", "Скидка 20% на абонемент", 5, 0xFFEF9A9A, TransactionCategory.SPORT, 6000.0),
        Discount(4, "Пятерочка", "Повышенный кэшбэк 3%", 7, 0xFFA5D6A7, TransactionCategory.FOOD, 5000.0),
        Discount(5, "Premium Banking", "Бесплатное обслуживание", 10, 0xFFB39DDB, TransactionCategory.EDUCATION, 8000.0)
    )

    // --- ИНВЕСТИЦИИ (НОВОЕ) ---

    // 1. Пополнить вклад
    fun investMoney(amount: Double) {
        if (_state.value.balance < amount) {
            _errorEvent.value = "Недостаточно наличных!"
            return
        }
        _state.update {
            val newState = it.copy(
                balance = it.balance - amount,
                depositBalance = it.depositBalance + amount
            )
            storage.saveState(newState)
            newState
        }
    }

    // 2. Снять с вклада
    fun withdrawMoney(amount: Double) {
        if (_state.value.depositBalance < amount) {
            _errorEvent.value = "На вкладе нет такой суммы!"
            return
        }
        _state.update {
            val newState = it.copy(
                balance = it.balance + amount,
                depositBalance = it.depositBalance - amount
            )
            storage.saveState(newState)
            newState
        }
    }

    // 3. Расчет ставки (Зависит от Рейтинга)
    fun getCurrentInterestRate(): Double {
        val score = _state.value.creditScore
        // База 1% + 0.5% за каждые 100 баллов рейтинга выше 300
        val baseRate = 1.0
        val bonus = ((score - 300).coerceAtLeast(0) / 100.0) * 0.5
        return baseRate + bonus
    }

    // --- ОСНОВНАЯ ЛОГИКА ---

    fun onTransaction(category: TransactionCategory, amount: Double, description: String) {
        if (_state.value.balance < amount) {
            _errorEvent.value = "Недостаточно средств!"
            return
        }

        // Проверка энергии для спорта
        if (category == TransactionCategory.SPORT && _state.value.energy < 20) {
            _errorEvent.value = "Слишком устал для спорта! Нужно поспать."
            return
        }

        _state.update { currentState ->
            val oldLevel = currentState.level
            var newState = EvolutionEngine.calculateNewState(currentState, category, amount)
            newState = newState.copy(balance = currentState.balance - amount)

            // Обновление статов и трат
            newState = when(category) {
                TransactionCategory.SPORT -> newState.copy(
                    spentOnSport = newState.spentOnSport + amount,
                    energy = (newState.energy - 20).coerceAtLeast(0)
                )
                TransactionCategory.EDUCATION -> newState.copy(
                    spentOnEducation = newState.spentOnEducation + amount,
                    energy = (newState.energy - 5).coerceAtLeast(0)
                )
                TransactionCategory.FOOD -> newState.copy(
                    spentOnFood = newState.spentOnFood + amount,
                    energy = (newState.energy + 10).coerceAtMost(100)
                )
                else -> newState
            }

            // Штраф рейтингу, если денег мало
            if (newState.balance < 1000) {
                newState = newState.copy(creditScore = (newState.creditScore - 5).coerceAtLeast(0))
            }

            // Level Up
            if (newState.level > oldLevel) {
                val bonus = 5000.0
                newState = newState.copy(balance = newState.balance + bonus)
                _levelUpEvent.value = newState.level
                _history.update { list -> listOf("🎁 БОНУС УРОВНЯ (+${bonus.toInt()}₽)") + list }
            }

            newState = checkHouseUpgrade(newState)
            storage.saveState(newState)
            newState
        }
        _history.update { list -> listOf("$description (-${amount.toInt()}₽)") + list }
    }

    fun onSalary() {
        if (_state.value.energy < 30) {
            _errorEvent.value = "Вы валитесь с ног! Поспите."
            return
        }
        _state.update {
            var newState = it.copy(
                balance = it.balance + 15000.0,
                energy = (it.energy - 30).coerceAtLeast(0),
                creditScore = (it.creditScore + 10).coerceAtMost(850)
            )
            newState = checkHouseUpgrade(newState)
            storage.saveState(newState)
            newState
        }
        _history.update { listOf("💰 ЗАРПЛАТА (+15000₽)") + it }
    }

    // СОН (Восстановление + Проценты по вкладу)
    fun onSleep() {
        val rate = getCurrentInterestRate()
        val deposit = _state.value.depositBalance
        val profit = deposit * (rate / 100.0) // Простой дневной процент

        _state.update {
            val newState = it.copy(
                energy = 100,
                mood = (it.mood + 10).coerceAtMost(100),
                depositBalance = it.depositBalance + profit
            )
            storage.saveState(newState)
            newState
        }

        val msg = if (profit > 0) "🛏️ Сон: +${profit.toInt()}₽ (Вклад)" else "🛏️ Вы выспались!"
        _history.update { listOf(msg) + it }
    }

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
                        newState = newState.copy(balance = newState.balance - lost, creditScore = (newState.creditScore - 5).coerceAtLeast(0))
                        msg = "💸 Потерял 500₽"
                    } else msg = "😅 Чуть не потерял деньги"
                }
                randomValue < 55 -> {
                    newState = newState.copy(balance = newState.balance + 10000.0, mood = 100, creditScore = (newState.creditScore+20).coerceAtMost(850))
                    msg = "🎰 ДЖЕКПОТ! +10 000₽"
                    _levelUpEvent.value = newState.level
                }
                else -> {
                    newState = newState.copy(energy = (newState.energy - 10).coerceAtLeast(0))
                    msg = "🥱 Устал без причины"
                }
            }
            newState = checkHouseUpgrade(newState)
            storage.saveState(newState)
            newState
        }
        if (msg.isNotEmpty()) {
            _history.update { listOf(msg) + it }
            _errorEvent.value = msg
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ ---
    private fun checkHouseUpgrade(currentState: AvatarState): AvatarState {
        val balance = currentState.balance
        var newHouseLevel = 1
        if (balance >= 100000) newHouseLevel = 4
        else if (balance >= 50000) newHouseLevel = 3
        else if (balance >= 20000) newHouseLevel = 2
        return currentState.copy(houseLevel = newHouseLevel)
    }

    fun getNextHouseTarget(): Double {
        val balance = _state.value.balance
        return when {
            balance < 20000 -> 20000.0
            balance < 50000 -> 50000.0
            balance < 100000 -> 100000.0
            else -> 0.0
        }
    }

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