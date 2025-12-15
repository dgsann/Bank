package com.example.bank.logic

import com.example.bank.model.AvatarState
import com.example.bank.model.TransactionCategory

object EvolutionEngine {
    fun calculateNewState(
        currentState: AvatarState,
        category: TransactionCategory,
        amount: Double
    ): AvatarState {
        var newState = currentState.copy()

        when (category) {
            TransactionCategory.SPORT -> {
                // Спорт дает силу, но чуть тратит энергию (настроение)
                val boost = 15
                newState = newState.copy(
                    strength = (newState.strength + boost).coerceAtMost(100),
                    mood = (newState.mood - 5).coerceAtLeast(0)
                )
            }
            TransactionCategory.EDUCATION -> {
                // Учеба дает интеллект
                val boost = 15
                val newIntellect = (newState.intellect + boost).coerceAtMost(100)
                newState = newState.copy(
                    intellect = newIntellect,
                    hasGlasses = newIntellect >= 50 // Логика очков
                )
            }
            TransactionCategory.FOOD -> {
                // Еда повышает настроение
                newState = newState.copy(mood = (newState.mood + 20).coerceAtMost(100))
            }
            else -> {
                newState = newState.copy(mood = (newState.mood + 5).coerceAtMost(100))
            }
        }

        // Повышаем уровень, если сумма статов большая
        val totalStats = newState.strength + newState.intellect + newState.mood
        val newLevel = 1 + (totalStats / 100)

        return newState.copy(level = newLevel)
    }
}