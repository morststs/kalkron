package click.e17.kalkron.ui.scientific

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import click.e17.kalkron.R
import click.e17.kalkron.domain.scientific.AngleUnit
import click.e17.kalkron.domain.scientific.ScientificAction
import click.e17.kalkron.ui.AppViewModelProvider
import click.e17.kalkron.ui.components.GlassPanel
import click.e17.kalkron.ui.components.ModeHeader
import click.e17.kalkron.ui.theme.AmberTelemetry
import click.e17.kalkron.ui.theme.CalculatorTheme
import click.e17.kalkron.ui.theme.CyanDeep
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.ErrorRed
import click.e17.kalkron.ui.theme.MetaText
import click.e17.kalkron.ui.theme.OnSurfaceBright

/**
 * 関数電卓画面（状態あり）。描画はステートレスな ScientificScreenContent に任せる。
 */
@Composable
fun ScientificScreen(
    modifier: Modifier = Modifier,
    viewModel: ScientificViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScientificScreenContent(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
fun ScientificScreenContent(
    uiState: ScientificUiState,
    onAction: (ScientificAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // 横向き: 左に表示部、右にキーパッド
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ModeHeader(mode = stringResource(R.string.mode_scientific))
                    Spacer(modifier = Modifier.height(12.dp))
                    ScientificDisplay(uiState = uiState, onAction = onAction, modifier = Modifier.weight(1f))
                }
                ScientificKeypad(uiState = uiState, onAction = onAction, modifier = Modifier.weight(1.2f))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                ModeHeader(mode = stringResource(R.string.mode_scientific))
                Spacer(modifier = Modifier.height(10.dp))
                ScientificDisplay(uiState = uiState, onAction = onAction, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(12.dp))
                ScientificKeypad(uiState = uiState, onAction = onAction, modifier = Modifier.weight(2.6f))
            }
        }
    }
}

/**
 * 表示部。上に角度単位のタブ、中段に式、下段に結果。
 */
@Composable
private fun ScientificDisplay(
    uiState: ScientificUiState,
    onAction: (ScientificAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    // 式が伸びたら右端（最新の入力）が見えるようにする
    LaunchedEffect(uiState.expression) { scrollState.scrollTo(scrollState.maxValue) }
    val resultScrollState = rememberScrollState()
    LaunchedEffect(uiState.result) { resultScrollState.scrollTo(resultScrollState.maxValue) }

    GlassPanel(modifier = modifier.fillMaxWidth()) {
        // 高さを埋めておかないと、下の weight の Spacer が効かず式と結果が上に寄る
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AngleUnit.entries.forEach { unit ->
                        AngleTab(
                            label = unit.label,
                            selected = unit == uiState.angleUnit,
                            onClick = { onAction(ScientificAction.SetAngleUnit(unit)) },
                        )
                    }
                }
                if (uiState.hasMemory) {
                    Text(text = "M", style = MaterialTheme.typography.labelSmall, color = AmberTelemetry)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = uiState.expression,
                    style = MaterialTheme.typography.labelLarge,
                    color = OnSurfaceBright,
                    maxLines = 1,
                    softWrap = false,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 桁の多い結果（例: −1.23456789012E+20）が画面幅を超えても切れないよう横にスクロールさせ、
            // 指数の部分が見えるよう右端を表示する（STD の表示部と同じ方式）
            Box(
                modifier = Modifier.fillMaxWidth().horizontalScroll(resultScrollState),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = uiState.result,
                    style = when (uiState.resultKind) {
                        ResultKind.FINAL -> MaterialTheme.typography.displayMedium
                        ResultKind.PREVIEW -> MaterialTheme.typography.headlineSmall
                        ResultKind.ERROR, ResultKind.EMPTY -> MaterialTheme.typography.bodyLarge
                    },
                    color = when (uiState.resultKind) {
                        ResultKind.FINAL -> ElectricCyan
                        ResultKind.PREVIEW -> MetaText
                        ResultKind.ERROR -> ErrorRed
                        ResultKind.EMPTY -> MetaText
                    },
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

/** 角度単位のタブ。選ばれているものをシアンで塗る */
@Composable
private fun AngleTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) ElectricCyan else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) CyanDeep else MetaText,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, heightDp = 780)
@Composable
private fun ScientificScreenPreview() {
    CalculatorTheme {
        ScientificScreenContent(
            uiState = ScientificUiState(
                expression = "sin(2π × 0.25) + ln(e²)",
                result = "3",
                resultKind = ResultKind.FINAL,
                secondActive = true,
                hasMemory = true,
            ),
            onAction = {},
        )
    }
}
