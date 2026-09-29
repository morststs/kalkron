package click.e17.kalkron.domain.programmer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 語長への正規化のテスト。
 * 符号ありは最上位ビットを符号として広げ、符号なしは上のビットを 0 にする。
 */
class WordSizeTest {

    @Test
    fun `8ビット符号ありではFFはマイナス1`() {
        assertEquals(-1L, WordSize.BYTE.normalize(0xFF, signed = true))
        assertEquals(127L, WordSize.BYTE.normalize(0x7F, signed = true))
        assertEquals(-128L, WordSize.BYTE.normalize(0x80, signed = true))
    }

    @Test
    fun `8ビット符号なしではFFは255`() {
        assertEquals(255L, WordSize.BYTE.normalize(0xFF, signed = false))
        assertEquals(255L, WordSize.BYTE.normalize(-1, signed = false))
    }

    @Test
    fun `語長を超えたビットは捨てる`() {
        assertEquals(1L, WordSize.BYTE.normalize(0x101, signed = true))
        assertEquals(0L, WordSize.WORD.normalize(0x10000, signed = false))
        assertEquals(1L, WordSize.DWORD.normalize(0x1_0000_0001, signed = true))
    }

    @Test
    fun `64ビットはそのまま`() {
        assertEquals(-1L, WordSize.QWORD.normalize(-1, signed = true))
        assertEquals(-1L, WordSize.QWORD.normalize(-1, signed = false))
    }

    @Test
    fun `ビットの並びとマスク`() {
        assertEquals(0xFFL, WordSize.BYTE.pattern(-1))
        assertEquals(0xFFFFFFFFL, WordSize.DWORD.mask)
        assertEquals(-1L, WordSize.QWORD.mask)
    }

    @Test
    fun `符号ありの範囲`() {
        assertEquals(127L, WordSize.BYTE.maxSigned())
        assertEquals(-128L, WordSize.BYTE.minSigned())
        assertEquals(Long.MAX_VALUE, WordSize.QWORD.maxSigned())
        assertEquals(Long.MIN_VALUE, WordSize.QWORD.minSigned())
    }
}
