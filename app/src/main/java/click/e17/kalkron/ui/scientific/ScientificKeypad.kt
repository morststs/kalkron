package click.e17.kalkron.ui.scientific

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
import click.e17.kalkron.domain.scientific.MathFunction
import click.e17.kalkron.domain.scientific.SciKey
import click.e17.kalkron.domain.scientific.ScientificAction
import click.e17.kalkron.domain.scientific.ScientificEngine
import click.e17.kalkron.domain.scientific.Symbol
import click.e17.kalkron.domain.scientific.Token
import click.e17.kalkron.ui.calculator.components.CalculatorButton
import click.e17.kalkron.ui.calculator.components.CalculatorButtonStyle
import click.e17.kalkron.ui.components.hairlineBorder
import click.e17.kalkron.ui.theme.CyanDeep
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.KeycapBase
import click.e17.kalkron.ui.theme.OnSurfaceMuted

/** キー 1 つ分の定義 */
private data class SciKeySpec(
    val label: String,
    val action: ScientificAction,
    val style: CalculatorButtonStyle,
    val icon: ImageVector? = null,
)

/**
 * 2nd / HYP の状態に応じたキーの表示名。
 * 実際に入るトークンを ScientificEngine.resolve で求め、それを表示名に直す。
 */
fun keyLabel(key: SciKey, second: Boolean, hyperbolic: Boolean): String =
    when (val token = ScientificEngine.resolve(key, second, hyperbolic)) {
        is Token.Fn -> when (token.function) {
            MathFunction.SQRT -> "√x"
            MathFunction.EXP -> "eˣ"
            else -> token.function.label
        }
        is Token.Sym -> if (token.symbol == Symbol.POWER) "xʸ" else "x²"
        is Token.Num -> token.digits
    }

/**
 * 関数電卓のキーパッド。1段目は背の低いチップ列、その下に 5 列 × 6 行のキー。
 */
@Composable
fun ScientificKeypad(
    uiState: ScientificUiState,
    onAction: (ScientificAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val second = uiState.secondActive
    val hyperbolic = uiState.hyperbolicActive

    fun fn(key: SciKey) = SciKeySpec(keyLabel(key, second, hyperbolic), ScientificAction.Key(key), CalculatorButtonStyle.Function)
    fun digit(n: Int) = SciKeySpec(n.toString(), ScientificAction.Digit(n), CalculatorButtonStyle.Number)
    fun sym(label: String, symbol: Symbol) = SciKeySpec(label, ScientificAction.Insert(symbol), CalculatorButtonStyle.Function)
    fun op(label: String, symbol: Symbol) = SciKeySpec(label, ScientificAction.Insert(symbol), CalculatorButtonStyle.Operator)

    val rows = listOf(
        listOf(
            SciKeySpec("d/dx", ScientificAction.InsertFunction(MathFunction.DERIVATIVE), CalculatorButtonStyle.Function),
            SciKeySpec("∫dx", ScientificAction.InsertFunction(MathFunction.INTEGRAL), CalculatorButtonStyle.Function),
            fn(SciKey.X_SQUARED),
            fn(SciKey.SQRT),
            SciKeySpec("AC", ScientificAction.Clear, CalculatorButtonStyle.Function),
        ),
        listOf(
            fn(SciKey.SIN), fn(SciKey.COS), fn(SciKey.TAN), fn(SciKey.LN),
            SciKeySpec("DEL", ScientificAction.Delete, CalculatorButtonStyle.Function, Icons.AutoMirrored.Filled.Backspace),
        ),
        listOf(sym("π", Symbol.PI), digit(7), digit(8), digit(9), op("÷", Symbol.DIVIDE)),
        listOf(sym("e", Symbol.E), digit(4), digit(5), digit(6), op("×", Symbol.TIMES)),
        listOf(sym("(", Symbol.LEFT_PAREN), digit(1), digit(2), digit(3), op("−", Symbol.MINUS)),
        listOf(
            sym(")", Symbol.RIGHT_PAREN), digit(0),
            SciKeySpec(".", ScientificAction.Decimal, CalculatorButtonStyle.Number),
            op("+", Symbol.PLUS),
            SciKeySpec("=", ScientificAction.Equals, CalculatorButtonStyle.Accent),
        ),
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // 1段目: 切り替え・変数・メモリのチップ列（ほかの行より低い）
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.6f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val chip = Modifier.weight(1f).fillMaxHeight()
            SciChip("2nd", active = second, onClick = { onAction(ScientificAction.ToggleSecond) }, modifier = chip)
            SciChip("HYP", active = hyperbolic, onClick = { onAction(ScientificAction.ToggleHyperbolic) }, modifier = chip)
            SciChip("x", onClick = { onAction(ScientificAction.Insert(Symbol.X)) }, modifier = chip)
            SciChip(",", onClick = { onAction(ScientificAction.Insert(Symbol.COMMA)) }, modifier = chip)
            SciChip(if (second) "RCL" else "STO", onClick = { onAction(ScientificAction.Store) }, modifier = chip)
            SciChip("ANS", onClick = { onAction(ScientificAction.Insert(Symbol.ANS)) }, modifier = chip)
        }

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { key ->
                    CalculatorButton(
                        label = key.label,
                        onClick = { onAction(key.action) },
                        style = key.style,
                        icon = key.icon,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

/**
 * 1段目の小さなキー。2nd / HYP が ON のときはシアンで塗って状態を示す。
 */
@Composable
private fun SciChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (active) ElectricCyan else KeycapBase)
            .then(if (active) Modifier else Modifier.hairlineBorder(shape))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) CyanDeep else OnSurfaceMuted,
        )
    }
}
