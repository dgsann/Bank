package com.example.bank.data

import android.content.Context
import com.example.bank.model.BudgetSettings
import com.example.bank.model.Receipt
import com.example.bank.model.ReceiptCategory
import com.example.bank.model.ReceiptSource
import org.json.JSONArray
import org.json.JSONObject

class AppStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences("finance_companion_prefs", Context.MODE_PRIVATE)

    fun loadReceipts(): List<Receipt> {
        val raw = prefs.getString("receipts", null) ?: return emptyList()
        val arr = JSONArray(raw)
        val result = ArrayList<Receipt>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            result.add(
                Receipt(
                    id = o.getLong("id"),
                    dateMillis = o.getLong("dateMillis"),
                    store = if (o.isNull("store")) null else o.getString("store"),
                    category = ReceiptCategory.valueOf(o.getString("category")),
                    amount = o.getDouble("amount"),
                    source = ReceiptSource.valueOf(o.optString("source", "MANUAL"))
                )
            )
        }
        return result
    }

    fun saveReceipts(receipts: List<Receipt>) {
        val arr = JSONArray()
        receipts.forEach { r ->
            val o = JSONObject()
            o.put("id", r.id)
            o.put("dateMillis", r.dateMillis)
            o.put("store", r.store ?: JSONObject.NULL)
            o.put("category", r.category.name)
            o.put("amount", r.amount)
            o.put("source", r.source.name)
            arr.put(o)
        }
        prefs.edit().putString("receipts", arr.toString()).apply()
    }

    fun loadBudget(): BudgetSettings = BudgetSettings(
        monthlyIncome = java.lang.Double.longBitsToDouble(prefs.getLong("monthlyIncome", 0L)),
        savingsGoal = java.lang.Double.longBitsToDouble(prefs.getLong("savingsGoal", 0L))
    )

    fun saveBudget(budget: BudgetSettings) {
        prefs.edit()
            .putLong("monthlyIncome", java.lang.Double.doubleToRawLongBits(budget.monthlyIncome))
            .putLong("savingsGoal", java.lang.Double.doubleToRawLongBits(budget.savingsGoal))
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
