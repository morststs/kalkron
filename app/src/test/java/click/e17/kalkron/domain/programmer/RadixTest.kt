package click.e17.kalkron.domain.programmer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 基数ごとの表示形式のテスト。
 */
class RadixTest {

    @Test
    fun `負の数はHEXとOCTとBINではビットの並びを表示する`() {
        assertEquals("FF", Radix.HEX.plain(-1, WordSize.BYTE, signed = true))
        assertEquals("377", Radix.OCT.plain(-1, WordSize.BYTE, signed = true))
        assertEquals("1111 1111", Radix.BIN.grouped(-1, WordSize.BYTE, signed = true))
    }

    @Test
    fun `DECは符号の有無に従う`() {
        assertEquals("-1", Radix.DEC.plain(-1, WordSize.BYTE, signed = true))
        assertEquals("255", Radix.DEC.plain(255, WordSize.BYTE, signed = false))
    }

    @Test
    fun `64ビット符号なしの最大値`() {
        assertEquals("18,446,744,073,709,551,615", Radix.DEC.grouped(-1, WordSize.QWORD, signed = false))
        assertEquals("FFFFFFFFFFFFFFFF", Radix.HEX.plain(-1, WordSize.QWORD, signed = false))
    }

    @Test
    fun `設計書の例と一致する`() {
        val v = 0x7FFF80A0L
        assertEquals("7FFF80A0", Radix.HEX.grouped(v, WordSize.DWORD, signed = true))
        assertEquals("2,147,451,040", Radix.DEC.grouped(v, WordSize.DWORD, signed = true))
        assertEquals("17777700240", Radix.OCT.grouped(v, WordSize.DWORD, signed = true))
        assertEquals("111 1111 1111 1111 1000 0000 1010 0000", Radix.BIN.grouped(v, WordSize.DWORD, signed = true))
    }

    @Test
    fun `DECの負の数も3桁区切り`() {
        assertEquals("-1,234", Radix.DEC.grouped(-1234, WordSize.DWORD, signed = true))
    }

    @Test
    fun `0はどの基数でも0`() {
        Radix.entries.forEach { assertEquals("0", it.grouped(0, WordSize.DWORD, signed = true)) }
    }

    @Test
    fun `使える数字`() {
        assertTrue(Radix.BIN.accepts(1))
        assertFalse(Radix.BIN.accepts(2))
        assertTrue(Radix.OCT.accepts(7))
        assertFalse(Radix.OCT.accepts(8))
        assertFalse(Radix.DEC.accepts(10))
        assertTrue(Radix.HEX.accepts(15))
    }
}
