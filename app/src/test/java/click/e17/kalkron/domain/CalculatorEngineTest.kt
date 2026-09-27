package click.e17.kalkron.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 電卓ロジックのユニットテスト。
 *
 * CalculatorEngine は Android に依存しないので、端末やエミュレータなしで
 * JVM 上で高速に実行できる（Android Studio で ▶ を押すか ./gradlew test）。
 */
class CalculatorEngineTest {

    /** 一連のキー操作をまとめて適用するヘルパー */
    private fun press(vararg actions: CalculatorAction): EngineResult {
        var result = EngineResult(CalculatorState())
        actions.forEach { action ->
            result = CalculatorEngine.reduce(result.state, action)
        }
        return result
    }

    private fun digits(value: String): Array<CalculatorAction> =
        value.map { char ->
            if (char == '.') CalculatorAction.Decimal
            else CalculatorAction.Digit(char.digitToInt())
        }.toTypedArray()

    // ------------------------------------------------------------------
    // 数値の入力
    // ------------------------------------------------------------------

    @Test
    fun `数字キーを続けて押すと連結される`() {
        val result = press(*digits("123"))
        assertEquals("123", result.state.input)
    }

    @Test
    fun `先頭の0は置き換えられる`() {
        val result = press(CalculatorAction.Digit(0), CalculatorAction.Digit(5))
        assertEquals("5", result.state.input)
    }

    @Test
    fun `小数点は1つまでしか入力できない`() {
        val result = press(*digits("1.5"), CalculatorAction.Decimal, CalculatorAction.Digit(2))
        assertEquals("1.52", result.state.input)
    }

    @Test
    fun `結果表示中に小数点を押すと0から入力し直しになる`() {
        val result = press(
            *digits("5"),
            CalculatorAction.Operate(Operator.ADD),
            *digits("5"),
            CalculatorAction.Equals,
            CalculatorAction.Decimal,
            CalculatorAction.Digit(5),
        )
        assertEquals("0.5", result.state.input)
    }

    @Test
    fun `入力できる桁数には上限がある`() {
        val result = press(*digits("1234567890123456"))
        assertEquals(CalculatorEngine.MAX_INPUT_DIGITS, result.state.input.count { it.isDigit() })
        assertEquals("123456789012", result.state.input)
    }

    // ------------------------------------------------------------------
    // 四則演算
    // ------------------------------------------------------------------

    @Test
    fun `足し算ができる`() {
        val result = press(
            *digits("12"),
            CalculatorAction.Operate(Operator.ADD),
            *digits("3"),
            CalculatorAction.Equals,
        )
        assertEquals("15", result.state.input)
    }

    @Test
    fun `引き算ができる`() {
        val result = press(
            *digits("10"),
            CalculatorAction.Operate(Operator.SUBTRACT),
            *digits("25"),
            CalculatorAction.Equals,
        )
        assertEquals("-15", result.state.input)
    }

    @Test
    fun `掛け算ができる`() {
        val result = press(
            *digits("12"),
            CalculatorAction.Operate(Operator.MULTIPLY),
            *digits("12"),
            CalculatorAction.Equals,
        )
        assertEquals("144", result.state.input)
    }

    @Test
    fun `割り算ができる`() {
        val result = press(
            *digits("10"),
            CalculatorAction.Operate(Operator.DIVIDE),
            *digits("4"),
            CalculatorAction.Equals,
        )
        assertEquals("2.5", result.state.input)
    }

    @Test
    fun `割り切れない割り算は有効桁数で丸められる`() {
        val result = press(
            *digits("10"),
            CalculatorAction.Operate(Operator.DIVIDE),
            *digits("3"),
            CalculatorAction.Equals,
        )
        assertEquals("3.33333333333", result.state.input)
    }

    @Test
    fun `小数の計算で誤差が出ない`() {
        // Double で 0.1 + 0.2 を計算すると 0.30000000000000004 になる。
        // BigDecimal を使っているのでこのテストが通る。
        val result = press(
            *digits("0.1"),
            CalculatorAction.Operate(Operator.ADD),
            *digits("0.2"),
            CalculatorAction.Equals,
        )
        assertEquals("0.3", result.state.input)
    }

