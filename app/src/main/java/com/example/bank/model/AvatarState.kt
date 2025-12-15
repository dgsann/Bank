package com.example.bank.model

data class AvatarState(
    val level: Int = 1,
    val strength: Int = 0,
    val intellect: Int = 0,
    val mood: Int = 50,
    val hasGlasses: Boolean = false,
    val houseLevel: Int = 1,
    val balance: Double = 10000.0,
    val spentOnFood: Double = 0.0,
    val spentOnSport: Double = 0.0,
    val spentOnEducation: Double = 0.0
)