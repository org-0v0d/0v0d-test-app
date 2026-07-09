package com.org.b0v0d_test_app

import com.org.b0v0d_test_app.data.BillingCycle
import com.org.b0v0d_test_app.data.Subscription
import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionTest {

    @Test
    fun `月額プランの年間換算は12倍`() {
        val netflix = Subscription(name = "Netflix", price = 1590, cycle = BillingCycle.MONTHLY)
        assertEquals(1590.0, netflix.monthlyEquivalent, 0.0)
        assertEquals(19080.0, netflix.yearlyEquivalent, 0.0)
    }

    @Test
    fun `年額プランの月額換算は12分の1`() {
        val prime = Subscription(name = "Amazonプライム", price = 5900, cycle = BillingCycle.YEARLY)
        assertEquals(5900.0, prime.yearlyEquivalent, 0.0)
        assertEquals(5900.0 / 12.0, prime.monthlyEquivalent, 0.0001)
    }

    @Test
    fun `equivalentForは表示サイクルに応じた換算を返す`() {
        val youtube = Subscription(name = "YouTube Premium", price = 1280, cycle = BillingCycle.MONTHLY)
        assertEquals(1280.0, youtube.equivalentFor(BillingCycle.MONTHLY), 0.0)
        assertEquals(15360.0, youtube.equivalentFor(BillingCycle.YEARLY), 0.0)
    }

    @Test
    fun `月額と年額が混在しても合計を正しく換算できる`() {
        val subscriptions = listOf(
            Subscription(name = "Netflix", price = 1590, cycle = BillingCycle.MONTHLY),
            Subscription(name = "Amazonプライム", price = 5900, cycle = BillingCycle.YEARLY),
        )
        val monthlyTotal = subscriptions.sumOf { it.monthlyEquivalent }
        val yearlyTotal = subscriptions.sumOf { it.yearlyEquivalent }
        assertEquals(1590.0 + 5900.0 / 12.0, monthlyTotal, 0.0001)
        assertEquals(19080.0 + 5900.0, yearlyTotal, 0.0)
    }
}
