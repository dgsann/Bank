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
