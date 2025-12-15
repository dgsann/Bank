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
            putBoolean("hasGlasses", state.hasGlasses)
            putInt("houseLevel", state.houseLevel)
            putLong("balance", java.lang.Double.doubleToRawLongBits(state.balance))

            // СОХРАНЯЕМ ТРАТЫ
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
        val hasGlasses = prefs.getBoolean("hasGlasses", false)
        val houseLevel = prefs.getInt("houseLevel", 1)
        val balance = java.lang.Double.longBitsToDouble(prefs.getLong("balance", java.lang.Double.doubleToRawLongBits(10000.0)))

        // ЗАГРУЖАЕМ ТРАТЫ
        val spentOnFood = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnFood", 0))
        val spentOnSport = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnSport", 0))
        val spentOnEducation = java.lang.Double.longBitsToDouble(prefs.getLong("spentOnEducation", 0))

        return AvatarState(
            level, strength, intellect, mood, hasGlasses, houseLevel, balance,
            spentOnFood, spentOnSport, spentOnEducation
        )
    }
}