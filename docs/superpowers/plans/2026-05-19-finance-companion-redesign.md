# Финансовый компаньон — План реализации

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Переделать игру «Банк-тамагочи» в серьёзный трекер расходов: чеки вместо доходов, контроль по месячному доходу, эмодзи-аватар как индикатор финансового здоровья, скидки по категории, тёмная премиальная тема.

**Architecture:** Чистый детерминированный движок `FinancialHealthEngine` (юнит-тесты, без Android), хранение чеков и настроек в SharedPreferences через `org.json`, `MainViewModel` с `StateFlow`, экраны Compose с нижней навигацией. Старый игровой код (доходы/сон/вклады/кредиты/эволюция) удаляется.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, AndroidX Lifecycle ViewModel, `org.json` (встроен в Android SDK), JUnit4. Без новых зависимостей. minSdk 24 → дата через `java.util.Calendar` (не `java.time`).

**Спецификация:** `docs/superpowers/specs/2026-05-19-finance-companion-redesign-design.md`

**Команда тестов:** `./gradlew :app:testDebugUnitTest` (Windows: `.\gradlew.bat :app:testDebugUnitTest`).
**Команда компиляции:** `./gradlew :app:compileDebugKotlin`.
Если Gradle недоступен в среде исполнения — отметить это явно и выполнить сборку/проверку UI вручную в Android Studio (UI автоматически здесь не проверяется — это указано в спецификации).

---

## Структура файлов

Создаются:
- `app/src/main/java/com/example/bank/model/Receipt.kt`
- `app/src/main/java/com/example/bank/model/ReceiptCategory.kt`
- `app/src/main/java/com/example/bank/model/ReceiptSource.kt`
- `app/src/main/java/com/example/bank/model/BudgetSettings.kt`
- `app/src/main/java/com/example/bank/model/AvatarStats.kt`
- `app/src/main/java/com/example/bank/logic/FinancialHealthEngine.kt`
- `app/src/main/java/com/example/bank/data/AppStorage.kt`
- `app/src/main/java/com/example/bank/ui/components/AppComponents.kt` (AvatarRing, GlassCard)
- `app/src/main/java/com/example/bank/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/example/bank/ui/screens/AddReceiptScreen.kt`
- `app/src/main/java/com/example/bank/ui/screens/ReceiptsScreen.kt`
- `app/src/main/java/com/example/bank/ui/screens/DiscountsScreen.kt`
- `app/src/main/java/com/example/bank/ui/screens/ProfileScreen.kt`
- `app/src/test/java/com/example/bank/FinancialHealthEngineTest.kt`

Перезаписываются:
- `app/src/main/java/com/example/bank/model/Discount.kt`
- `app/src/main/java/com/example/bank/presentation/MainViewModel.kt`
- `app/src/main/java/com/example/bank/MainActivity.kt`
- `app/src/main/java/com/example/bank/ui/theme/Color.kt`
- `app/src/main/java/com/example/bank/ui/theme/Theme.kt`

Удаляются:
- `app/src/main/java/com/example/bank/model/AvatarState.kt`
- `app/src/main/java/com/example/bank/model/TransactionCategory.kt`
- `app/src/main/java/com/example/bank/logic/EvolutionEngine.kt`
- `app/src/main/java/com/example/bank/data/AvatarStorage.kt`
- Дублирующие файлы в корне репозитория (вне source set, мусор): `AvatarState.kt`, `AvatarStorage.kt`, `MainActivity.kt`, `MainViewModel.kt`

Не трогаем: `ui/theme/Type.kt` (содержит `Typography`).

---

## Task 1: Модель данных (новые файлы)

**Files:**
- Create: `app/src/main/java/com/example/bank/model/ReceiptCategory.kt`
- Create: `app/src/main/java/com/example/bank/model/ReceiptSource.kt`
- Create: `app/src/main/java/com/example/bank/model/Receipt.kt`
- Create: `app/src/main/java/com/example/bank/model/BudgetSettings.kt`
- Create: `app/src/main/java/com/example/bank/model/AvatarStats.kt`

Старые `AvatarState.kt` / `TransactionCategory.kt` пока НЕ трогаем (на них ещё ссылается старый код). Проект остаётся компилируемым: добавляем только новые файлы.

- [ ] **Step 1: Создать `ReceiptCategory.kt`**

```kotlin
package com.example.bank.model

enum class ReceiptCategory(val displayName: String, val emoji: String) {
    PRODUCTS("Продукты", "🛒"),
    CAFE("Кафе и рестораны", "☕"),
    TRANSPORT("Транспорт", "🚇"),
    HEALTH("Здоровье и спорт", "💪"),
    EDUCATION("Образование", "📚"),
    ENTERTAINMENT("Развлечения", "🎬"),
    SHOPPING("Покупки", "🛍️"),
    HOUSING("ЖКХ и дом", "🏠"),
    OTHER("Прочее", "•")
}
```

- [ ] **Step 2: Создать `ReceiptSource.kt`**

```kotlin
package com.example.bank.model

enum class ReceiptSource { MANUAL, EMAIL, PHOTO }
```

- [ ] **Step 3: Создать `Receipt.kt`**

