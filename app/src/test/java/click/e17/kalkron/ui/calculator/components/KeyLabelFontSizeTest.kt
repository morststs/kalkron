package click.e17.kalkron.ui.calculator.components

import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * キーの文字の大きさのテスト。
 * 1文字のキーは役割ごとの大きさ、2文字以上の単語は種類にかかわらず単語の大きさにする。
 */
class KeyLabelFontSizeTest {

    @Test
    fun `1文字のキーは役割ごとの大きさ`() {
        assertEquals(22.sp, keyLabelFontSize(CalculatorButtonStyle.Number, "7"))
        assertEquals(22.sp, keyLabelFontSize(CalculatorButtonStyle.Number, "A"))
        assertEquals(26.sp, keyLabelFontSize(CalculatorButtonStyle.Operator, "÷"))
        assertEquals(26.sp, keyLabelFontSize(CalculatorButtonStyle.Accent, "="))
        assertEquals(16.sp, keyLabelFontSize(CalculatorButtonStyle.Function, "π"))
    }

    @Test
    fun `2文字以上の単語は種類にかかわらず単語の大きさ`() {
        // 演算子キーの MOD が 26sp で表示されると、隣の単語キーの倍近い大きさになってしまう
        assertEquals(16.sp, keyLabelFontSize(CalculatorButtonStyle.Operator, "MOD"))
        assertEquals(16.sp, keyLabelFontSize(CalculatorButtonStyle.Function, "AC"))
        assertEquals(16.sp, keyLabelFontSize(CalculatorButtonStyle.Function, "sin⁻¹"))
        assertEquals(16.sp, keyLabelFontSize(CalculatorButtonStyle.Function, "x²"))
    }
}
