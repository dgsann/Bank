package com.example.bank.model

enum class ReceiptCategory(val displayName: String, val emoji: String) {
    PRODUCTS("Продукты", "🛒"),
    CAFE("Кафе и рестораны", "☕"),
    TRANSPORT("Транспорт", "🚇"),
    HEALTH("Здоровье и спорт", "💪"),
    EDUCATION("Образование", "📚"),
    ENTERTAINMENT("Развлечения", "🎬"),
    SHOPPING("Покупки", "🛍️"),
    HOUSING("ЖКХ и дом", "🏠"),
    OTHER("Прочее", "•")
}
