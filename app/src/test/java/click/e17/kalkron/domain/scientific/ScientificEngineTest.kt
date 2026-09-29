package click.e17.kalkron.domain.scientific

import click.e17.kalkron.domain.Calculation
import click.e17.kalkron.domain.CalculatorMode
import click.e17.kalkron.domain.scientific.ScientificAction.Clear
import click.e17.kalkron.domain.scientific.ScientificAction.Decimal
import click.e17.kalkron.domain.scientific.ScientificAction.Delete
import click.e17.kalkron.domain.scientific.ScientificAction.Digit
import click.e17.kalkron.domain.scientific.ScientificAction.Equals
import click.e17.kalkron.domain.scientific.ScientificAction.Insert
import click.e17.kalkron.domain.scientific.ScientificAction.Key
import click.e17.kalkron.domain.scientific.ScientificAction.SetAngleUnit
import click.e17.kalkron.domain.scientific.ScientificAction.Store
import click.e17.kalkron.domain.scientific.ScientificAction.ToggleHyperbolic
import click.e17.kalkron.domain.scientific.ScientificAction.ToggleSecond
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 入力の編集と評価のテスト。キー操作の列を与えて、状態の変化を確かめる。
 */
class ScientificEngineTest {

    /** 状態に操作を順に適用する。最後の操作の結果を返す */
    private fun press(vararg actions: ScientificAction, from: ScientificState = ScientificState()): ScientificResult {
        var result = ScientificResult(from)
        actions.forEach { result = ScientificEngine.reduce(result.state, it) }
        return result
    }

    /** 式を直接入力した状態を作る（評価のテストを短く書くため） */
    private fun typed(source: String) = ScientificState(tokens = Lexer.tokenize(source))

    private fun tokensOf(result: ScientificResult) = result.state.tokens

    @Test
    fun `数字は1つの数値トークンにまとまる`() {
        assertEquals(listOf(Token.Num("12")), tokensOf(press(Digit(1), Digit(2))))
    }

    @Test
    fun `先頭の0は置き換える`() {
        assertEquals(listOf(Token.Num("5")), tokensOf(press(Digit(0), Digit(5))))
    }

    @Test
    fun `小数点は1つまで`() {
        assertEquals(listOf(Token.Num("1.5")), tokensOf(press(Digit(1), Decimal, Digit(5), Decimal)))
        assertEquals(listOf(Token.Num("0.")), tokensOf(press(Decimal)))
    }

    @Test
    fun `2ndとHYPで関数が切り替わり押した後はOFFに戻る`() {
        val plain = press(Key(SciKey.SIN))
        assertEquals(listOf(Token.Fn(MathFunction.SIN)), tokensOf(plain))

        val inverse = press(ToggleSecond, Key(SciKey.SIN))
        assertEquals(listOf(Token.Fn(MathFunction.ASIN)), tokensOf(inverse))
        assertFalse(inverse.state.second)

        val hyperbolic = press(ToggleHyperbolic, Key(SciKey.COS))
        assertEquals(listOf(Token.Fn(MathFunction.COSH)), tokensOf(hyperbolic))
        assertFalse(hyperbolic.state.hyperbolic)

        val both = press(ToggleSecond, ToggleHyperbolic, Key(SciKey.TAN))
        assertEquals(listOf(Token.Fn(MathFunction.ATANH)), tokensOf(both))
    }

    @Test
    fun `2ndで別の機能になるキー`() {
        assertEquals(Token.Fn(MathFunction.LOG), ScientificEngine.resolve(SciKey.LN, second = true, hyperbolic = false))
        assertEquals(Token.Sym(Symbol.POWER), ScientificEngine.resolve(SciKey.X_SQUARED, second = true, hyperbolic = false))
        assertEquals(Token.Fn(MathFunction.EXP), ScientificEngine.resolve(SciKey.SQRT, second = true, hyperbolic = false))
        assertEquals(Token.Sym(Symbol.SQUARE), ScientificEngine.resolve(SciKey.X_SQUARED, second = false, hyperbolic = true))
    }

    @Test
    fun `2ndを2回押すとOFF`() {
        assertFalse(press(ToggleSecond, ToggleSecond).state.second)
    }

    @Test
    fun `削除は数値を1文字ずつ関数をまとめて消す`() {
        assertEquals(listOf(Token.Num("1")), tokensOf(press(Digit(1), Digit(2), Delete)))
        assertEquals(listOf(Token.Num("2")), tokensOf(press(Digit(2), Key(SciKey.SIN), Delete)))
        assertEquals(emptyList<Token>(), tokensOf(press(Delete)))
    }

    @Test
    fun `ACは式だけを消しANSとメモリと角度単位は残す`() {
        val before = ScientificState(
            tokens = Lexer.tokenize("1+2"),
            angleUnit = AngleUnit.DEG,
            ans = 7.0,
            memory = 3.0,
            error = CalcError.SYNTAX,
        )
        val after = press(Clear, from = before).state
        assertEquals(emptyList<Token>(), after.tokens)
        assertNull(after.error)
        assertEquals(AngleUnit.DEG, after.angleUnit)
        assertEquals(7.0, after.ans, 0.0)
        assertEquals(3.0, after.memory)
    }

    @Test
    fun `イコールで計算し履歴用の計算を返す`() {
        val result = press(Equals, from = typed("2+3×4"))
        assertEquals(14.0, result.state.result)
        assertEquals(14.0, result.state.ans, 0.0)
        assertEquals(Calculation("2 + 3 × 4", "14", CalculatorMode.SCIENTIFIC), result.completed)
    }

    @Test
    fun `空の式のイコールは何もしない`() {
        val result = press(Equals)
        assertNull(result.completed)
        assertNull(result.state.result)
    }