```kotlin
package com.example.bank.model

data class Receipt(
    val id: Long,
    val dateMillis: Long,
    val store: String?,
    val category: ReceiptCategory,
    val amount: Double,
    val source: ReceiptSource = ReceiptSource.MANUAL
)
```

- [ ] **Step 4: Создать `BudgetSettings.kt`**

```kotlin
package com.example.bank.model

data class BudgetSettings(
    val monthlyIncome: Double = 0.0,
    val savingsGoal: Double = 0.0
)
```

- [ ] **Step 5: Создать `AvatarStats.kt`**

```kotlin
package com.example.bank.model

data class AvatarStats(
    val financialHealth: Int = 50,
    val health: Int = 50,
    val growth: Int = 50,
    val mood: Int = 50
)
```

- [ ] **Step 6: Проверить компиляцию**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (старый код не затронут, новые файлы компилируются).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/bank/model/ReceiptCategory.kt app/src/main/java/com/example/bank/model/ReceiptSource.kt app/src/main/java/com/example/bank/model/Receipt.kt app/src/main/java/com/example/bank/model/BudgetSettings.kt app/src/main/java/com/example/bank/model/AvatarStats.kt
git commit -m "feat: Новая модель данных (чеки, бюджет, статы аватара)"
```

---

## Task 2: Движок `FinancialHealthEngine` (TDD)

**Files:**
- Test: `app/src/test/java/com/example/bank/FinancialHealthEngineTest.kt`
- Create: `app/src/main/java/com/example/bank/logic/FinancialHealthEngine.kt`

Чистый Kotlin, без Android. Тестируется локальным JUnit (`./gradlew :app:testDebugUnitTest`).

- [ ] **Step 1: Написать падающий тест `FinancialHealthEngineTest.kt`**

```kotlin
package com.example.bank

