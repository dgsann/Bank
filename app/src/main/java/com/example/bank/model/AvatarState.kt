package com.example.bank.model

data class AvatarState(
    val level: Int = 1,
    val strength: Int = 0,
    val intellect: Int = 0,
    val mood: Int = 50,

    // НОВЫЕ ШКАЛЫ
    val energy: Int = 100,      // Энергия (по умолчанию полная)
    val creditScore: Int = 300, // Кредитный рейтинг (старт с 300, макс 850)

    val hasGlasses: Boolean = false,
    val houseLevel: Int = 1,
    val balance: Double = 10000.0,

    val spentOnFood: Double = 0.0,
    val spentOnSport: Double = 0.0,
    val spentOnEducation: Double = 0.0
)