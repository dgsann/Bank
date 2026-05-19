package com.example.bank.model

data class Receipt(
    val id: Long,
    val dateMillis: Long,
    val store: String?,
    val category: ReceiptCategory,
    val amount: Double,
    val source: ReceiptSource = ReceiptSource.MANUAL
)