import com.example.bank.logic.FinancialHealthEngine
import com.example.bank.model.AvatarStats
import com.example.bank.model.BudgetSettings
import com.example.bank.model.Receipt
import com.example.bank.model.ReceiptCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class FinancialHealthEngineTest {

    private fun dateOf(year: Int, month1to12: Int, day: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, month1to12 - 1, day, 12, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun receipt(
        year: Int, month: Int, day: Int,
        amount: Double, category: ReceiptCategory
    ) = Receipt(
        id = day.toLong() + month * 100L + year * 10000L,
        dateMillis = dateOf(year, month, day),
        store = null,
        category = category,
        amount = amount
    )

    @Test
    fun monthlySpent_countsOnlyGivenMonth() {
        val list = listOf(
            receipt(2026, 5, 10, 1000.0, ReceiptCategory.PRODUCTS),
            receipt(2026, 5, 20, 500.0, ReceiptCategory.CAFE),
            receipt(2026, 4, 30, 999.0, ReceiptCategory.PRODUCTS)
        )
        assertEquals(1500.0, FinancialHealthEngine.monthlySpent(list, 2026, 5), 0.001)
        assertEquals(999.0, FinancialHealthEngine.monthlySpent(list, 2026, 4), 0.001)
    }

    @Test
    fun financialHealth_noIncome_returnsNeutral50() {
        assertEquals(50, FinancialHealthEngine.financialHealth(0.0, 0.0, 0.0))
    }

    @Test
    fun financialHealth_breakEven_returns20() {
        assertEquals(20, FinancialHealthEngine.financialHealth(60000.0, 60000.0, 25000.0))
    }

    @Test
    fun financialHealth_overspend_scalesDown() {
        assertEquals(10, FinancialHealthEngine.financialHealth(60000.0, 90000.0, 25000.0))
    }

    @Test
    fun financialHealth_heavyOverspend_clampsToZero() {
        assertEquals(0, FinancialHealthEngine.financialHealth(60000.0, 150000.0, 0.0))
    }

    @Test
    fun financialHealth_reachingGoal_returns100() {
        assertEquals(100, FinancialHealthEngine.financialHealth(60000.0, 35000.0, 25000.0))
    }

    @Test
    fun financialHealth_halfwayToGoal_returns60() {
        assertEquals(60, FinancialHealthEngine.financialHealth(60000.0, 47500.0, 25000.0))
    }

    @Test
    fun lifestyleStats_educationRaisesGrowth() {
        val list = listOf(receipt(2026, 5, 5, 5000.0, ReceiptCategory.EDUCATION))
        val stats = FinancialHealthEngine.lifestyleStats(list, 2026, 5)
        assertEquals(100, stats.growth)
        assertEquals(50, stats.health)
        assertEquals(50, stats.mood)
    }

    @Test
    fun lifestyleStats_sportRaisesHealth_cafeLowersIt() {
        val list = listOf(
            receipt(2026, 5, 5, 6000.0, ReceiptCategory.HEALTH),
            receipt(2026, 5, 6, 5000.0, ReceiptCategory.CAFE)
        )
        val stats = FinancialHealthEngine.lifestyleStats(list, 2026, 5)
        assertEquals(85, stats.health)
        assertEquals(65, stats.mood)
        assertEquals(50, stats.growth)
    }

    @Test
    fun computeStats_combinesFinancialHealthAndLifestyle() {
        val list = listOf(
            receipt(2026, 5, 5, 5000.0, ReceiptCategory.EDUCATION),
            receipt(2026, 5, 6, 42500.0, ReceiptCategory.PRODUCTS)
        )
        val budget = BudgetSettings(monthlyIncome = 60000.0, savingsGoal = 25000.0)
        val stats = FinancialHealthEngine.computeStats(list, budget, 2026, 5)
        assertEquals(AvatarStats(financialHealth = 60, health = 60, growth = 100, mood = 50), stats)
    }

    @Test
    fun avatarEmoji_mapsThresholds() {
        assertEquals("🤩", FinancialHealthEngine.avatarEmoji(90))
        assertEquals("🙂", FinancialHealthEngine.avatarEmoji(75))
        assertEquals("😐", FinancialHealthEngine.avatarEmoji(55))
        assertEquals("😟", FinancialHealthEngine.avatarEmoji(35))
        assertEquals("😰", FinancialHealthEngine.avatarEmoji(10))
    }
}
```

Тест разблокировки скидки (`isDiscountUnlocked`) добавляется в Task 5 вместе с переработкой `Discount` — здесь его НЕТ, чтобы Task 2 был самодостаточным и компилировался.

- [ ] **Step 2: Запустить тест — убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.bank.FinancialHealthEngineTest"`
Expected: FAIL — `Unresolved reference: FinancialHealthEngine`.

- [ ] **Step 3: Создать `FinancialHealthEngine.kt`**

```kotlin
package com.example.bank.logic

import com.example.bank.model.AvatarStats
import com.example.bank.model.BudgetSettings
import com.example.bank.model.Receipt
import com.example.bank.model.ReceiptCategory
import java.util.Calendar
import kotlin.math.roundToInt

object FinancialHealthEngine {

    private fun yearMonthOf(millis: Long): Pair<Int, Int> {
        val c = Calendar.getInstance()
        c.timeInMillis = millis
        return c.get(Calendar.YEAR) to (c.get(Calendar.MONTH) + 1)
    }

    private fun inMonth(r: Receipt, year: Int, month: Int): Boolean {
        val (y, m) = yearMonthOf(r.dateMillis)
        return y == year && m == month
    }

    fun monthlySpent(receipts: List<Receipt>, year: Int, month: Int): Double =
        receipts.filter { inMonth(it, year, month) }.sumOf { it.amount }

    fun spentByCategory(
        receipts: List<Receipt>, year: Int, month: Int
    ): Map<ReceiptCategory, Double> =
        receipts.filter { inMonth(it, year, month) }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

    fun financialHealth(income: Double, monthlySpent: Double, savingsGoal: Double): Int {
        if (income <= 0.0) return 50
        val savings = income - monthlySpent
        if (savings <= 0.0) {
            val overspendRatio = (monthlySpent - income) / income
            return (20.0 * (1.0 - overspendRatio)).coerceIn(0.0, 20.0).roundToInt()
        }
        val savingsRate = savings / income
        val goalRate =
            if (savingsGoal > 0.0) (savingsGoal / income).coerceIn(0.01, 1.0) else 0.2
        val ratioToGoal = (savingsRate / goalRate).coerceIn(0.0, 1.0)
        return (20.0 + 80.0 * ratioToGoal).roundToInt().coerceIn(0, 100)
    }

    private fun norm(value: Double, reference: Double) =
        (value / reference).coerceIn(0.0, 1.0)

    fun lifestyleStats(receipts: List<Receipt>, year: Int, month: Int): AvatarStats {
        val cat = spentByCategory(receipts, year, month)
        fun g(c: ReceiptCategory) = cat[c] ?: 0.0
        val health = (50.0
            + 50.0 * norm(g(ReceiptCategory.HEALTH), 6000.0)
            + 10.0 * norm(g(ReceiptCategory.PRODUCTS), 8000.0)
            - 15.0 * norm(g(ReceiptCategory.CAFE), 5000.0))
            .coerceIn(0.0, 100.0).roundToInt()
        val growth = (50.0
            + 50.0 * norm(g(ReceiptCategory.EDUCATION), 5000.0))
            .coerceIn(0.0, 100.0).roundToInt()
        val mood = (50.0
            + 40.0 * norm(g(ReceiptCategory.ENTERTAINMENT), 5000.0)
            + 15.0 * norm(g(ReceiptCategory.CAFE), 4000.0))
            .coerceIn(0.0, 100.0).roundToInt()
        return AvatarStats(health = health, growth = growth, mood = mood)
    }

    fun computeStats(
        receipts: List<Receipt>, budget: BudgetSettings, year: Int, month: Int
    ): AvatarStats {
        val spent = monthlySpent(receipts, year, month)
        val fh = financialHealth(budget.monthlyIncome, spent, budget.savingsGoal)
        return lifestyleStats(receipts, year, month).copy(financialHealth = fh)
    }

    fun avatarEmoji(financialHealth: Int): String = when {
        financialHealth >= 85 -> "🤩"
        financialHealth >= 70 -> "🙂"
        financialHealth >= 50 -> "😐"
        financialHealth >= 30 -> "😟"
        else -> "😰"
    }
}
```

`isDiscountUnlocked` намеренно не включён сюда — он добавляется в Task 5 вместе с переработкой `Discount` (старый `Discount.kt` несовместим до Task 5).

- [ ] **Step 4: Запустить тесты — убедиться, что проходят**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.bank.FinancialHealthEngineTest"`
Expected: PASS (11 тестов).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/bank/logic/FinancialHealthEngine.kt app/src/test/java/com/example/bank/FinancialHealthEngineTest.kt
git commit -m "feat: FinancialHealthEngine с юнит-тестами (TDD)"
```

---

## Task 3: Хранилище `AppStorage`

**Files:**
- Create: `app/src/main/java/com/example/bank/data/AppStorage.kt`

Старый `AvatarStorage.kt` пока не трогаем. Новый файл компилируется рядом.

- [ ] **Step 1: Создать `AppStorage.kt`**

```kotlin
package com.example.bank.data

import android.content.Context
import com.example.bank.model.BudgetSettings
import com.example.bank.model.Receipt
import com.example.bank.model.ReceiptCategory
import com.example.bank.model.ReceiptSource
import org.json.JSONArray
import org.json.JSONObject

class AppStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences("finance_companion_prefs", Context.MODE_PRIVATE)

    fun loadReceipts(): List<Receipt> {
        val raw = prefs.getString("receipts", null) ?: return emptyList()
        val arr = JSONArray(raw)
        val result = ArrayList<Receipt>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            result.add(
                Receipt(
                    id = o.getLong("id"),
                    dateMillis = o.getLong("dateMillis"),
                    store = if (o.isNull("store")) null else o.getString("store"),
                    category = ReceiptCategory.valueOf(o.getString("category")),
                    amount = o.getDouble("amount"),
                    source = ReceiptSource.valueOf(o.optString("source", "MANUAL"))
                )
            )
        }
        return result
    }

    fun saveReceipts(receipts: List<Receipt>) {
        val arr = JSONArray()
        receipts.forEach { r ->
            val o = JSONObject()
            o.put("id", r.id)
            o.put("dateMillis", r.dateMillis)
            o.put("store", r.store ?: JSONObject.NULL)
            o.put("category", r.category.name)
            o.put("amount", r.amount)
            o.put("source", r.source.name)
            arr.put(o)
        }
        prefs.edit().putString("receipts", arr.toString()).apply()
    }

    fun loadBudget(): BudgetSettings = BudgetSettings(
        monthlyIncome = java.lang.Double.longBitsToDouble(prefs.getLong("monthlyIncome", 0L)),
        savingsGoal = java.lang.Double.longBitsToDouble(prefs.getLong("savingsGoal", 0L))
    )

    fun saveBudget(budget: BudgetSettings) {
        prefs.edit()
            .putLong("monthlyIncome", java.lang.Double.doubleToRawLongBits(budget.monthlyIncome))
            .putLong("savingsGoal", java.lang.Double.doubleToRawLongBits(budget.savingsGoal))
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
```

Примечание: `longBitsToDouble(0L) == 0.0`, поэтому дефолтный доход/цель = `0.0` корректно.

- [ ] **Step 2: Проверить компиляцию**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/bank/data/AppStorage.kt
git commit -m "feat: AppStorage — хранение чеков и бюджета (JSON в SharedPreferences)"
```

---

## Task 4: Тёмная премиальная тема

**Files:**
- Overwrite: `app/src/main/java/com/example/bank/ui/theme/Color.kt`
- Overwrite: `app/src/main/java/com/example/bank/ui/theme/Theme.kt`

`Type.kt` не трогаем — в нём объявлен `Typography`, на который ссылается `Theme.kt`.

- [ ] **Step 1: Перезаписать `Color.kt`**

```kotlin
package com.example.bank.ui.theme

import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF0E1116)
val SurfaceCard = Color(0xFF171C23)
val SurfaceBar = Color(0xFF13171D)
val BorderColor = Color(0xFF242B34)
val Accent = Color(0xFF34E29B)
val AccentHealth = Color(0xFFFF7A59)
val AccentGrowth = Color(0xFF5B8DEF)
val TextPrimary = Color(0xFFE8ECF1)
val TextSecondary = Color(0xFF8A93A0)
val TextTertiary = Color(0xFF5B6573)
val DangerColor = Color(0xFFFF6B6B)
```

- [ ] **Step 2: Перезаписать `Theme.kt`**

```kotlin
package com.example.bank.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Bg,
    secondary = AccentGrowth,
    onSecondary = Bg,
    background = Bg,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceBar,
    onSurfaceVariant = TextSecondary,
    error = DangerColor,
    onError = Bg,
    outline = BorderColor
)

@Composable
fun BankTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 3: Проверить компиляцию**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. (Старый `MainActivity` вызывает `BankTheme { ... }` — сигнатура совместима.)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/bank/ui/theme/Color.kt app/src/main/java/com/example/bank/ui/theme/Theme.kt
git commit -m "style: Тёмная премиальная тема (Dark Premium)"
```

---

## Task 5: Перезапись Discount + MainViewModel + временный MainActivity, удаление старого кода

Это «атомный» переход на новую модель. Старые `MainActivity`/`MainViewModel`/`EvolutionEngine`/`AvatarStorage`/`AvatarState`/`TransactionCategory`/старый `Discount` несовместимы — заменяем/удаляем вместе. `MainActivity` временно делаем минимальным (без полноценного UI), чтобы проект компилировался; полноценные экраны добавим в Task 6–10.

**Files:**
- Overwrite: `app/src/main/java/com/example/bank/model/Discount.kt`
- Overwrite: `app/src/main/java/com/example/bank/presentation/MainViewModel.kt`
- Overwrite: `app/src/main/java/com/example/bank/MainActivity.kt`
- Modify: `app/src/main/java/com/example/bank/logic/FinancialHealthEngine.kt` (добавить `isDiscountUnlocked` + импорт `Discount`)
- Modify: `app/src/test/java/com/example/bank/FinancialHealthEngineTest.kt` (добавить тест `isDiscountUnlocked_triggersAtThreshold` + импорты)
- Delete: `app/src/main/java/com/example/bank/model/AvatarState.kt`
- Delete: `app/src/main/java/com/example/bank/model/TransactionCategory.kt`
- Delete: `app/src/main/java/com/example/bank/logic/EvolutionEngine.kt`
- Delete: `app/src/main/java/com/example/bank/data/AvatarStorage.kt`

- [ ] **Step 1: Перезаписать `Discount.kt`**

```kotlin
package com.example.bank.model

data class Discount(
    val id: Int,
    val title: String,
    val description: String,
    val category: ReceiptCategory,
    val requiredAmount: Double
)
```

- [ ] **Step 2: Добавить `isDiscountUnlocked` в `FinancialHealthEngine.kt`**

Добавить импорт `import com.example.bank.model.Discount` (рядом с прочими `import com.example.bank.model.*`) и функцию последним членом `object FinancialHealthEngine` (перед закрывающей `}`):

```kotlin
    fun isDiscountUnlocked(
        discount: Discount, receipts: List<Receipt>, year: Int, month: Int
    ): Boolean =
        (spentByCategory(receipts, year, month)[discount.category] ?: 0.0) >= discount.requiredAmount
```

- [ ] **Step 3: Добавить тест `isDiscountUnlocked_triggersAtThreshold` в `FinancialHealthEngineTest.kt`**

Добавить импорты в начало файла:

```kotlin
import com.example.bank.model.Discount
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
```

Добавить тестовый метод последним членом класса `FinancialHealthEngineTest` (перед закрывающей `}`):

```kotlin
    @Test
    fun isDiscountUnlocked_triggersAtThreshold() {
        val d = Discount(1, "−10%", "Образование", ReceiptCategory.EDUCATION, 4000.0)
        val under = listOf(receipt(2026, 5, 1, 3000.0, ReceiptCategory.EDUCATION))
        val over = listOf(receipt(2026, 5, 1, 4000.0, ReceiptCategory.EDUCATION))
        assertFalse(FinancialHealthEngine.isDiscountUnlocked(d, under, 2026, 5))
        assertTrue(FinancialHealthEngine.isDiscountUnlocked(d, over, 2026, 5))
    }
```

- [ ] **Step 4: Перезаписать `MainViewModel.kt`**

```kotlin
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

    fun addReceipt(amount: Double, category: ReceiptCategory, store: String?, dateMillis: Long) {
        if (amount <= 0.0) {
            _error.value = "Сумма должна быть больше нуля"
            return
        }
        val receipt = Receipt(
            id = System.currentTimeMillis(),
            dateMillis = dateMillis,
            store = store?.trim()?.ifBlank { null },
            category = category,
            amount = amount,
            source = ReceiptSource.MANUAL
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

    fun isDiscountUnlocked(d: Discount): Boolean =
        spentInCategory(d.category) >= d.requiredAmount

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

    companion object {
        fun factory(storage: AppStorage): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(storage) }
        }
    }
}
```

- [ ] **Step 5: Перезаписать `MainActivity.kt` (временная заглушка для компиляции)**

```kotlin
package com.example.bank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bank.data.AppStorage
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.theme.BankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = AppStorage(applicationContext)
        val viewModel: MainViewModel by viewModels { MainViewModel.factory(storage) }
        setContent { BankTheme { RootPlaceholder(viewModel) } }
    }
}

@Composable
private fun RootPlaceholder(viewModel: MainViewModel) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Финансовый компаньон")
        }
    }
}
```

- [ ] **Step 6: Удалить старые файлы**

```bash
git rm app/src/main/java/com/example/bank/model/AvatarState.kt app/src/main/java/com/example/bank/model/TransactionCategory.kt app/src/main/java/com/example/bank/logic/EvolutionEngine.kt app/src/main/java/com/example/bank/data/AvatarStorage.kt
```

- [ ] **Step 7: Проверить компиляцию и тесты**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL; 12 тестов проходят (включая `isDiscountUnlocked_triggersAtThreshold`).

- [ ] **Step 8: Commit**

```bash
git add -A app/src/main/java/com/example/bank app/src/test/java/com/example/bank
git commit -m "feat: Переход на модель чеков (ViewModel, Discount), удалён игровой код"
```

---

## Task 6: Переиспользуемые компоненты + экран «Главная»

**Files:**
- Create: `app/src/main/java/com/example/bank/ui/components/AppComponents.kt`
- Create: `app/src/main/java/com/example/bank/ui/screens/HomeScreen.kt`
- Modify: `app/src/main/java/com/example/bank/MainActivity.kt` (показать `HomeScreen`)

- [ ] **Step 1: Создать `AppComponents.kt`**

```kotlin
package com.example.bank.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.logic.FinancialHealthEngine
import com.example.bank.ui.theme.Accent
import com.example.bank.ui.theme.BorderColor
import com.example.bank.ui.theme.SurfaceCard

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun AvatarRing(financialHealth: Int, modifier: Modifier = Modifier) {
    val clamped = financialHealth.coerceIn(0, 100)
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(150.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(
                color = BorderColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = Accent,
                startAngle = -90f,
                sweepAngle = 360f * (clamped / 100f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        Text(text = FinancialHealthEngine.avatarEmoji(clamped), fontSize = 56.sp)
    }
}

@Composable
fun StatBar(label: String, value: Int, barColor: androidx.compose.ui.graphics.Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = com.example.bank.ui.theme.TextSecondary)
            Text("$value", fontSize = 12.sp, color = com.example.bank.ui.theme.TextPrimary)
        }
        androidx.compose.material3.LinearProgressIndicator(
            progress = { value.coerceIn(0, 100) / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = BorderColor
        )
    }
}
```

- [ ] **Step 2: Создать `HomeScreen.kt`**

```kotlin
package com.example.bank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.components.AvatarRing
import com.example.bank.ui.components.GlassCard
import com.example.bank.ui.components.StatBar
import com.example.bank.ui.theme.Accent
import com.example.bank.ui.theme.AccentGrowth
import com.example.bank.ui.theme.AccentHealth
import com.example.bank.ui.theme.BorderColor
import com.example.bank.ui.theme.TextSecondary

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val stats by viewModel.stats.collectAsState()
    val budget by viewModel.budget.collectAsState()
    viewModel.receipts.collectAsState().value // подписка: перерисовка при изменении чеков
    val spent = viewModel.monthlySpent()
    val income = budget.monthlyIncome
    val saved = income - spent
    val budgetProgress = if (income > 0) (spent / income).coerceIn(0.0, 1.0).toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarRing(financialHealth = stats.financialHealth)
        Text("Финансовое здоровье", fontSize = 12.sp, color = TextSecondary)
        Text(
            "${stats.financialHealth}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Accent
        )
        if (income <= 0.0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Укажите месячный доход в Профиле",
                fontSize = 12.sp,
                color = AccentHealth
            )
        }
        Spacer(Modifier.height(16.dp))

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Потрачено / Доход", fontSize = 12.sp, color = TextSecondary)
                Text(
                    "${spent.toInt()} / ${income.toInt()} ₽",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { budgetProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (spent > income && income > 0) com.example.bank.ui.theme.DangerColor else Accent,
                trackColor = BorderColor
            )
        }
        Spacer(Modifier.height(10.dp))

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Накоплено за месяц", fontSize = 12.sp, color = TextSecondary)
                Text(
                    "${if (saved >= 0) "+" else ""}${saved.toInt()} ₽",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (saved >= 0) Accent else com.example.bank.ui.theme.DangerColor
                )
            }
            if (budget.savingsGoal > 0) {
                val goalPct = (saved / budget.savingsGoal).coerceIn(0.0, 1.0)
                Text(
                    "Цель: ${budget.savingsGoal.toInt()} ₽  ·  ${(goalPct * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        GlassCard {
            Text(
                "ОБРАЗ ЖИЗНИ",
                fontSize = 10.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            StatBar("💪 Здоровье", stats.health, AccentHealth)
            StatBar("🧠 Развитие", stats.growth, AccentGrowth)
            StatBar("😊 Настроение", stats.mood, Accent)
        }
    }
}
```

- [ ] **Step 3: Обновить `MainActivity.kt` — показать `HomeScreen`**

Заменить тело `setContent`:

```kotlin
        setContent { BankTheme { com.example.bank.ui.screens.HomeScreen(viewModel) } }
```

и удалить `RootPlaceholder` (больше не нужен).

- [ ] **Step 4: Проверить компиляцию**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/bank/ui/components/AppComponents.kt app/src/main/java/com/example/bank/ui/screens/HomeScreen.kt app/src/main/java/com/example/bank/MainActivity.kt
git commit -m "feat: Экран «Главная» + компоненты AvatarRing/GlassCard/StatBar"
```

---

## Task 7: Экран «Добавить чек»

**Files:**
- Create: `app/src/main/java/com/example/bank/ui/screens/AddReceiptScreen.kt`

- [ ] **Step 1: Создать `AddReceiptScreen.kt`**

```kotlin
package com.example.bank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.model.ReceiptCategory
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.theme.BorderColor
import com.example.bank.ui.theme.TextSecondary
import com.example.bank.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddReceiptScreen(viewModel: MainViewModel, onSaved: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(ReceiptCategory.PRODUCTS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Новый чек", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { new -> amountText = new.filter { it.isDigit() || it == '.' } },
            label = { Text("Сумма, ₽") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text("Категория", fontSize = 11.sp, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ReceiptCategory.values().forEach { cat ->
                FilterChip(
                    selected = selected == cat,
                    onClick = { selected = cat },
                    label = { Text("${cat.emoji} ${cat.displayName}", fontSize = 12.sp) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = store,
            onValueChange = { store = it },
            label = { Text("Магазин (необязательно)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text("Дата: сегодня", fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                viewModel.addReceipt(
                    amount = amount,
                    category = selected,
                    store = store,
                    dateMillis = System.currentTimeMillis()
                )
                if (amount > 0.0) {
                    amountText = ""
                    store = ""
                    onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Сохранить чек", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(20.dp))
        Text("АВТОМАТИЧЕСКИЙ ИМПОРТ", fontSize = 10.sp, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {},
            enabled = false,
            colors = ButtonDefaults.buttonColors(disabledContainerColor = BorderColor),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) { Text("📧 Импорт с почты · скоро", color = TextTertiary) }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {},
            enabled = false,
            colors = ButtonDefaults.buttonColors(disabledContainerColor = BorderColor),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) { Text("📷 Скан фото чека · скоро", color = TextTertiary) }
    }
}
```

- [ ] **Step 2: Проверить компиляцию**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/bank/ui/screens/AddReceiptScreen.kt
git commit -m "feat: Экран «Добавить чек» (ручной ввод + неактивные импорт/скан)"
```

---

## Task 8: Экран «Чеки»

**Files:**
- Create: `app/src/main/java/com/example/bank/ui/screens/ReceiptsScreen.kt`

- [ ] **Step 1: Создать `ReceiptsScreen.kt`**

```kotlin
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
    val dayFmt = remember { SimpleDateFormat("d MMMM", Locale("ru")) }
    val grouped = receipts
        .sortedByDescending { it.dateMillis }
        .groupBy { dayFmt.format(Date(it.dateMillis)) }

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
                                    detectTapGestures(onLongPress = { pendingDelete = r })
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
}
```

- [ ] **Step 2: Проверить компиляцию**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/bank/ui/screens/ReceiptsScreen.kt
git commit -m "feat: Экран «Чеки» (список по дням, удаление с подтверждением)"
```

---

## Task 9: Экран «Скидки»

**Files:**
- Create: `app/src/main/java/com/example/bank/ui/screens/DiscountsScreen.kt`

- [ ] **Step 1: Создать `DiscountsScreen.kt`**

```kotlin
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
```

- [ ] **Step 2: Проверить компиляцию**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/bank/ui/screens/DiscountsScreen.kt
git commit -m "feat: Экран «Скидки» (порог по категории, заглушка промокода)"
```

---

## Task 10: Экран «Профиль» + сборка навигации в `MainActivity`

**Files:**
- Create: `app/src/main/java/com/example/bank/ui/screens/ProfileScreen.kt`
- Overwrite: `app/src/main/java/com/example/bank/MainActivity.kt`

- [ ] **Step 1: Создать `ProfileScreen.kt`**

```kotlin
package com.example.bank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.components.GlassCard
import com.example.bank.ui.theme.DangerColor
import com.example.bank.ui.theme.TextSecondary

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val budget by viewModel.budget.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    var income by remember(budget.monthlyIncome) {
        mutableStateOf(if (budget.monthlyIncome > 0) budget.monthlyIncome.toInt().toString() else "")
    }
    var goal by remember(budget.savingsGoal) {
        mutableStateOf(if (budget.savingsGoal > 0) budget.savingsGoal.toInt().toString() else "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Профиль", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = income,
            onValueChange = { income = it.filter { c -> c.isDigit() } },
            label = { Text("Месячный доход, ₽") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it.filter { c -> c.isDigit() } },
            label = { Text("Цель накоплений в месяц, ₽") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                viewModel.setMonthlyIncome(income.toDoubleOrNull() ?: 0.0)
                viewModel.setSavingsGoal(goal.toDoubleOrNull() ?: 0.0)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Сохранить", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(16.dp))
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Чеков всего", fontSize = 12.sp, color = TextSecondary)
                Text("${receipts.size}", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Потрачено за месяц", fontSize = 12.sp, color = TextSecondary)
                Text("${viewModel.monthlySpent().toInt()} ₽", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.resetData() },
            colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) { Text("Сбросить все данные") }

        Spacer(Modifier.height(16.dp))
        Text(
            "Финансовый компаньон · v2.0",
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
```

- [ ] **Step 2: Перезаписать `MainActivity.kt` — нижняя навигация + роутинг + обработка error/toast**

```kotlin
package com.example.bank

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.bank.data.AppStorage
import com.example.bank.presentation.MainViewModel
import com.example.bank.ui.screens.AddReceiptScreen
import com.example.bank.ui.screens.DiscountsScreen
import com.example.bank.ui.screens.HomeScreen
import com.example.bank.ui.screens.ProfileScreen
import com.example.bank.ui.screens.ReceiptsScreen
import com.example.bank.ui.theme.BankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = AppStorage(applicationContext)
        val viewModel: MainViewModel by viewModels { MainViewModel.factory(storage) }
        setContent { BankTheme { MainScreen(viewModel) } }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val error by viewModel.error.collectAsState()
    val toast by viewModel.toast.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Главная") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Чеки") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.AddCircle, null) },
                    label = { Text("Добавить") }
                )
                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { tab = 3 },
                    icon = { Icon(Icons.Default.Star, null) },
                    label = { Text("Скидки") }
                )
                NavigationBarItem(
                    selected = tab == 4,
                    onClick = { tab = 4 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Профиль") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (tab) {
                0 -> HomeScreen(viewModel)
                1 -> ReceiptsScreen(viewModel)
                2 -> AddReceiptScreen(viewModel, onSaved = { tab = 0 })
                3 -> DiscountsScreen(viewModel)
                4 -> ProfileScreen(viewModel)
            }
        }
    }
}
```

- [ ] **Step 3: Проверить компиляцию и тесты**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL; 12 тестов проходят.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/bank/ui/screens/ProfileScreen.kt app/src/main/java/com/example/bank/MainActivity.kt
git commit -m "feat: Экран «Профиль» + нижняя навигация (5 экранов)"
```

---

## Task 11: Очистка и финальная проверка

**Files:**
- Delete: дублирующие файлы в корне репозитория `AvatarState.kt`, `AvatarStorage.kt`, `MainActivity.kt`, `MainViewModel.kt` (вне `app/src` — мусор, не участвуют в сборке)

- [ ] **Step 1: Удалить мусорные дубликаты в корне (если присутствуют в git)**

```bash
git ls-files | grep -E "^(AvatarState|AvatarStorage|MainActivity|MainViewModel)\.kt$"
```
Если команда выводит файлы — удалить их:
```bash
git rm AvatarState.kt AvatarStorage.kt MainActivity.kt MainViewModel.kt
```
Если вывод пустой — пропустить (они не отслеживаются git, шаг не нужен).

- [ ] **Step 2: Полная сборка debug-APK**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. Если Gradle/SDK недоступны в среде исполнения — зафиксировать это явно и выполнить сборку в Android Studio.

- [ ] **Step 3: Все юнит-тесты**

Run: `./gradlew :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL; `FinancialHealthEngineTest` — 12 тестов PASS; `ExampleUnitTest` — PASS.

- [ ] **Step 4: Ручная проверка UI (чек-лист, в Android Studio / на устройстве)**

UI автоматически не проверяется (указано в спецификации). Прогнать вручную:
- Профиль: ввести доход 60000, цель 25000 → Сохранить. Главная: «Укажите доход» исчезла, кольцо/число обновились.
- Добавить чек: сумма 5000, категория «Образование» → Сохранить → переход на Главную, «Развитие» вырос, «Потрачено» обновилось.
- Добавить пустую сумму → Toast «Сумма должна быть больше нуля».
- Чеки: чек виден, сгруппирован по дню; долгое нажатие → диалог → Удалить → исчез, статы пересчитались.
- Скидки: после трат на образование ≥ 4000 ₽ скидка «Читай-город» разблокирована; «Активировать» → Toast с промокодом; заблокированные показывают прогресс.
- Профиль: «Сбросить все данные» → всё обнулилось, доход пуст, Главная нейтральная (50, 😐).
- Перезапуск приложения: чеки и доход сохранились.
- Внешний вид: тёмная премиальная тема, навигация из 5 пунктов.

- [ ] **Step 5: Commit (если на Step 1 что-то удалялось)**

```bash
git commit -m "chore: Удалены мусорные дубликаты файлов из корня репозитория"
```
Если удалять было нечего — коммит пропустить.

---

## Карта покрытия спецификации

- Концепция/удаление игрового кода → Task 5 (удаление), Task 1–4 (новая основа)
- Модель данных (§3) → Task 1, Task 5 (Discount)
- Движок (§4): monthlySpent / financialHealth / lifestyleStats / avatarEmoji / income≤0→50 → Task 2
- Хранение (§6) → Task 3
- ViewModel/поток данных (§6) → Task 5
- Тёмная тема (§6) → Task 4
- Экраны (§5): Главная/Чеки/Добавить/Скидки/Профиль + навигация → Task 6–10
- Эмодзи-аватар в кольце → Task 6 (AvatarRing)
- Скидки по категории + заглушка промокода (§1, §5) → Task 5 (логика), Task 9 (экран)
- Обработка ошибок на границе ввода (§6) → Task 5 (валидация), Task 10 (Toast)
- Тестирование (§7) → Task 2 (TDD движка), Task 11 (чек-лист UI)
- Миграция (§8): новый ключ `finance_companion_prefs`, старые ключи не читаются → Task 3
- YAGNI (§9): без новых зависимостей, без date picker, без email/OCR, без лимитов по категориям → соблюдено во всех задачах
