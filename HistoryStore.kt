package com.example.poolcalculator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Estimate(
    val id: Long,
    val name: String,
    val dateMillis: Long,
    val unitLabel: String,
    val length: Double,
    val width: Double,
    val shallow: Double,
    val deep: Double,
    val totalAreaFt2: Double,
    val volumeLitres: Double,
    val filterLph: Double,
    val poolCost: Double,
    val gst: Double,
    val total: Double
)

object HistoryStore {
    private const val PREF = "pool_history"
    private const val KEY = "items"

    fun load(ctx: Context): List<Estimate> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Estimate(
                    o.getLong("id"), o.getString("name"), o.getLong("date"),
                    o.getString("unit"), o.getDouble("l"), o.getDouble("w"),
                    o.getDouble("s"), o.getDouble("d"), o.getDouble("area"),
                    o.getDouble("vol"), o.getDouble("filter"),
                    o.getDouble("cost"), o.getDouble("gst"), o.getDouble("total")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun save(ctx: Context, list: List<Estimate>) {
        val arr = JSONArray()
        list.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id); put("name", e.name); put("date", e.dateMillis)
                put("unit", e.unitLabel); put("l", e.length); put("w", e.width)
                put("s", e.shallow); put("d", e.deep); put("area", e.totalAreaFt2)
                put("vol", e.volumeLitres); put("filter", e.filterLph)
                put("cost", e.poolCost); put("gst", e.gst); put("total", e.total)
            })
        }
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    fun add(ctx: Context, e: Estimate) = save(ctx, listOf(e) + load(ctx))
    fun delete(ctx: Context, id: Long) = save(ctx, load(ctx).filter { it.id != id })
    fun clear(ctx: Context) = save(ctx, emptyList())
}
