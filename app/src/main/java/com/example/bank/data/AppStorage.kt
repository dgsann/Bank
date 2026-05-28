package com.example.bank.data

import android.content.Context
import com.example.bank.model.BudgetSettings
import com.example.bank.model.Receipt
import com.example.bank.model.ReceiptItem
import com.example.bank.model.ReceiptCategory
import com.example.bank.model.ReceiptSource
import org.json.JSONArray
import org.json.JSONObject

import com.example.bank.model.Achievement

class AppStorage(context: Context) {
    // ... existing ...

    fun loadAchievements(): List<Achievement> {
        val raw = prefs.getString("achievements", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val result = ArrayList<Achievement>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                result.add(Achievement(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    description = o.getString("description"),
                    targetCategory = if (o.isNull("targetCategory")) null else ReceiptCategory.valueOf(o.getString("targetCategory")),
                    durationDays = o.getInt("durationDays"),
                    createdAt = o.getLong("createdAt"),
                    isCompleted = o.getBoolean("isCompleted"),
                    isFailed = o.getBoolean("isFailed")
                ))
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAchievements(list: List<Achievement>) {
        val arr = JSONArray()
        list.forEach { a ->
            val o = JSONObject()
            o.put("id", a.id)
            o.put("title", a.title)
            o.put("description", a.description)
            o.put("targetCategory", a.targetCategory?.name ?: JSONObject.NULL)
            o.put("durationDays", a.durationDays)
            o.put("createdAt", a.createdAt)
            o.put("isCompleted", a.isCompleted)
            o.put("isFailed", a.isFailed)
            arr.put(o)
        }
        prefs.edit().putString("achievements", arr.toString()).apply()
    }

    private val prefs =
        context.getSharedPreferences("finance_companion_prefs", Context.MODE_PRIVATE)

    fun loadReceipts(): List<Receipt> {
        val raw = prefs.getString("receipts", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val result = ArrayList<Receipt>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                
                val itemsArr = o.optJSONArray("items")
                val items = ArrayList<ReceiptItem>()
                if (itemsArr != null) {
                    for (j in 0 until itemsArr.length()) {
                        val io = itemsArr.getJSONObject(j)
                        items.add(ReceiptItem(
                            name = io.getString("name"),
                            price = io.getDouble("price"),
                            category = ReceiptCategory.valueOf(io.getString("category"))
                        ))
                    }
                }

                result.add(
                    Receipt(
                        id = o.getLong("id"),
                        dateMillis = o.getLong("dateMillis"),
                        store = if (o.isNull("store")) null else o.getString("store"),
                        category = ReceiptCategory.valueOf(o.getString("category")),
                        amount = o.getDouble("amount"),
                        source = ReceiptSource.valueOf(o.optString("source", "MANUAL")),
                        items = items
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
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
            
            val itemsArr = JSONArray()
            r.items.forEach { item ->
                val io = JSONObject()
                io.put("name", item.name)
                io.put("price", item.price)
                io.put("category", item.category.name)
                itemsArr.put(io)
            }
            o.put("items", itemsArr)

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
