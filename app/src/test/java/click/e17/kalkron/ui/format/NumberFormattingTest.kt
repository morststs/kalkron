package click.e17.kalkron.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 表示用の 3 桁区切り整形のテスト。
 * 入力途中の文字列（"12." など）を壊さないことが重要なので、そこも確認する。
 */
class NumberFormattingTest {

    @Test
    fun `4桁以上に区切りが入る`() {
        assertEquals("1,024", groupDigits("1024"))
        assertEquals("50,032", groupDigits("50032"))
        assertEquals("123,456,789", groupDigits("123456789"))
    }

    @Test
    fun `3桁以下はそのまま`() {
        assertEquals("0", groupDigits("0"))
        assertEquals("999", groupDigits("999"))
    }

    @Test
    fun `小数部には区切りを入れない`() {
        assertEquals("1,024.5", groupDigits("1024.5"))
        assertEquals("50,032.00", groupDigits("50032.00"))
        assertEquals("0.123456", groupDigits("0.123456"))
    }

    @Test
    fun `入力途中の小数点を壊さない`() {
        assertEquals("1,024.", groupDigits("1024."))
        assertEquals("0.", groupDigits("0."))
    }

    @Test
    fun `負の符号を保つ`() {
        assertEquals("-1,024", groupDigits("-1024"))
        assertEquals("-0.5", groupDigits("-0.5"))
    }

    @Test
    fun `指数表記やエラー文字は触らない`() {
        assertEquals("1.000000E+20", groupDigits("1.000000E+20"))
        assertEquals("エラー", groupDigits("エラー"))
        assertEquals("", groupDigits(""))
    }

    @Test
    fun `式の中の数値だけを区切る`() {
        assertEquals("1,024 +", groupExpression("1024 +"))
        assertEquals("1,024 × 48.5", groupExpression("1024 × 48.5"))
        assertEquals("10 ÷ 3", groupExpression("10 ÷ 3"))
    }

    @Test
    fun `式の演算子記号は区切り対象にならない`() {
        // 引き算の記号は U+2212 なので、符号の "-" とは別物として扱われる
        assertEquals("12,345 − 6,789", groupExpression("12345 − 6789"))
    }
}
