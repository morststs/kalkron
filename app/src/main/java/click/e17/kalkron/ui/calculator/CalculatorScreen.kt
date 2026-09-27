package click.e17.kalkron.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import click.e17.kalkron.R
import click.e17.kalkron.domain.CalculatorAction
import click.e17.kalkron.domain.CalculatorEngine
import click.e17.kalkron.ui.AppViewModelProvider
import click.e17.kalkron.ui.calculator.components.CalculatorDisplay
import click.e17.kalkron.ui.calculator.components.CalculatorKeypad
import click.e17.kalkron.ui.components.ModeHeader
import click.e17.kalkron.ui.components.TelemetryStrip
import click.e17.kalkron.ui.theme.CalculatorTheme

/**
 * 電卓画面（状態あり）。
 *
 * ViewModel から状態を受け取り、実際の描画はステートレスな
 * [CalculatorScreenContent] に委譲する。
 */
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalculatorScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun CalculatorScreenContent(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ステータス帯には実際の状態を出す（飾りの数値は置かない）
    val telemetry = listOf(
        "OP" to (uiState.pendingOperator ?: "--"),
        "DIGITS" to "%02d/%d".format(uiState.digitCount, CalculatorEngine.MAX_INPUT_DIGITS),
        "MODE" to "STANDARD",
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // 横向き: 左に表示部、右にキーパッド
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ModeHeader(mode = stringResource(R.string.mode_standard))
                    Spacer(modifier = Modifier.height(12.dp))
                    CalculatorDisplay(uiState = uiState, compact = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    TelemetryStrip(items = telemetry)
                }
                CalculatorKeypad(
                    onAction = onAction,
                    modifier = Modifier.weight(1.1f),
                )
            }
        } else {
            // 縦向き: ヘッダー → 帯 → 表示部 → キーパッド
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                ModeHeader(mode = stringResource(R.string.mode_standard))
                Spacer(modifier = Modifier.height(10.dp))
                TelemetryStrip(items = telemetry)
                Spacer(modifier = Modifier.height(12.dp))
                CalculatorDisplay(
                    uiState = uiState,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                CalculatorKeypad(
                    onAction = onAction,
                    modifier = Modifier.weight(2.4f),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121316, heightDp = 780)
@Composable
private fun CalculatorScreenPreview() {
    CalculatorTheme {
        CalculatorScreenContent(
            uiState = CalculatorUiState(
                display = "50032.00",
                expression = "1024 +",
                pendingOperator = "+",
                digitCount = 7,
            ),
            onAction = {},
        )
    }
}
