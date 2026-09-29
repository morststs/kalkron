package click.e17.kalkron.ui.programmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import click.e17.kalkron.domain.programmer.ProgSymbol
import click.e17.kalkron.domain.programmer.ProgrammerAction
import click.e17.kalkron.ui.calculator.components.CalculatorButton
import click.e17.kalkron.ui.calculator.components.CalculatorButtonStyle
import click.e17.kalkron.ui.components.hairlineBorder
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.KeycapBase

/** キー 1 つ分の定義 */
private data class ProgKeySpec(
    val label: String,
    val action: ProgrammerAction,
    val style: CalculatorButtonStyle,
    val span: Float = 1f,
    val enabled: Boolean = true,
    val icon: ImageVector? = null,
)

/**
 * プログラマーモードのキーパッド。1〜2段目はビット演算のチップ列、その下に 5 列 × 6 行のキー。
 * 今の基数で使えない数字キーは暗くして押せなくする。
 */
@Composable
fun ProgrammerKeypad(
    uiState: ProgrammerUiState,
    onAction: (ProgrammerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun digit(value: Int) = ProgKeySpec(
        label = value.toString(16).uppercase(),
        action = ProgrammerAction.Digit(value),
        style = CalculatorButtonStyle.Number,
        enabled = value in uiState.enabledDigits,
    )
    fun op(label: String, symbol: ProgSymbol) = ProgKeySpec(label, ProgrammerAction.Insert(symbol), CalculatorButtonStyle.Operator)
    fun fn(label: String, action: ProgrammerAction) = ProgKeySpec(label, action, CalculatorButtonStyle.Function)

    val chipRows = listOf(
        listOf(ProgSymbol.AND, ProgSymbol.OR, ProgSymbol.XOR, ProgSymbol.NOT, ProgSymbol.NAND),
        listOf(ProgSymbol.NOR, ProgSymbol.ROL, ProgSymbol.ROR, ProgSymbol.SHL, ProgSymbol.SHR),
    )
    val rows = listOf(
        listOf(digit(10), digit(11), digit(12), fn("AC", ProgrammerAction.Clear),
            ProgKeySpec("DEL", ProgrammerAction.Delete, CalculatorButtonStyle.Function, icon = Icons.AutoMirrored.Filled.Backspace)),
        listOf(digit(13), digit(14), digit(15),
            fn("(", ProgrammerAction.Insert(ProgSymbol.LEFT_PAREN)), fn(")", ProgrammerAction.Insert(ProgSymbol.RIGHT_PAREN))),
        listOf(digit(7), digit(8), digit(9), op("÷", ProgSymbol.DIVIDE), op("MOD", ProgSymbol.MOD)),
        listOf(digit(4), digit(5), digit(6), op("×", ProgSymbol.TIMES), fn("2's", ProgrammerAction.Complement)),
        listOf(digit(1), digit(2), digit(3), op("−", ProgSymbol.MINUS), fn("ANS", ProgrammerAction.Insert(ProgSymbol.ANS))),
        listOf(digit(0).copy(span = 2f), op("+", ProgSymbol.PLUS),
            ProgKeySpec("=", ProgrammerAction.Equals, CalculatorButtonStyle.Accent, span = 2f)),
    )

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chipRows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().weight(0.6f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { symbol ->
                    OperatorChip(
                        label = symbol.text,
                        onClick = { onAction(ProgrammerAction.Insert(symbol)) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    CalculatorButton(
                        label = key.label,
                        onClick = { onAction(key.action) },
                        style = key.style,
                        icon = key.icon,
                        enabled = key.enabled,
                        modifier = Modifier.weight(key.span).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

/** ビット演算の小さなキー。文字はシアン */
@Composable
private fun OperatorChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .clip(shape)
            .background(KeycapBase)
            .hairlineBorder(shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = ElectricCyan)
    }
}
