package click.e17.kalkron.ui.programmer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * BIN の表示を空白の区切り（4ビットごと）で複数行に分ける処理のテスト。
 */
class SplitGroupsTest {

    @Test
    fun `64ビットは32ビットずつ同じ長さの2行に分ける`() {
        val bin = "1111 1110 1101 1100 1011 1010 1001 1000 0111 0110 0101 0100 0011 0010 0001 0000"
        val lines = splitGroups(bin, lineCount = 2)
        assertEquals(
            listOf(
                "1111 1110 1101 1100 1011 1010 1001 1000",
                "0111 0110 0101 0100 0011 0010 0001 0000",
            ),
            lines,
        )
        assertEquals(lines[0].length, lines[1].length)
    }

    @Test
    fun `区切りの数が割り切れないときは上の行を長くする`() {
        assertEquals(listOf("1 2", "3"), splitGroups("1 2 3", lineCount = 2))
    }

    @Test
    fun `1行のときや区切りが行数より少ないときはそのまま`() {
        assertEquals(listOf("1010 0101"), splitGroups("1010 0101", lineCount = 1))
        assertEquals(listOf("1010"), splitGroups("1010", lineCount = 2))
    }
}
