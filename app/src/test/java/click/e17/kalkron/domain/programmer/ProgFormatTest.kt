package click.e17.kalkron.domain.programmer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 式と結果の表示用の整形のテスト。
 */
class ProgFormatTest {

    private fun expr(
        source: String,
        radix: Radix = Radix.DEC,
        wordSize: WordSize = WordSize.DWORD,
        signed: Boolean = true,
    ) = ProgFormat.expression(ProgLexer.tokenize(source, radix, wordSize, signed), radix, wordSize, signed)

    @Test
    fun `演算子の前後に空白を入れる`() {
        assertEquals("FF AND F", expr("FF AND F", Radix.HEX))
        assertEquals("(1 + 2) << 3", expr("(1+2)<<3"))
    }

    @Test
    fun `単項のマイナスには空白を入れずNOTの後ろには入れる`() {
        assertEquals("−5 × 3", expr("−5×3"))
        assertEquals("2 − −3", expr("2−−3"))
        assertEquals("NOT 0", expr("NOT 0"))
        assertEquals("1 AND NOT (2)", expr("1 AND NOT (2)"))
    }

    @Test
    fun `数値は今の基数で表示する`() {
        val tokens = listOf(ProgToken.Num(-5))
        // 負の数の符号は、キーで入れる単項の − と同じ記号にする
        assertEquals("−5", ProgFormat.expression(tokens, Radix.DEC, WordSize.BYTE, signed = true))
        assertEquals("10 − −5", ProgFormat.expression(listOf(ProgToken.Num(10), ProgToken.Sym(ProgSymbol.MINUS)) + tokens, Radix.DEC, WordSize.BYTE, signed = true))
        assertEquals("FB", ProgFormat.expression(tokens, Radix.HEX, WordSize.BYTE, signed = true))
    }

    @Test
    fun `演算子で終わる入力途中の式`() {
        assertEquals("1 +", expr("1 +"))
        assertEquals("", ProgFormat.expression(emptyList(), Radix.DEC, WordSize.DWORD, signed = true))
    }

    @Test
    fun `結果は基数名を付ける`() {
        assertEquals("F (HEX)", ProgFormat.result(15, Radix.HEX, WordSize.DWORD, signed = true))
        assertEquals("1,000,000 (DEC)", ProgFormat.result(1_000_000, Radix.DEC, WordSize.DWORD, signed = true))
        assertEquals("1111 0000 (BIN)", ProgFormat.result(0xF0, Radix.BIN, WordSize.BYTE, signed = false))
    }
}
