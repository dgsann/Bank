package com.example.bank.data

import android.content.Context
import com.example.bank.model.AvatarState

class AvatarStorage(context: Context) {
    private val prefs = context.getSharedPreferences("fintama_prefs", Context.MODE_PRIVATE)

    fun saveState(state: AvatarState) {
        prefs.edit().apply {
            putInt("level", state.level)
            putInt("strength", state.strength)
            putInt("intellect", state.intellect)
            putInt("mood", state.mood)
            putInt("energy", state.energy)
            putInt("creditScore", state.creditScore)

            putBoolean("hasGlasses", state.hasGlasses)
            putInt("houseLevel", state.houseLevel)

            // Финансы
            putLong("balance", java.lang.Double.doubleToRawLongBits(state.balance))
            putLong("depositBalance", java.lang.Double.doubleToRawLongBits(state.depositBalance))
            putLong("loanBalance", java.lang.Double.doubleToRawLongBits(state.loanBalance)) // <-- Сохраняем долг

            // Траты
            putLong("spentOnFood", java.lang.Double.doubleToRawLongBits(state.spentOnFood))
            putLong("spentOnSport", java.lang.Double.doubleToRawLongBits(state.spentOnSport))
            putLong("spentOnEducation", java.lang.Double.doubleToRawLongBits(state.spentOnEducation))

            apply()
        }
    }

    fun loadState(): AvatarState {
        val level = prefs.getInt("level", 1)
        val strength = prefs.getInt("strength", 0)
        val intellect = prefs.getInt("intellect", 0)
        val mood = prefs.getInt("mood", 50)
        val energy = prefs.getInt("energy", 100)
        val creditScore = prefs.getInt("creditScore", 300)

        val hasGlasses = prefs.getBoolean("hasGlasses", false)
        val houseLevel = prefs.getInt("houseLevel", 1)

        val balance = java.lang.Double.longBitsToDouble(prefs.getLong("balance", java.lang.Double.doubleToRawLongBits(10000.0)))
        val depositBalance = java.lang.Double.longBitsToDouble(prefs.getLong("depositBalance", 0))
        val loanBalance = java.lang.Double.longBitsToDouble(prefs.getLong("loanBalance", 0)) // <-- Загружаем долг

        val spentOnFood = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnFood", 0))
        val spentOnSport = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnSport", 0))
        val spentOnEducation = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnEducation", 0))

        return AvatarState(
            level, strength, intellect, mood, energy, creditScore,
            hasGlasses, houseLevel, balance, depositBalance, loanBalance, // <-- Вставляем сюда
            spentOnFood, spentOnSport, spentOnEducation
        )
    }
}