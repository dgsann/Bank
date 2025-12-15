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

    // --- ЛОГИКА КАРЬЕРЫ ---
    data class JobTier(val title: String, val salary: Double, val minIntellect: Int)

    fun getCurrentJob(): JobTier {
        val intellect = _state.value.intellect
        return when {
            intellect >= 100 -> JobTier("Гендиректор 👑", 200000.0, 100)
            intellect >= 80 -> JobTier("Топ-менеджер 💼", 80000.0, 80)
            intellect >= 50 -> JobTier("Тимлид 👨‍💻", 40000.0, 50)
            intellect >= 20 -> JobTier("Младший спец ☕", 15000.0, 20)
            else -> JobTier("Стажер 🧹", 5000.0, 0)
        }
    }

    // --- ФИНАНСЫ (ВКЛАД И КРЕДИТ) ---

    fun investMoney(amount: Double) {
        _state.update { currentState ->
            if (currentState.balance < amount) {
                _errorEvent.value = "Недостаточно наличных!"
                return@update currentState
            }
            val newState = currentState.copy(
                balance = currentState.balance - amount,
                depositBalance = currentState.depositBalance + amount
            )
            storage.saveState(newState)
            newState
        }
    }

    fun withdrawMoney(amount: Double) {
        _state.update { currentState ->
            if (currentState.depositBalance < amount) {
                _errorEvent.value = "На вкладе нет такой суммы!"
                return@update currentState
            }
            val newState = currentState.copy(
                balance = currentState.balance + amount,
                depositBalance = currentState.depositBalance - amount
            )
            storage.saveState(newState)
            newState
        }
    }

    fun takeLoan(amount: Double) {
        if (_state.value.creditScore < 200) {
            _errorEvent.value = "Банк отказал! Низкий рейтинг."
            return
        }
        val maxLoan = getCurrentJob().salary * 10
        if (_state.value.loanBalance + amount > maxLoan) {
            _errorEvent.value = "Лимит кредита превышен!"
            return
        }
        _state.update {
            val newState = it.copy(balance = it.balance + amount, loanBalance = it.loanBalance + amount)
            storage.saveState(newState)
            newState
        }
        _history.update { listOf("🏦 Взял кредит: +${amount.toInt()}₽") + it }
    }

    fun repayLoan(amount: Double) {
        val currentLoan = _state.value.loanBalance
        if (currentLoan <= 0) { _errorEvent.value = "Нет долгов!"; return }
        val payment = if (amount > currentLoan) currentLoan else amount

        _state.update { currentState ->
            if (currentState.balance < payment) {
                _errorEvent.value = "Недостаточно средств!"
                return@update currentState
            }
            val newState = currentState.copy(
                balance = currentState.balance - payment,
                loanBalance = currentState.loanBalance - payment,
                creditScore = (currentState.creditScore + 2).coerceAtMost(850)
            )
            storage.saveState(newState)
            newState
        }
        _history.update { listOf("💸 Погасил долг: -${payment.toInt()}₽") + it }
    }

    fun getCurrentInterestRate(): Double {
        val score = _state.value.creditScore
        val baseRate = 1.0
        val bonus = ((score - 300).coerceAtLeast(0) / 100.0) * 0.5
        return baseRate + bonus
    }

    // --- ТРАНЗАКЦИИ ---
    fun onTransaction(category: TransactionCategory, amount: Double, description: String) {
        _state.update { currentState ->
            if (currentState.balance < amount) {
                _errorEvent.value = "Недостаточно средств!"
                return@update currentState
            }
            if (category == TransactionCategory.SPORT && currentState.energy < 20) {
                _errorEvent.value = "Слишком устал для спорта! Нужно поспать."
                return@update currentState
            }

            val oldLevel = currentState.level
            var newState = EvolutionEngine.calculateNewState(currentState, category, amount)
            newState = newState.copy(balance = currentState.balance - amount)

            newState = when(category) {
                TransactionCategory.SPORT -> newState.copy(spentOnSport = newState.spentOnSport + amount, energy = (newState.energy - 20).coerceAtLeast(0))
                TransactionCategory.EDUCATION -> newState.copy(spentOnEducation = newState.spentOnEducation + amount, energy = (newState.energy - 5).coerceAtLeast(0))
                TransactionCategory.FOOD -> newState.copy(spentOnFood = newState.spentOnFood + amount, energy = (newState.energy + 10).coerceAtMost(100))
                else -> newState
            }

            if (newState.balance < 1000) {
                newState = newState.copy(creditScore = (newState.creditScore - 5).coerceAtLeast(0))
            }

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
        if (_errorEvent.value == null) _history.update { list -> listOf("$description (-${amount.toInt()}₽)") + list }
    }

    fun onSalary() {
        if (_state.value.energy < 30) {
            _errorEvent.value = "Вы валитесь с ног! Поспите."
            return
        }
        val job = getCurrentJob()
        _state.update {
            var newState = it.copy(balance = it.balance + job.salary, energy = (it.energy - 30).coerceAtLeast(0), creditScore = (it.creditScore + 5).coerceAtMost(850))
            newState = checkHouseUpgrade(newState)
            storage.saveState(newState)
            newState
        }
        _history.update { listOf("💰 ЗП (${job.title}): +${job.salary.toInt()}₽") + it }
    }

    // --- СОН (Расчет процентов, деградация статов) ---
    fun onSleep() {
        val depositRate = getCurrentInterestRate()
        val loanRate = 5.0

        _state.update { currentState ->
            val depositProfit = currentState.depositBalance * (depositRate / 100.0)
            val loanInterest = currentState.loanBalance * (loanRate / 100.0)

            val newStrength = (currentState.strength - 5).coerceAtLeast(0)
            val newIntellect = (currentState.intellect - 3).coerceAtLeast(0)
            val scorePenalty = if (currentState.loanBalance > 0) 1 else 0

            val newState = currentState.copy(
                energy = 100,
                mood = (currentState.mood + 10).coerceAtMost(100),
                depositBalance = currentState.depositBalance + depositProfit,
                loanBalance = currentState.loanBalance + loanInterest,
                strength = newStrength,
                intellect = newIntellect,
                creditScore = (currentState.creditScore - scorePenalty).coerceAtLeast(0)
            )
            storage.saveState(newState)

            var msg = "🛏️ Новый день."
            if (depositProfit > 0) msg += " Вклад: +${depositProfit.toInt()}."
            if (loanInterest > 0) msg += " Кредит: -${loanInterest.toInt()}."
            _history.update { listOf(msg) + it }

            newState
        }
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