package com.org.b0v0d_test_app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.org.b0v0d_test_app.data.BillingCycle
import com.org.b0v0d_test_app.data.Category
import com.org.b0v0d_test_app.data.Subscription
import com.org.b0v0d_test_app.data.servicePresets

/**
 * サブスクの追加・編集ダイアログ。
 * [original] がnullなら新規追加(プリセット選択を表示)、非nullなら編集。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSubscriptionDialog(
    original: Subscription?,
    onSave: (Subscription) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(original?.name ?: "") }
    var priceText by remember { mutableStateOf(original?.price?.toString() ?: "") }
    var cycle by remember { mutableStateOf(original?.cycle ?: BillingCycle.MONTHLY) }
    var category by remember { mutableStateOf(original?.category ?: Category.OTHER) }
    var confirmingDelete by remember { mutableStateOf(false) }

    val price = priceText.toLongOrNull()
    val canSave = name.isNotBlank() && price != null && price > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (original == null) "サブスクを追加" else "サブスクを編集") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (original == null) {
                    Text("プリセットから選択", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        servicePresets.forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    name = preset.name
                                    priceText = preset.price.toString()
                                    cycle = preset.cycle
                                    category = preset.category
                                },
                                label = { Text(preset.name) },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("サービス名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input -> priceText = input.filter(Char::isDigit) },
                    label = { Text("料金(円)") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    BillingCycle.entries.forEachIndexed { index, entry ->
                        SegmentedButton(
                            selected = cycle == entry,
                            onClick = { cycle = entry },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = BillingCycle.entries.size,
                            ),
                        ) { Text(entry.label) }
                    }
                }

                Text("カテゴリ", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { entry ->
                        FilterChip(
                            selected = category == entry,
                            onClick = { category = entry },
                            label = { Text(entry.label) },
                        )
                    }
                }

                if (price != null && price > 0) {
                    val preview = Subscription(name = "", price = price, cycle = cycle)
                    Text(
                        text = "換算: 月あたり ${formatYen(preview.monthlyEquivalent)}" +
                            " ／ 年間 ${formatYen(preview.yearlyEquivalent)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        Subscription(
                            id = original?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            price = price ?: return@TextButton,
                            cycle = cycle,
                            category = category,
                        )
                    )
                },
            ) { Text("保存") }
        },
        dismissButton = {
            if (onDelete != null) {
                TextButton(
                    onClick = { confirmingDelete = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("削除") }
            }
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        },
    )

    if (confirmingDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("削除しますか?") },
            text = { Text("「${original?.name}」を削除します。この操作は取り消せません。") },
            confirmButton = {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("削除") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("キャンセル") }
            },
        )
    }
}
