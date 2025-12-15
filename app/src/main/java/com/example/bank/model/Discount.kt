package com.example.bank.model

data class Discount(
    val id: Int,
    val title: String,
    val description: String,
    val requiredLevel: Int,
    val color: Long,
    val requiredCategory: TransactionCategory,
    val requiredAmount: Double
)