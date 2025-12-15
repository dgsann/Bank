package com.example.bank.data

import android.content.Context
import com.example.bank.model.AvatarState

class AvatarStorage(context: Context) {
    private val prefs = context.getSharedPreferences("fintama_prefs", Context.MODE_PRIVATE)

    fun saveState(state: AvatarState) {
        prefs.edit().apply {
            // Основные статы
            putInt("level", state.level)
            putInt("strength", state.strength)
            putInt("intellect", state.intellect)
            putInt("mood", state.mood)

            // НОВЫЕ ШКАЛЫ
            putInt("energy", state.energy)
            putInt("creditScore", state.creditScore)

            // Внешность и дом
            putBoolean("hasGlasses", state.hasGlasses)
            putInt("houseLevel", state.houseLevel)

            // Финансы (Double сохраняем как Long bits)
            putLong("balance", java.lang.Double.doubleToRawLongBits(state.balance))

            // Статистика трат (для умных скидок)
            putLong("spentOnFood", java.lang.Double.doubleToRawLongBits(state.spentOnFood))
            putLong("spentOnSport", java.lang.Double.doubleToRawLongBits(state.spentOnSport))
            putLong("spentOnEducation", java.lang.Double.doubleToRawLongBits(state.spentOnEducation))

            apply()
        }
    }

    fun loadState(): AvatarState {
        // Загрузка с дефолтными значениями
        val level = prefs.getInt("level", 1)
        val strength = prefs.getInt("strength", 0)
        val intellect = prefs.getInt("intellect", 0)
        val mood = prefs.getInt("mood", 50)

        // По умолчанию Энергия = 100, Рейтинг = 300
        val energy = prefs.getInt("energy", 100)
        val creditScore = prefs.getInt("creditScore", 300)

        val hasGlasses = prefs.getBoolean("hasGlasses", false)
        val houseLevel = prefs.getInt("houseLevel", 1)

        val balance = java.lang.Double.longBitsToDouble(
            prefs.getLong("balance", java.lang.Double.doubleToRawLongBits(10000.0))
        )

        val spentOnFood = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnFood", 0))
        val spentOnSport = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnSport", 0))
        val spentOnEducation = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnEducation", 0))

        return AvatarState(
            level = level,
            strength = strength,
            intellect = intellect,
            mood = mood,
            energy = energy,
            creditScore = creditScore,
            hasGlasses = hasGlasses,
            houseLevel = houseLevel,
            balance = balance,
            spentOnFood = spentOnFood,
            spentOnSport = spentOnSport,
            spentOnEducation = spentOnEducation
        )
    }
}