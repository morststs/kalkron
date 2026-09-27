package click.e17.kalkron.ui.calculator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import click.e17.kalkron.domain.CalculatorAction
import click.e17.kalkron.domain.Operator

/**
 * キー 1 つ分の定義。
 *
 * @param span 横方向に何列分を占めるか（0 は 2 列、= は 4 列使う）
 */
private data class KeySpec(
    val label: String,
    val action: CalculatorAction,
    val style: CalculatorButtonStyle,
    val span: Float = 1f,
)

/**
 * キー配置。Stitch の Standard Precision Calculator と同じ
 * 「6 行 + `=` が最下段いっぱい」のリズムに合わせている。
 */
private val KEYPAD: List<List<KeySpec>> = listOf(
    listOf(
        KeySpec("AC", CalculatorAction.Clear, CalculatorButtonStyle.Function),
        KeySpec("+/-", CalculatorAction.ToggleSign, CalculatorButtonStyle.Number),
        KeySpec("%", CalculatorAction.Percent, CalculatorButtonStyle.Number),
        KeySpec("DEL", CalculatorAction.Delete, CalculatorButtonStyle.Function),
    ),
    listOf(
        KeySpec("7", CalculatorAction.Digit(7), CalculatorButtonStyle.Number),
        KeySpec("8", CalculatorAction.Digit(8), CalculatorButtonStyle.Number),
        KeySpec("9", CalculatorAction.Digit(9), CalculatorButtonStyle.Number),
        KeySpec("÷", CalculatorAction.Operate(Operator.DIVIDE), CalculatorButtonStyle.Operator),
    ),
    listOf(
        KeySpec("4", CalculatorAction.Digit(4), CalculatorButtonStyle.Number),
        KeySpec("5", CalculatorAction.Digit(5), CalculatorButtonStyle.Number),
        KeySpec("6", CalculatorAction.Digit(6), CalculatorButtonStyle.Number),
        KeySpec("×", CalculatorAction.Operate(Operator.MULTIPLY), CalculatorButtonStyle.Operator),
    ),
    listOf(
        KeySpec("1", CalculatorAction.Digit(1), CalculatorButtonStyle.Number),
        KeySpec("2", CalculatorAction.Digit(2), CalculatorButtonStyle.Number),
        KeySpec("3", CalculatorAction.Digit(3), CalculatorButtonStyle.Number),
        KeySpec("−", CalculatorAction.Operate(Operator.SUBTRACT), CalculatorButtonStyle.Operator),
    ),
    listOf(
        KeySpec("0", CalculatorAction.Digit(0), CalculatorButtonStyle.Number, span = 2f),
        KeySpec(".", CalculatorAction.Decimal, CalculatorButtonStyle.Number),
        KeySpec("+", CalculatorAction.Operate(Operator.ADD), CalculatorButtonStyle.Operator),
    ),
    listOf(
        KeySpec("=", CalculatorAction.Equals, CalculatorButtonStyle.Accent, span = 4f),
    ),
)

/**
 * キーパッド全体。
 *
 * 押されたキーに対応する [CalculatorAction] を onAction で親に渡すだけで、
 * この Composable 自身は状態を持たない（state hoisting）。
 */
@Composable
fun CalculatorKeypad(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        // Stitch の指定どおり、キー同士の間隔は詰める（8px）
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KEYPAD.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { key ->
                    CalculatorButton(
                        label = key.label,
                        onClick = { onAction(key.action) },
                        style = key.style,
                        modifier = Modifier
                            .weight(key.span)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}
