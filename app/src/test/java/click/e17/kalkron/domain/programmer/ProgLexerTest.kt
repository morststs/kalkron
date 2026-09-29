package click.e17.kalkron.domain.programmer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 字句解析のテスト。数値は指定した基数で読み、語長で正規化する。
 */
class ProgLexerTest {

    private fun num(v: Long) = ProgToken.Num(v)
    private fun sym(s: ProgSymbol) = ProgToken.Sym(s)

    @Test
    fun `DECの数値と演算子`() {
        assertEquals(listOf(num(12), sym(ProgSymbol.PLUS), num(3)), ProgLexer.tokenize("12 + 3"))
    }

    @Test
    fun `HEXではAからFも数字で語と衝突しない`() {
        assertEquals(
            listOf(num(255), sym(ProgSymbol.AND), num(15)),
            ProgLexer.tokenize("FF AND F", Radix.HEX),
        )
        assertEquals(listOf(num(0xADD)), ProgLexer.tokenize("ADD", Radix.HEX))
    }

    @Test
    fun `語の演算子`() {
        assertEquals(
            listOf(
                sym(ProgSymbol.NOT), num(1), sym(ProgSymbol.MOD), num(2), sym(ProgSymbol.NAND), num(3),
                sym(ProgSymbol.NOR), num(4), sym(ProgSymbol.XOR), num(5), sym(ProgSymbol.OR), num(6),
            ),
            ProgLexer.tokenize("NOT 1 MOD 2 NAND 3 NOR 4 XOR 5 OR 6"),
        )
        assertEquals(
            listOf(num(1), sym(ProgSymbol.SHL), num(2), sym(ProgSymbol.SHR), num(3), sym(ProgSymbol.ROL), num(4), sym(ProgSymbol.ROR), num(5)),
            ProgLexer.tokenize("1 << 2 >> 3 RoL 4 RoR 5"),
        )
    }

    @Test
    fun `括弧とANSとASCIIの演算子`() {
        assertEquals(
            listOf(sym(ProgSymbol.LEFT_PAREN), sym(ProgSymbol.ANS), sym(ProgSymbol.MINUS), num(1), sym(ProgSymbol.RIGHT_PAREN), sym(ProgSymbol.TIMES), num(2), sym(ProgSymbol.DIVIDE), num(3)),
            ProgLexer.tokenize("(ANS - 1) * 2 / 3"),
        )
    }

    @Test
    fun `数値は語長で正規化する`() {
        assertEquals(listOf(num(-1)), ProgLexer.tokenize("FF", Radix.HEX, WordSize.BYTE, signed = true))
        assertEquals(listOf(num(255)), ProgLexer.tokenize("FF", Radix.HEX, WordSize.BYTE, signed = false))
    }

    @Test
    fun `BINとOCTの数値`() {
        assertEquals(listOf(num(5)), ProgLexer.tokenize("101", Radix.BIN))
        assertEquals(listOf(num(8)), ProgLexer.tokenize("10", Radix.OCT))
    }

    @Test
    fun `読めない入力はエラー`() {
        assertProgError(ProgError.SYNTAX) { ProgLexer.tokenize("1 # 2") }
        // DEC では A は数字ではない
        assertProgError(ProgError.SYNTAX) { ProgLexer.tokenize("1A") }
        // 64ビットに収まらない数
        assertProgError(ProgError.SYNTAX) { ProgLexer.tokenize("99999999999999999999") }
    }
}
