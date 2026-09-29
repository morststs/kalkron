package click.e17.kalkron.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import click.e17.kalkron.R
import click.e17.kalkron.domain.CalculationRecord
import click.e17.kalkron.domain.CalculatorMode
import click.e17.kalkron.ui.AppViewModelProvider
import click.e17.kalkron.ui.components.GlassPanel
import click.e17.kalkron.ui.components.ModeHeader
import click.e17.kalkron.ui.components.TelemetryStrip
import click.e17.kalkron.ui.theme.AmberTelemetry
import click.e17.kalkron.ui.theme.CalculatorTheme
import click.e17.kalkron.ui.theme.CyanBright
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.Hairline
import click.e17.kalkron.ui.theme.MetaText
import click.e17.kalkron.ui.theme.VioletContainer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 計算履歴の一覧画面（状態あり）。
 */
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreenContent(
        uiState = uiState,
        onClearHistory = viewModel::onClearHistory,
        modifier = modifier,
    )
}

/**
 * Stitch の "History Tape & Matrix Log" を再現した一覧。
 * 1 件ずつをテープログのカードとして積み上げる。
 */
@Composable
fun HistoryScreenContent(
    uiState: HistoryUiState,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ModeHeader(mode = stringResource(R.string.mode_history))
        Spacer(modifier = Modifier.height(10.dp))
        TelemetryStrip(
            items = listOf(
                "TAPE" to if (uiState.isLoading) "LOADING" else "${uiState.records.size} ENTRIES",
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.records.isEmpty() && !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.history_empty_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MetaText,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.history_empty_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MetaText,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = uiState.records, key = { it.id }) { record ->
                        HistoryItem(record = record)
                    }
                }
            }
        }

        if (uiState.records.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onClearHistory,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberTelemetry),
                border = BorderStroke(1.dp, Hairline),
            ) {
                Text(
                    text = stringResource(R.string.clear_history),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

/** 履歴 1 件分のテープログカード */
@Composable
private fun HistoryItem(
    record: CalculationRecord,
    modifier: Modifier = Modifier,
) {
    // モードごとの色。標準はシアン、関数電卓はアンバー
    val modeColor = when (record.mode) {
        CalculatorMode.STANDARD -> ElectricCyan
        CalculatorMode.SCIENTIFIC -> AmberTelemetry
        CalculatorMode.PROGRAMMER -> VioletContainer
    }

    GlassPanel(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // 左端のアクセントバー
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(modeColor),
            )
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${record.mode.name}  #%04d".format(record.id),
                        style = MaterialTheme.typography.labelSmall,
                        color = modeColor,
                    )
                    Text(
                        text = formatTimestamp(record.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MetaText,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = record.displayExpression(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MetaText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "= ${record.displayResult()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CyanBright,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

private val TIMESTAMP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MM/dd HH:mm:ss")

/** エポックミリ秒を端末のタイムゾーンで整形する（minSdk 26 なので java.time が使える） */
private fun formatTimestamp(epochMillis: Long): String =
    TIMESTAMP_FORMATTER.format(
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    )

@Preview(showBackground = true, backgroundColor = 0xFF121316, heightDp = 700)
@Composable
private fun HistoryScreenPreview() {
    CalculatorTheme {
        HistoryScreenContent(
            uiState = HistoryUiState(
                records = listOf(
                    CalculationRecord(id = 42, expression = "1024 + 48.5", result = "1072.5", createdAt = 0L),
                    CalculationRecord(id = 41, expression = "10 ÷ 3", result = "3.33333333333", createdAt = 0L),
                ),
                isLoading = false,
            ),
            onClearHistory = {},
        )
    }
}
