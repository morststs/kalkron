package click.e17.kalkron.domain.programmer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 評価器のテスト。語長・符号の有無ごとに、CPU と同じ結果になることを確かめる。
 */
class ProgEvaluatorTest {

    private fun calc(
        source: String,
        wordSize: WordSize = WordSize.DWORD,
        signed: Boolean = true,
        radix: Radix = Radix.DEC,
        ans: Long = 0L,
    ): Long = ProgEvaluator.evaluate(
        ProgParser.parse(ProgLexer.tokenize(source, radix, wordSize, signed)),
        ProgContext(wordSize, signed, ans),
    )

    @Test
    fun `優先順位どおりに計算する`() {
        assertEquals(7L, calc("1 + 2 × 3"))
        assertEquals(32L, calc("1 << 2 + 3"))
        assertEquals(3L, calc("1 OR 2 AND 3"))
    }

    @Test
    fun `桁あふれは回り込む`() {
        assertEquals(-128L, calc("127 + 1", WordSize.BYTE, signed = true))
        assertEquals(0L, calc("255 + 1", WordSize.BYTE, signed = false))
        assertEquals(Long.MIN_VALUE, calc("9223372036854775807 + 1", WordSize.QWORD, signed = true))
    }

    @Test
    fun `符号ありの割り算は0に向かって切り捨て`() {
        assertEquals(-3L, calc("−7 ÷ 2"))
        assertEquals(-1L, calc("−7 MOD 2"))
    }

    @Test
    fun `符号なしの割り算`() {
        assertEquals(0x7CL, calc("F9 ÷ 2", WordSize.BYTE, signed = false, radix = Radix.HEX))
        // 64ビット符号なしの最大値 ÷ 2
        assertEquals(Long.MAX_VALUE, calc("FFFFFFFFFFFFFFFF ÷ 2", WordSize.QWORD, signed = false, radix = Radix.HEX))
        assertEquals(1L, calc("FFFFFFFFFFFFFFFF MOD 2", WordSize.QWORD, signed = false, radix = Radix.HEX))
    }

    @Test
    fun `符号ありの最小値をマイナス1で割ると回り込む`() {
        assertEquals(-128L, calc("−128 ÷ −1", WordSize.BYTE, signed = true))
    }

    @Test
    fun `右シフトは符号ありなら算術で符号なしなら論理`() {
        assertEquals(-4L, calc("−8 >> 1"))
        assertEquals(0x7FFFFFFCL, calc("FFFFFFF8 >> 1", WordSize.DWORD, signed = false, radix = Radix.HEX))
        assertEquals(Long.MAX_VALUE, calc("FFFFFFFFFFFFFFFF >> 1", WordSize.QWORD, signed = false, radix = Radix.HEX))
    }

    @Test
    fun `語長以上のシフトは全ビットが押し出される`() {
        assertEquals(0L, calc("1 << 32"))
        assertEquals(0L, calc("8 >> 40"))
        assertEquals(-1L, calc("−8 >> 40"))
    }

    @Test
    fun `シフトとローテートの回数が負ならエラー`() {
        assertProgError(ProgError.NEGATIVE_SHIFT) { calc("1 << −1") }
        assertProgError(ProgError.NEGATIVE_SHIFT) { calc("1 >> −1") }
        assertProgError(ProgError.NEGATIVE_SHIFT) { calc("1 RoL −1") }
    }

    @Test
    fun `ローテートは語長の中で回す`() {
        assertEquals(0x03L, calc("81 RoL 1", WordSize.BYTE, signed = false, radix = Radix.HEX))
        assertEquals(0xC0L, calc("81 RoR 1", WordSize.BYTE, signed = false, radix = Radix.HEX))
        // 回数は語長で割った余り（9 は 1 と同じ）
        assertEquals(0x03L, calc("81 RoL 9", WordSize.BYTE, signed = false, radix = Radix.HEX))
        assertEquals(0x81L, calc("81 RoL 8", WordSize.BYTE, signed = false, radix = Radix.HEX))
        assertEquals(0x18L, calc("8000000000000001 RoL 4", WordSize.QWORD, signed = false, radix = Radix.HEX))
    }

    @Test
    fun `NOTとNANDとNOR`() {
        assertEquals(-1L, calc("NOT 0", WordSize.BYTE, signed = true))
        assertEquals(255L, calc("NOT 0", WordSize.BYTE, signed = false))
        assertEquals(0x0FL, calc("F0 NAND FF", WordSize.BYTE, signed = false, radix = Radix.HEX))
        assertEquals(0L, calc("F0 NOR 0F", WordSize.BYTE, signed = false, radix = Radix.HEX))
    }

    @Test
    fun `ANSの値を使う`() {
        assertEquals(10L, calc("ANS × 2", ans = 5L))
    }

    @Test
    fun `0で割るとエラー`() {
        assertProgError(ProgError.DIVISION_BY_ZERO) { calc("1 ÷ 0") }
        assertProgError(ProgError.DIVISION_BY_ZERO) { calc("1 MOD 0") }
    }
}
