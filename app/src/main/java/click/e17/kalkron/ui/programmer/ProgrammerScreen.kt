package click.e17.kalkron.ui.programmer

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import click.e17.kalkron.R
import click.e17.kalkron.domain.programmer.ProgrammerAction
import click.e17.kalkron.domain.programmer.Radix
import click.e17.kalkron.domain.programmer.WordSize
import click.e17.kalkron.ui.AppViewModelProvider
import click.e17.kalkron.ui.components.GlassPanel
import click.e17.kalkron.ui.components.ModeHeader
import click.e17.kalkron.ui.scientific.ResultKind
import click.e17.kalkron.ui.theme.CalculatorTheme
import click.e17.kalkron.ui.theme.CyanDeep
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.ErrorRed
import click.e17.kalkron.ui.theme.MetaText
import click.e17.kalkron.ui.theme.OnSurfaceBright
import click.e17.kalkron.ui.theme.OnSurfaceMuted

/**
 * プログラマーモード画面（状態あり）。描画はステートレスな ProgrammerScreenContent に任せる。
 */
@Composable
fun ProgrammerScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgrammerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProgrammerScreenContent(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
fun ProgrammerScreenContent(
    uiState: ProgrammerUiState,
    onAction: (ProgrammerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // 横向き: 左に表示部とグリッド、右にキーパッド
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 横向きは高さが足りないので、左の列はスクロールできるようにする
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    ModeHeader(mode = stringResource(R.string.mode_programmer))
                    Spacer(modifier = Modifier.height(8.dp))
                    WordSizeRow(uiState = uiState, onAction = onAction)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgrammerDisplay(uiState = uiState, onAction = onAction)
                    Spacer(modifier = Modifier.height(8.dp))
                    BitGrid(bits = uiState.bits, onFlip = { onAction(ProgrammerAction.FlipBit(it)) })
                }
                ProgrammerKeypad(uiState = uiState, onAction = onAction, modifier = Modifier.weight(1.1f))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                ModeHeader(mode = stringResource(R.string.mode_programmer))
                Spacer(modifier = Modifier.height(8.dp))
                WordSizeRow(uiState = uiState, onAction = onAction)
                Spacer(modifier = Modifier.height(8.dp))
                // 表示部は4行すべてが見えるよう中身の高さのまま置き、残りの高さをキーパッドに回す
                ProgrammerDisplay(uiState = uiState, onAction = onAction)
                Spacer(modifier = Modifier.height(8.dp))
                BitGrid(bits = uiState.bits, onFlip = { onAction(ProgrammerAction.FlipBit(it)) })
                Spacer(modifier = Modifier.height(8.dp))
                ProgrammerKeypad(uiState = uiState, onAction = onAction, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** 語長のタブと、符号の切り替え */
@Composable
private fun WordSizeRow(uiState: ProgrammerUiState, onAction: (ProgrammerAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WordSize.entries.forEach { size ->
            Tab(
                label = size.bits.toString(),
                selected = size == uiState.wordSize,
                onClick = { onAction(ProgrammerAction.SetWordSize(size)) },
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Tab(
            label = if (uiState.signed) "SIGNED" else "UNSIGNED",
            selected = true,
            onClick = { onAction(ProgrammerAction.ToggleSigned) },
        )
    }
}

/**
 * 表示部。上段に式、中段に結果、下段に HEX / DEC / OCT / BIN の4行。
 * 4行はいま編集している数を表示し、タップすると入力の基数が切り替わる。
 */
@Composable
private fun ProgrammerDisplay(
    uiState: ProgrammerUiState,
    onAction: (ProgrammerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expressionScroll = rememberScrollState()
    val resultScroll = rememberScrollState()
    LaunchedEffect(uiState.expression) { expressionScroll.scrollTo(expressionScroll.maxValue) }
    LaunchedEffect(uiState.result) { resultScroll.scrollTo(resultScroll.maxValue) }

    GlassPanel(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(expressionScroll), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = uiState.expression,
                    style = MaterialTheme.typography.labelLarge,
                    color = OnSurfaceBright,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            // 結果の行は高さを固定する。確定（大きい字）と先出し（小さい字）で高さが変わると、下の4行が動いてしまうため
            Box(
                modifier = Modifier.fillMaxWidth().height(36.dp).horizontalScroll(resultScroll),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = uiState.result,
                    style = when (uiState.resultKind) {
                        ResultKind.FINAL -> MaterialTheme.typography.headlineMedium
                        ResultKind.PREVIEW -> MaterialTheme.typography.headlineSmall
                        ResultKind.ERROR, ResultKind.EMPTY -> MaterialTheme.typography.bodyLarge
                    },
                    color = when (uiState.resultKind) {
                        ResultKind.FINAL -> ElectricCyan
                        ResultKind.PREVIEW, ResultKind.EMPTY -> MetaText
                        ResultKind.ERROR -> ErrorRed
                    },
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            uiState.radixLines.forEach { line ->
                RadixRow(line = line, onClick = { onAction(ProgrammerAction.SetRadix(line.radix)) })
            }
        }
    }
}

/** 基数1行分。選ばれている基数はシアンで強調し、右端に印を付ける */
@Composable
private fun RadixRow(line: RadixLine, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = line.radix.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (line.selected) ElectricCyan else MetaText,
            modifier = Modifier.width(36.dp),
        )
        // 幅に合わせて行数と文字の大きさを決める（64ビットの BIN は上下2行になる）
        RadixValue(
            radix = line.radix,
            text = line.text,
            style = MaterialTheme.typography.labelMedium,
            color = if (line.selected) OnSurfaceBright else OnSurfaceMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (line.selected) "●" else "○",
            style = MaterialTheme.typography.labelSmall,
            color = if (line.selected) ElectricCyan else MetaText,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** 語長・符号のタブ。選ばれているものをシアンで塗る */
@Composable
private fun Tab(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) ElectricCyan else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) CyanDeep else MetaText,
        )
    }
}

@Preview(name = "縦（下部ナビを除いた高さ）", showBackground = true, backgroundColor = 0xFF050608, widthDp = 390, heightDp = 640)
@Preview(name = "横", showBackground = true, backgroundColor = 0xFF050608, widthDp = 780, heightDp = 300)
@Composable
private fun ProgrammerScreenPreview() {
    CalculatorTheme {
        ProgrammerScreenContent(
            uiState = ProgrammerUiState(
                expression = "7FFF8000 OR A0",
                result = "7FFF80A0",
                resultKind = ResultKind.FINAL,
                radixLines = listOf(
                    RadixLine(Radix.HEX, "7FFF80A0", true),
                    RadixLine(Radix.DEC, "2,147,451,040", false),
                    RadixLine(Radix.OCT, "17777700240", false),
                    RadixLine(Radix.BIN, "111 1111 1111 1111 1000 0000 1010 0000", false),
                ),
                bits = List(32) { index -> (0x7FFF80A0L ushr index) and 1L == 1L },
                radix = Radix.HEX,
                enabledDigits = (0..15).toSet(),
            ),
            onAction = {},
        )
    }
}
