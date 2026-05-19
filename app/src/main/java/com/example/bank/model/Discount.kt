package com.example.bank.model

data class Discount(
    val id: Int,
    val title: String,
    val description: String,
    val category: ReceiptCategory,
    val requiredAmount: Double
)
