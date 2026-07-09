package com.org.b0v0d_test_app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** SharedPreferencesにJSONで永続化するシンプルなリポジトリ */
class SubscriptionRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<Subscription> {
        val raw = prefs.getString(KEY_SUBSCRIPTIONS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i -> array.getJSONObject(i).toSubscription() }
        }.getOrDefault(emptyList())
    }

    fun save(subscriptions: List<Subscription>) {
        val array = JSONArray()
        subscriptions.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_SUBSCRIPTIONS, array.toString()).apply()
    }

    private fun Subscription.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("price", price)
        put("cycle", cycle.name)
        put("category", category.name)
    }

    private fun JSONObject.toSubscription(): Subscription = Subscription(
        id = getString("id"),
        name = getString("name"),
        price = getLong("price"),
        cycle = BillingCycle.valueOf(getString("cycle")),
        category = runCatching { Category.valueOf(getString("category")) }
            .getOrDefault(Category.OTHER),
    )

    companion object {
        private const val PREFS_NAME = "subscriptions"
        private const val KEY_SUBSCRIPTIONS = "subscriptions_json"
    }
}
