package click.e17.kalkron.ui.history

import click.e17.kalkron.domain.CalculationRecord
import click.e17.kalkron.domain.CalculatorMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 履歴カードに出す文字列のテスト。
 * 標準・関数電卓は3桁区切りを入れ、プログラマーは入れない。
 */
class HistoryFormattingTest {

    private fun record(expression: String, result: String, mode: CalculatorMode) =
        CalculationRecord(expression = expression, result = result, createdAt = 0L, mode = mode)

    @Test
    fun `標準と関数電卓は3桁区切りを入れる`() {
        val standard = record("1024 + 48.5", "1072.5", CalculatorMode.STANDARD)
        assertEquals("1,024 + 48.5", standard.displayExpression())
        assertEquals("1,072.5", standard.displayResult())

        val scientific = record("2000 × 3", "6000", CalculatorMode.SCIENTIFIC)
        assertEquals("2,000 × 3", scientific.displayExpression())
        assertEquals("6,000", scientific.displayResult())
    }

    @Test
    fun `プログラマーは区切りを入れない`() {
        // BIN の 1010 に区切りを入れると 1,010 になり、意味が変わってしまう
        val programmer = record("1010 AND 11", "10 (BIN)", CalculatorMode.PROGRAMMER)
        assertEquals("1010 AND 11", programmer.displayExpression())
        assertEquals("10 (BIN)", programmer.displayResult())
    }
}
