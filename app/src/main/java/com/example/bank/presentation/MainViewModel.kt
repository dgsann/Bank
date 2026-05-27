package com.example.bank.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bank.data.AppStorage
import com.example.bank.logic.FinancialHealthEngine
import com.example.bank.model.AvatarStats
import com.example.bank.model.BudgetSettings
import com.example.bank.model.Discount
import com.example.bank.model.Receipt
import com.example.bank.model.ReceiptItem
import com.example.bank.model.ReceiptCategory
import com.example.bank.model.ReceiptSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class MainViewModel(private val storage: AppStorage) : ViewModel() {

    private val _receipts = MutableStateFlow(storage.loadReceipts())
    val receipts = _receipts.asStateFlow()

    private val _budget = MutableStateFlow(storage.loadBudget())
    val budget = _budget.asStateFlow()

    private val _stats = MutableStateFlow(computeStats())
    val stats = _stats.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast = _toast.asStateFlow()

    // --- SCANNER STATE ---
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scannerStep = MutableStateFlow(ScannerStep.IDLE)
    val scannerStep = _scannerStep.asStateFlow()

    private val _scannedReceipt = MutableStateFlow<Receipt?>(null)
    val scannedReceipt = _scannedReceipt.asStateFlow()

    private val _isEmailImportEnabled = MutableStateFlow(false)
    val isEmailImportEnabled = _isEmailImportEnabled.asStateFlow()

    enum class ScannerStep { IDLE, CAPTURING, ANALYZING, RESULT }
    // ---------------------

    val discounts = listOf(
        Discount(1, "−10% в «Читай-город»", "За траты на образование", ReceiptCategory.EDUCATION, 4000.0),
        Discount(2, "−15% в FitnessHouse", "За траты на здоровье и спорт", ReceiptCategory.HEALTH, 6000.0),
        Discount(3, "Кэшбэк 5% в кафе", "За траты в кафе и ресторанах", ReceiptCategory.CAFE, 4000.0),
        Discount(4, "−7% в супермаркетах", "За траты на продукты", ReceiptCategory.PRODUCTS, 10000.0),
        Discount(5, "−20% на онлайн-кинотеатры", "За траты на развлечения", ReceiptCategory.ENTERTAINMENT, 3000.0)
    )

    private fun currentYearMonth(): Pair<Int, Int> {
        val c = Calendar.getInstance()
        return c.get(Calendar.YEAR) to (c.get(Calendar.MONTH) + 1)
    }

    private fun computeStats(): AvatarStats {
        val (y, m) = currentYearMonth()
        return FinancialHealthEngine.computeStats(_receipts.value, _budget.value, y, m)
    }

    private fun refresh() {
        _stats.value = computeStats()
    }

    fun addReceipt(amount: Double, category: ReceiptCategory, store: String?, dateMillis: Long, items: List<ReceiptItem> = emptyList()) {
        if (amount <= 0.0) {
            _error.value = "Сумма должна быть больше нуля"
            return
        }
        val nextId = (_receipts.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val receipt = Receipt(
            id = nextId,
            dateMillis = dateMillis,
            store = store?.trim()?.ifBlank { null },
            category = category,
            amount = amount,
            source = ReceiptSource.MANUAL,
            items = items
        )
        _receipts.value = listOf(receipt) + _receipts.value
        storage.saveReceipts(_receipts.value)
        refresh()
    }

    fun deleteReceipt(id: Long) {
        _receipts.value = _receipts.value.filterNot { it.id == id }
        storage.saveReceipts(_receipts.value)
        refresh()
    }

    fun setMonthlyIncome(value: Double) {
        if (value < 0.0) {
            _error.value = "Доход не может быть отрицательным"
            return
        }
        _budget.value = _budget.value.copy(monthlyIncome = value)
        storage.saveBudget(_budget.value)
        refresh()
    }

    fun setSavingsGoal(value: Double) {
        if (value < 0.0) {
            _error.value = "Цель не может быть отрицательной"
            return
        }
        _budget.value = _budget.value.copy(savingsGoal = value)
        storage.saveBudget(_budget.value)
        refresh()
    }

    fun monthlySpent(): Double {
        val (y, m) = currentYearMonth()
        return FinancialHealthEngine.monthlySpent(_receipts.value, y, m)
    }

    fun spentInCategory(category: ReceiptCategory): Double {
        val (y, m) = currentYearMonth()
        return FinancialHealthEngine.spentByCategory(_receipts.value, y, m)[category] ?: 0.0
    }

    fun activateDiscount(d: Discount) {
        val code = "FC-" + (1000..9999).random()
        _toast.value = "Промокод $code скопирован (демо). Скидки подтягиваются из партнёрской сети."
    }

    fun resetData() {
        storage.clearAll()
        _receipts.value = emptyList()
        _budget.value = BudgetSettings()
        refresh()
    }

    fun clearError() {
        _error.value = null
    }

    fun clearToast() {
        _toast.value = null
    }

    // --- SCANNER ACTIONS ---
    fun startScanning() {
        _isScanning.value = true
        _scannerStep.value = ScannerStep.CAPTURING
    }

    fun takePhoto() {
        _scannerStep.value = ScannerStep.ANALYZING
        // Симуляция распознавания (3 секунды для реализма видео)
        kotlin.concurrent.thread {
            Thread.sleep(3000)
            _scannedReceipt.value = Receipt(
                id = System.currentTimeMillis(),
                dateMillis = System.currentTimeMillis(),
                store = "Пятёрочка",
                category = ReceiptCategory.PRODUCTS,
                amount = 79.99,
                source = ReceiptSource.MANUAL,
                items = listOf(
                    ReceiptItem("ALP.GOLD Шок.ОР.мол.вк.ван/п85г", 79.99, ReceiptCategory.PRODUCTS)
                )
            )
            _scannerStep.value = ScannerStep.RESULT
        }
    }

    fun confirmScan() {
        val receipt = _scannedReceipt.value ?: return
        addReceipt(receipt.amount, receipt.category, receipt.store, receipt.dateMillis, receipt.items)
        closeScanner()
    }

    fun closeScanner() {
        _isScanning.value = false
        _scannerStep.value = ScannerStep.IDLE
        _scannedReceipt.value = null
    }

    fun importFromEmail() {
        _isEmailImportEnabled.value = !_isEmailImportEnabled.value
        
        if (_isEmailImportEnabled.value) {
            val emailReceiptItems = listOf(
                ReceiptItem("LAYS Чипсы карт.риф.вк.лоб.140г", 179.99, ReceiptCategory.PRODUCTS),
                ReceiptItem("РЖ.КРАЙ Хлеб нар.300г", 94.99, ReceiptCategory.PRODUCTS),
                ReceiptItem("КОЛОМ.Хлеб ДАРНИЦ.форм.нар.350г", 113.98, ReceiptCategory.PRODUCTS),
                ReceiptItem("[М+] САРАФ.Кефир дет.3,2% 930г", 109.99, ReceiptCategory.PRODUCTS),
                ReceiptItem("[М+] САРАФ.Кефир дет.3,2% 930г", 109.99, ReceiptCategory.PRODUCTS),
                ReceiptItem("MEN.Жев.рез.P.FR.вкус.арб.15,5г", 36.99, ReceiptCategory.PRODUCTS),
                ReceiptItem("Пакет ПЯТЕРОЧКА 65х40см", 9.99, ReceiptCategory.PRODUCTS)
            )
            
            val calendar = Calendar.getInstance().apply {
                set(2026, Calendar.MAY, 21, 17, 20)
            }

            addReceipt(
                amount = 655.92,
                category = ReceiptCategory.PRODUCTS,
                store = "Пятёрочка (Email)",
                dateMillis = calendar.timeInMillis,
                items = emailReceiptItems
            )
            _toast.value = "Синхронизация включена: чек из почты импортирован"
        } else {
            _toast.value = "Синхронизация с почтой отключена"
        }
    }
    // -----------------------

    companion object {
        fun factory(storage: AppStorage): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(storage) }
        }
    }
}
