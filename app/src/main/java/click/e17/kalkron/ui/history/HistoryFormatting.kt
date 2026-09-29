package click.e17.kalkron.ui.history

import click.e17.kalkron.domain.CalculationRecord
import click.e17.kalkron.domain.CalculatorMode
import click.e17.kalkron.ui.format.groupDigits
import click.e17.kalkron.ui.format.groupExpression

/**
 * 履歴カードに出す式。
 * プログラマーの記録は保存時に整形済みで、BIN の数字に区切りを入れると意味が変わるので、そのまま出す。
 */
fun CalculationRecord.displayExpression(): String = when (mode) {
    CalculatorMode.PROGRAMMER -> expression
    CalculatorMode.STANDARD, CalculatorMode.SCIENTIFIC -> groupExpression(expression)
}

/** 履歴カードに出す結果。考え方は displayExpression と同じ */
fun CalculationRecord.displayResult(): String = when (mode) {
    CalculatorMode.PROGRAMMER -> result
    CalculatorMode.STANDARD, CalculatorMode.SCIENTIFIC -> groupDigits(result)
}
