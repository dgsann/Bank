package com.example.bank.model

data class Receipt(
    val id: Long,
    val dateMillis: Long,
    val store: String?,
    val category: ReceiptCategory,
    val amount: Double,
    val source: ReceiptSource,
    val items: List<ReceiptItem> = emptyList()
) {
    val totalAmount: Double get() = items.sumOf { it.price }.let { if (it == 0.0) amount else it }
}

data class ReceiptItem(
    val name: String,
    val price: Double,
    val category: ReceiptCategory
)
