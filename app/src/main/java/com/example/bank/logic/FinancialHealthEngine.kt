package com.example.bank.logic

import com.example.bank.model.AvatarStats
import com.example.bank.model.BudgetSettings
import com.example.bank.model.Discount
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

    private fun norm(value: Double, reference: Double): Double {
        if (reference <= 0.0) return 0.0
        return (value / reference).coerceIn(0.0, 1.0)
    }

    fun lifestyleStats(receipts: List<Receipt>, year: Int, month: Int): AvatarStats {
        val cat = spentByCategory(receipts, year, month)
        fun g(c: ReceiptCategory) = cat[c] ?: 0.0
        
        val health = (50.0
            + 40.0 * norm(g(ReceiptCategory.HEALTH), 6000.0)
            + 10.0 * norm(g(ReceiptCategory.PRODUCTS), 8000.0)
            - 30.0 * norm(g(ReceiptCategory.BAD_HABITS), 3000.0)
            - 20.0 * norm(g(ReceiptCategory.FAST_FOOD), 4000.0))
            .coerceIn(0.0, 100.0).roundToInt()
            
        val growth = (50.0
            + 50.0 * norm(g(ReceiptCategory.EDUCATION), 5000.0))
            .coerceIn(0.0, 100.0).roundToInt()
            
        val mood = (50.0
            + 35.0 * norm(g(ReceiptCategory.ENTERTAINMENT), 5000.0)
            + 15.0 * norm(g(ReceiptCategory.CAFE), 4000.0)
            + 10.0 * norm(g(ReceiptCategory.FAST_FOOD), 2000.0)
            - 25.0 * norm(g(ReceiptCategory.DEBTS), 5000.0))
            .coerceIn(0.0, 100.0).roundToInt()

        val comfort = (50.0
            + 30.0 * norm(g(ReceiptCategory.HOUSING), 10000.0)
            + 20.0 * norm(g(ReceiptCategory.SHOPPING), 7000.0))
            .coerceIn(0.0, 100.0).roundToInt()

        val energy = (50.0
            + 25.0 * norm(g(ReceiptCategory.PRODUCTS), 10000.0)
            + 25.0 * norm(g(ReceiptCategory.CAFE), 5000.0)
            - 40.0 * norm(g(ReceiptCategory.BAD_HABITS), 4000.0))
            .coerceIn(0.0, 100.0).roundToInt()

        val social = (50.0
            + 30.0 * norm(g(ReceiptCategory.CAFE), 6000.0)
            + 20.0 * norm(g(ReceiptCategory.ENTERTAINMENT), 4000.0))
            .coerceIn(0.0, 100.0).roundToInt()

        return AvatarStats(
            health = health, 
            growth = growth, 
            mood = mood,
            comfort = comfort,
            energy = energy,
            social = social
        )
    }

    fun housingInfo(saved: Double): Pair<String, String> = when {
        saved >= 100000 -> "🏰 Замок" to "💎 Роскошь и статус"
        saved >= 50000 -> "🏡 Коттедж" to "🌳 Уютный сад и забор"
        saved >= 20000 -> "🏠 Дом" to "🧱 Надежные стены"
        else -> "⛺ Палатка" to "🔥 Костер и свобода"
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

    fun isDiscountUnlocked(
        discount: Discount, receipts: List<Receipt>, year: Int, month: Int
    ): Boolean =
        (spentByCategory(receipts, year, month)[discount.category] ?: 0.0) >= discount.requiredAmount
}