    @Test
    fun `確定後のイコールは履歴を二重に作らない`() {
        val first = press(Equals, from = typed("1+1"))
        val second = press(Equals, from = first.state)
        assertNull(second.completed)
    }

    @Test
    fun `確定後の数字は新しい式を始める`() {
        val done = press(Equals, from = typed("2+3")).state
        assertEquals(listOf(Token.Num("7")), tokensOf(press(Digit(7), from = done)))
        assertEquals(listOf(Token.Fn(MathFunction.SIN)), tokensOf(press(Key(SciKey.SIN), from = done)))
    }

    @Test
    fun `確定後の演算子はANSに続ける`() {
        val done = press(Equals, from = typed("2+3")).state
        assertEquals(
            listOf(Token.Sym(Symbol.ANS), Token.Sym(Symbol.PLUS)),
            tokensOf(press(Insert(Symbol.PLUS), from = done)),
        )
        assertEquals(
            listOf(Token.Sym(Symbol.ANS), Token.Sym(Symbol.SQUARE)),
            tokensOf(press(Key(SciKey.X_SQUARED), from = done)),
        )
        // 2 + 3 = 5 に続けて + 1 = で 6
        assertEquals(6.0, press(Insert(Symbol.PLUS), Digit(1), Equals, from = done).state.result)
    }

    @Test
    fun `エラーのとき式は残り履歴は作らない`() {
        val result = press(Equals, from = typed("1÷0"))
        assertEquals(CalcError.DIVISION_BY_ZERO, result.state.error)
        assertEquals(Lexer.tokenize("1÷0"), result.state.tokens)
        assertNull(result.completed)
    }

    @Test
    fun `エラーの後の入力でエラーが消える`() {
        val failed = press(Equals, from = typed("1÷0")).state
        val deleted = press(Delete, from = failed).state
        assertNull(deleted.error)
        assertEquals(Lexer.tokenize("1÷"), deleted.tokens)

        // 末尾の "0" は先頭の 0 として置き換わる
        val typedMore = press(Digit(2), from = failed).state
        assertNull(typedMore.error)
        assertEquals(Lexer.tokenize("1÷2"), typedMore.tokens)
    }

    @Test
    fun `STOで直前の結果を保存しRCLで式に入れる`() {
        val stored = press(Equals, Store, from = typed("5")).state
        assertEquals(5.0, stored.memory)

        val recalled = press(ToggleSecond, Store, from = ScientificState(memory = 5.0))
        assertEquals(listOf(Token.Sym(Symbol.MEMORY)), tokensOf(recalled))
        assertFalse(recalled.state.second)

        val used = press(Insert(Symbol.TIMES), Digit(2), Equals, from = recalled.state)
        assertEquals(10.0, used.state.result)
    }

    @Test
    fun `角度単位を切り替えて計算する`() {
        val result = press(SetAngleUnit(AngleUnit.DEG), Equals, from = typed("sin(30)"))
        assertEquals(0.5, result.state.result!!, 1e-15)
    }

    @Test
    fun `先出しは式が成り立つ間だけ値を返す`() {
        assertEquals(5.0, ScientificEngine.preview(typed("2+3")))
        assertNull(ScientificEngine.preview(typed("2+")))
        assertNull(ScientificEngine.preview(ScientificState()))
        assertNull(ScientificEngine.preview(typed("1÷0")))
    }

    @Test
    fun `角度単位を切り替えると確定結果を消して新しい単位で先出しする`() {
        val done = press(Equals, from = typed("sin(30)")).state
        val switched = press(SetAngleUnit(AngleUnit.DEG), from = done).state
        assertNull(switched.result)
        assertEquals(0.5, ScientificEngine.preview(switched)!!, 1e-15)

        val recalculated = press(Equals, from = switched)
        assertEquals(0.5, recalculated.state.result!!, 1e-15)
        assertEquals("0.5", recalculated.completed?.result)
    }

    @Test
    fun `微分と積分を含む式は先出ししない`() {
        // 先出しはキー操作のたびにメインスレッドで計算するため、重い計算は = のときだけにする
        assertNull(ScientificEngine.preview(typed("∫(x²,0,1)")))
        assertNull(ScientificEngine.preview(typed("d/dx(x²,1)")))
    }

    @Test
    fun `履歴にはANSとMを値に置き換えた式を残す`() {
        val result = press(Equals, from = typed("ANS×2+M").copy(ans = 5.0, memory = 1.5))
        assertEquals(Calculation("5 × 2 + 1.5", "11.5", CalculatorMode.SCIENTIFIC), result.completed)
        // 画面の式は ANS のまま
        assertEquals("ANS × 2 + M", ScientificFormat.expression(result.state.tokens))
    }

    @Test
    fun `負の値に置き換えるときは括弧で囲む`() {
        val result = press(Equals, from = typed("2^ANS").copy(ans = -1.0))
        assertEquals("2^(−1)", result.completed?.expression)
        val tiny = press(Equals, from = typed("ANS×2").copy(ans = -1.5e-20))
        assertEquals("(−1.5E-20) × 2", tiny.completed?.expression)
    }

    @Test
    fun `暗黙の掛け算で数と隣り合うときも括弧で囲む`() {
        // 2ANS をそのまま 25 と書くと、25 という1つの数に読めてしまう
        assertEquals("2(5)", press(Equals, from = typed("2ANS").copy(ans = 5.0)).completed?.expression)
        assertEquals("(5)π", press(Equals, from = typed("ANSπ").copy(ans = 5.0)).completed?.expression)
        assertEquals("sin(5)", press(Equals, from = typed("sin(ANS)").copy(ans = 5.0)).completed?.expression)
    }
}
