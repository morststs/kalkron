package click.e17.kalkron.domain.scientific

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 字句解析のテスト。文字列が正しい単位（トークン）に分かれることを確かめる。
 */
class LexerTest {

    private fun num(s: String) = Token.Num(s)
    private fun sym(s: Symbol) = Token.Sym(s)
    private fun fn(f: MathFunction) = Token.Fn(f)

    @Test
    fun `数値と演算子に分かれる`() {
        assertEquals(
            listOf(num("12.5"), sym(Symbol.PLUS), num("3")),
            Lexer.tokenize("12.5+3"),
        )
    }

    @Test
    fun `空白は無視する`() {
        assertEquals(
            listOf(num("1"), sym(Symbol.TIMES), num("2")),
            Lexer.tokenize(" 1 × 2 "),
        )
    }

    @Test
    fun `関数は開き括弧までを1つのトークンにする`() {
        assertEquals(
            listOf(fn(MathFunction.ASIN), num("0.5"), sym(Symbol.RIGHT_PAREN)),
            Lexer.tokenize("sin⁻¹(0.5)"),
        )
    }

    @Test
    fun `長い関数名を優先する`() {
        // "sinh(" を "sin(" と "h" に分けてはいけない
        assertEquals(listOf(fn(MathFunction.SINH), sym(Symbol.X), sym(Symbol.RIGHT_PAREN)), Lexer.tokenize("sinh(x)"))
        assertEquals(listOf(fn(MathFunction.EXP), num("1"), sym(Symbol.RIGHT_PAREN)), Lexer.tokenize("exp(1)"))
    }

    @Test
    fun `区切りの無い入力も分解できる`() {
        assertEquals(listOf(num("2"), sym(Symbol.PI)), Lexer.tokenize("2π"))
        assertEquals(listOf(sym(Symbol.ANS), sym(Symbol.TIMES), sym(Symbol.MEMORY)), Lexer.tokenize("ANS×M"))
    }

    @Test
    fun `微分と積分の関数を読める`() {
        assertEquals(
            listOf(
                fn(MathFunction.DERIVATIVE), sym(Symbol.X), sym(Symbol.SQUARE),
                sym(Symbol.COMMA), num("0"), sym(Symbol.RIGHT_PAREN),
            ),
            Lexer.tokenize("d/dx(x², 0)"),
        )
        assertEquals(fn(MathFunction.INTEGRAL), Lexer.tokenize("∫(x,0,1)").first())
    }

    @Test
    fun `ASCIIの演算子も受け付ける`() {
        assertEquals(
            listOf(num("2"), sym(Symbol.TIMES), num("3"), sym(Symbol.MINUS), num("4"), sym(Symbol.DIVIDE), num("5")),
            Lexer.tokenize("2*3-4/5"),
        )
    }

    @Test
    fun `小数点が2つある数値はエラー`() {
        assertCalcError(CalcError.SYNTAX) { Lexer.tokenize("1.2.3") }
    }

    @Test
    fun `知らない文字はエラー`() {
        assertCalcError(CalcError.SYNTAX) { Lexer.tokenize("2 # 3") }
    }
}
