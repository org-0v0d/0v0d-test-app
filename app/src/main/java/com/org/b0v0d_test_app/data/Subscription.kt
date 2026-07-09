package com.org.b0v0d_test_app.data

import java.util.UUID

enum class BillingCycle(val label: String) {
    MONTHLY("月額"),
    YEARLY("年額"),
}

enum class Category(val label: String) {
    VIDEO("動画"),
    MUSIC("音楽"),
    GAME("ゲーム"),
    CLOUD("クラウド"),
    AI("AI"),
    OTHER("その他"),
}

data class Subscription(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Long,
    val cycle: BillingCycle,
    val category: Category = Category.OTHER,
) {
    val monthlyEquivalent: Double
        get() = when (cycle) {
            BillingCycle.MONTHLY -> price.toDouble()
            BillingCycle.YEARLY -> price / 12.0
        }

    val yearlyEquivalent: Double
        get() = when (cycle) {
            BillingCycle.MONTHLY -> price * 12.0
            BillingCycle.YEARLY -> price.toDouble()
        }

    fun equivalentFor(viewCycle: BillingCycle): Double = when (viewCycle) {
        BillingCycle.MONTHLY -> monthlyEquivalent
        BillingCycle.YEARLY -> yearlyEquivalent
    }
}

data class Preset(
    val name: String,
    val price: Long,
    val cycle: BillingCycle,
    val category: Category,
)

// 価格は2026年時点の日本向け代表プラン(税込)
val servicePresets = listOf(
    Preset("Netflix", 1590, BillingCycle.MONTHLY, Category.VIDEO),
    Preset("YouTube Premium", 1280, BillingCycle.MONTHLY, Category.VIDEO),
    Preset("Amazonプライム", 5900, BillingCycle.YEARLY, Category.VIDEO),
    Preset("Disney+", 990, BillingCycle.MONTHLY, Category.VIDEO),
    Preset("Spotify Premium", 980, BillingCycle.MONTHLY, Category.MUSIC),
    Preset("Apple Music", 1080, BillingCycle.MONTHLY, Category.MUSIC),
    Preset("PlayStation Plus", 6800, BillingCycle.YEARLY, Category.GAME),
    Preset("iCloud+ 200GB", 450, BillingCycle.MONTHLY, Category.CLOUD),
    Preset("ChatGPT Plus", 3000, BillingCycle.MONTHLY, Category.AI),
    Preset("Claude Pro", 3000, BillingCycle.MONTHLY, Category.AI),
    Preset("Google AI Pro", 2900, BillingCycle.MONTHLY, Category.AI),
    Preset("GitHub Copilot Pro", 1500, BillingCycle.MONTHLY, Category.AI),
)
