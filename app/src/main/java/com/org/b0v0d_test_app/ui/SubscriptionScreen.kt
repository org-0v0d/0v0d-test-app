package com.org.b0v0d_test_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.org.b0v0d_test_app.data.BillingCycle
import com.org.b0v0d_test_app.data.Category
import com.org.b0v0d_test_app.data.Subscription
import com.org.b0v0d_test_app.data.SubscriptionRepository
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

fun formatYen(amount: Double): String =
    "¥" + NumberFormat.getNumberInstance(Locale.JAPAN).format(amount.roundToLong())

/** カテゴリのアバター色(container / onContainer)。カテゴリごとに固定割り当て。 */
private fun Category.avatarColors(): Pair<Color, Color> = when (this) {
    Category.VIDEO -> Color(0xFFFFDAD6) to Color(0xFF410002)
    Category.MUSIC -> Color(0xFFD3E4FF) to Color(0xFF001C38)
    Category.GAME -> Color(0xFFD8E7CB) to Color(0xFF121F0E)
    Category.CLOUD -> Color(0xFFEADDFF) to Color(0xFF21005D)
    Category.AI -> Color(0xFFB8EAE0) to Color(0xFF00201A)
    Category.OTHER -> Color(0xFFFFDF9E) to Color(0xFF261A00)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionApp() {
    val context = LocalContext.current
    val repository = remember { SubscriptionRepository(context) }
    var subscriptions by remember { mutableStateOf(repository.load()) }
    var viewCycle by rememberSaveable { mutableStateOf(BillingCycle.MONTHLY) }
    var editing by remember { mutableStateOf<Subscription?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    fun update(newList: List<Subscription>) {
        subscriptions = newList
        repository.save(newList)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { CenterAlignedTopAppBar(title = { Text("サブスク管理") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Text(
                    text = "＋",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { contentDescription = "サブスクを追加" },
                )
            }
        },
    ) { innerPadding ->
        val sorted = subscriptions.sortedByDescending { it.monthlyEquivalent }
        val totalForView = subscriptions.sumOf { it.equivalentFor(viewCycle) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SummaryCard(
                    subscriptions = subscriptions,
                    viewCycle = viewCycle,
                    onViewCycleChange = { viewCycle = it },
                )
            }

            if (subscriptions.isEmpty()) {
                item {
                    Text(
                        text = "まだ登録がありません。\n右下の＋ボタンからサブスクを追加しましょう。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                    )
                }
            } else {
                items(sorted, key = { it.id }) { subscription ->
                    SubscriptionRow(
                        subscription = subscription,
                        viewCycle = viewCycle,
                        shareOfTotal = if (totalForView > 0) {
                            (subscription.equivalentFor(viewCycle) / totalForView).toFloat()
                        } else 0f,
                        onClick = {
                            editing = subscription
                            showDialog = true
                        },
                    )
                }
            }
        }
    }

    if (showDialog) {
        val target = editing
        EditSubscriptionDialog(
            original = target,
            onSave = { saved ->
                update(
                    if (target == null) subscriptions + saved
                    else subscriptions.map { if (it.id == saved.id) saved else it }
                )
                showDialog = false
            },
            onDelete = if (target == null) null else {
                {
                    update(subscriptions.filterNot { it.id == target.id })
                    showDialog = false
                }
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun SummaryCard(
    subscriptions: List<Subscription>,
    viewCycle: BillingCycle,
    onViewCycleChange: (BillingCycle) -> Unit,
) {
    val monthlyTotal = subscriptions.sumOf { it.monthlyEquivalent }
    val yearlyTotal = subscriptions.sumOf { it.yearlyEquivalent }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SingleChoiceSegmentedButtonRow {
                BillingCycle.entries.forEachIndexed { index, entry ->
                    SegmentedButton(
                        selected = viewCycle == entry,
                        onClick = { onViewCycleChange(entry) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = BillingCycle.entries.size,
                        ),
                    ) { Text("${entry.label}換算") }
                }
            }

            Text(
                text = formatYen(if (viewCycle == BillingCycle.MONTHLY) monthlyTotal else yearlyTotal),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = when (viewCycle) {
                    BillingCycle.MONTHLY -> "年間では ${formatYen(yearlyTotal)}"
                    BillingCycle.YEARLY -> "月あたり ${formatYen(monthlyTotal)}"
                } + " ・ ${subscriptions.size}件",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SubscriptionRow(
    subscription: Subscription,
    viewCycle: BillingCycle,
    shareOfTotal: Float,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val (container, onContainer) = subscription.category.avatarColors()
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(container),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = subscription.name.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    color = onContainer,
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${subscription.cycle.label}プラン" +
                        " ${formatYen(subscription.price.toDouble())} ・ ${subscription.category.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                // 合計に占める割合バー
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(shareOfTotal.coerceIn(0f, 1f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatYen(subscription.equivalentFor(viewCycle)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (viewCycle == BillingCycle.MONTHLY) "/月" else "/年",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
