package click.e17.kalkron.domain.scientific

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 表示用の整形のテスト。
 */
class ScientificFormatTest {

    @Test
    fun `整数と小数`() {
        assertEquals("0", ScientificFormat.number(0.0))
        assertEquals("14", ScientificFormat.number(14.0))
        assertEquals("1234567.891", ScientificFormat.number(1234567.891))
        assertEquals("-2.5", ScientificFormat.number(-2.5))
    }

    @Test
    fun `有効桁数12桁に丸める`() {
        assertEquals("0.3", ScientificFormat.number(0.1 + 0.2))
        assertEquals("0.333333333333", ScientificFormat.number(1.0 / 3))
        assertEquals("0.666666666667", ScientificFormat.number(2.0 / 3))
        assertEquals("123456789012000", ScientificFormat.number(123456789012345.0))
    }

    @Test
    fun `大きい値と小さい値は指数表記`() {
        assertEquals("1E+15", ScientificFormat.number(1e15))
        assertEquals("1.23456789012E+20", ScientificFormat.number(1.2345678901234e20))
        assertEquals("-2.5E-7", ScientificFormat.number(-2.5e-7))
        // 丸めた結果が 1e15 に達したら指数表記にする
        assertEquals("1E+15", ScientificFormat.number(999999999999999.0))
    }

    private fun expr(source: String) = ScientificFormat.expression(Lexer.tokenize(source))

    @Test
    fun `二項演算子の前後に空白を入れる`() {
        assertEquals("2 + 3 × 4", expr("2+3×4"))
        assertEquals("sin(2π) ÷ 2", expr("sin(2π)÷2"))
    }

    @Test
    fun `単項マイナスと累乗には空白を入れない`() {
        assertEquals("−2^2", expr("−2^2"))
        assertEquals("2 × −3", expr("2×−3"))
        assertEquals("sin(−x)", expr("sin(−x)"))
    }

    @Test
    fun `カンマの後に空白を入れる`() {
        assertEquals("∫(x², 0, 1)", expr("∫(x²,0,1)"))
    }

    @Test
    fun `演算子で終わる入力途中の式`() {
        assertEquals("2 +", expr("2+"))
        assertEquals("", ScientificFormat.expression(emptyList()))
    }
}
