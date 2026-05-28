package com.example.bank.model

import java.util.UUID

data class Achievement(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetCategory: ReceiptCategory?,
    val durationDays: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false
)
