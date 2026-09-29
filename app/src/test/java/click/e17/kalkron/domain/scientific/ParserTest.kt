package click.e17.kalkron.domain.scientific

import click.e17.kalkron.domain.scientific.BinaryOp.ADD
import click.e17.kalkron.domain.scientific.BinaryOp.MULTIPLY
import click.e17.kalkron.domain.scientific.BinaryOp.POWER
import click.e17.kalkron.domain.scientific.BinaryOp.SUBTRACT
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 構文解析のテスト。演算子の優先順位や結合の向きが、
 * 構文木の形として正しく表れることを確かめる。
 */
class ParserTest {

    private fun parse(source: String): Expr = Parser.parse(Lexer.tokenize(source))
    private fun n(v: Double) = Expr.Num(v)
    private fun bin(op: BinaryOp, l: Expr, r: Expr) = Expr.Binary(op, l, r)

    @Test
    fun `掛け算は足し算より先に結び付く`() {
        assertEquals(bin(ADD, n(2.0), bin(MULTIPLY, n(3.0), n(4.0))), parse("2+3×4"))
    }

    @Test
    fun `括弧で順序を変えられる`() {
        assertEquals(bin(MULTIPLY, bin(ADD, n(2.0), n(3.0)), n(4.0)), parse("(2+3)×4"))
    }

    @Test
    fun `累乗は右から結び付く`() {
        assertEquals(bin(POWER, n(2.0), bin(POWER, n(3.0), n(2.0))), parse("2^3^2"))
    }

    @Test
    fun `単項マイナスは累乗より弱い`() {
        assertEquals(Expr.Negate(bin(POWER, n(2.0), n(2.0))), parse("−2^2"))
        assertEquals(Expr.Negate(Expr.Square(n(2.0))), parse("−2²"))
    }

    @Test
    fun `指数に単項マイナスを書ける`() {
        assertEquals(bin(POWER, n(2.0), Expr.Negate(n(1.0))), parse("2^−1"))
    }

    @Test
    fun `暗黙の掛け算`() {
        assertEquals(bin(MULTIPLY, n(2.0), Expr.Constant(Symbol.PI)), parse("2π"))
        assertEquals(bin(MULTIPLY, n(2.0), bin(ADD, n(1.0), n(3.0))), parse("2(1+3)"))
        assertEquals(
            bin(MULTIPLY, n(2.0), Expr.Call(MathFunction.SIN, listOf(Expr.Variable))),
            parse("2sin(x)"),
        )
    }

    @Test
    fun `マイナスは暗黙の掛け算にしない`() {
        assertEquals(bin(SUBTRACT, n(2.0), n(3.0)), parse("2−3"))
    }

    @Test
    fun `入力途中の小数点付きの数値を読める`() {
        assertEquals(n(12.0), parse("12."))
    }

    @Test
    fun `複数の引数を取る関数`() {
        assertEquals(
            Expr.Call(MathFunction.INTEGRAL, listOf(Expr.Square(Expr.Variable), n(0.0), n(1.0))),
            parse("∫(x²,0,1)"),
        )
    }

    @Test
    fun `正しくない式はエラー`() {
        listOf("", "(2+3", "2+", "2)", "×2", "2++3", ".", "sin(1,2)", "d/dx(x)", "∫(x,0)").forEach { source ->
            assertCalcError(CalcError.SYNTAX) { parse(source) }
        }
    }
}