    @Test
    fun `演算子を続けて押すと途中結果が確定する`() {
        val result = press(
            *digits("2"),
            CalculatorAction.Operate(Operator.ADD),
            *digits("3"),
            CalculatorAction.Operate(Operator.ADD),
        )
        assertEquals("5", result.state.input)
        assertEquals(Operator.ADD, result.state.pendingOperator)
    }

    @Test
    fun `連続して計算できる`() {
        val result = press(
            *digits("2"),
            CalculatorAction.Operate(Operator.ADD),
            *digits("3"),
            CalculatorAction.Operate(Operator.MULTIPLY),
            *digits("4"),
            CalculatorAction.Equals,
        )
        // 左から順に計算されるので (2 + 3) * 4 = 20
        assertEquals("20", result.state.input)
    }

    @Test
    fun `演算子を押し直すと後から押した方が使われる`() {
        val result = press(
            *digits("8"),
            CalculatorAction.Operate(Operator.ADD),
            CalculatorAction.Operate(Operator.MULTIPLY),
            *digits("2"),
            CalculatorAction.Equals,
        )
        assertEquals("16", result.state.input)
    }

    @Test
    fun `途中式が表示される`() {
        val result = press(*digits("12"), CalculatorAction.Operate(Operator.ADD))
        assertEquals("12 +", result.state.expression)
    }

    // ------------------------------------------------------------------
    // 履歴に残る計算
    // ------------------------------------------------------------------

    @Test
    fun `イコールを押すと完了した計算が返る`() {
        val result = press(
            *digits("12"),
            CalculatorAction.Operate(Operator.ADD),
            *digits("3"),
            CalculatorAction.Equals,
        )
        assertNotNull(result.completed)
        assertEquals("12 + 3", result.completed?.expression)
        assertEquals("15", result.completed?.result)
    }

    @Test
    fun `演算子を押していないイコールでは何も完了しない`() {
        val result = press(*digits("12"), CalculatorAction.Equals)
        assertNull(result.completed)
        assertEquals("12", result.state.input)
    }

    // ------------------------------------------------------------------
    // エラー処理
    // ------------------------------------------------------------------

    @Test
    fun `0で割るとエラーになる`() {
        val result = press(
            *digits("5"),
            CalculatorAction.Operate(Operator.DIVIDE),
            *digits("0"),
            CalculatorAction.Equals,
        )
        assertTrue(result.state.isError)
        assertEquals(CalculatorEngine.ERROR_TEXT, result.state.input)
        assertNull(result.completed)
    }

    @Test
    fun `エラー後に数字を押すと入力を再開できる`() {
        val afterError = press(
            *digits("5"),
            CalculatorAction.Operate(Operator.DIVIDE),
            *digits("0"),
            CalculatorAction.Equals,
        )
        val result = CalculatorEngine.reduce(afterError.state, CalculatorAction.Digit(7))
        assertEquals("7", result.state.input)
        assertTrue(!result.state.isError)
    }

    // ------------------------------------------------------------------
    // 補助キー
    // ------------------------------------------------------------------

    @Test
    fun `符号を反転できる`() {
        val result = press(*digits("42"), CalculatorAction.ToggleSign)
        assertEquals("-42", result.state.input)

        val toggledBack = CalculatorEngine.reduce(result.state, CalculatorAction.ToggleSign)
        assertEquals("42", toggledBack.state.input)
    }

    @Test
    fun `パーセントは100分の1にする`() {
        val result = press(*digits("50"), CalculatorAction.Percent)
        assertEquals("0.5", result.state.input)
    }

    @Test
    fun `DELで1文字ずつ削除できる`() {
        val result = press(*digits("123"), CalculatorAction.Delete)
        assertEquals("12", result.state.input)
    }

    @Test
    fun `DELで全部消すと0に戻る`() {
        val result = press(
            *digits("7"),
            CalculatorAction.Delete,
        )
        assertEquals("0", result.state.input)
    }

    @Test
    fun `ACで初期状態に戻る`() {
        val result = press(
            *digits("12"),
            CalculatorAction.Operate(Operator.ADD),
            *digits("3"),
            CalculatorAction.Clear,
        )
        assertEquals(CalculatorState(), result.state)
    }
}
