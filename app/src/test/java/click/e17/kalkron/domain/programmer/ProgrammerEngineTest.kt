package click.e17.kalkron.domain.programmer

import click.e17.kalkron.domain.Calculation
import click.e17.kalkron.domain.CalculatorMode
import click.e17.kalkron.domain.programmer.ProgrammerAction.Clear
import click.e17.kalkron.domain.programmer.ProgrammerAction.Complement
import click.e17.kalkron.domain.programmer.ProgrammerAction.Delete
import click.e17.kalkron.domain.programmer.ProgrammerAction.Digit
import click.e17.kalkron.domain.programmer.ProgrammerAction.Equals
import click.e17.kalkron.domain.programmer.ProgrammerAction.FlipBit
import click.e17.kalkron.domain.programmer.ProgrammerAction.Insert
import click.e17.kalkron.domain.programmer.ProgrammerAction.SetRadix
import click.e17.kalkron.domain.programmer.ProgrammerAction.SetWordSize
import click.e17.kalkron.domain.programmer.ProgrammerAction.ToggleSigned
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 入力の編集と評価のテスト。
 */
class ProgrammerEngineTest {

    private fun press(vararg actions: ProgrammerAction, from: ProgrammerState = ProgrammerState()): ProgrammerResult {
        var result = ProgrammerResult(from)
        actions.forEach { result = ProgrammerEngine.reduce(result.state, it) }
        return result
    }

    /** 式を直接入力した状態を作る */
    private fun typed(
        source: String,
        radix: Radix = Radix.DEC,
        wordSize: WordSize = WordSize.DWORD,
        signed: Boolean = true,
    ) = ProgrammerState(
        tokens = ProgLexer.tokenize(source, radix, wordSize, signed),
        radix = radix,
        wordSize = wordSize,
        signed = signed,
    )

    private fun num(v: Long) = ProgToken.Num(v)
    private fun sym(s: ProgSymbol) = ProgToken.Sym(s)

    @Test
    fun `数字を続けて押すと1つの数になる`() {
        assertEquals(listOf(num(12)), press(Digit(1), Digit(2)).state.tokens)
    }

    @Test
    fun `今の基数で使えない数字は受け付けない`() {
        val bin = ProgrammerState(radix = Radix.BIN)
        assertEquals(emptyList<ProgToken>(), press(Digit(2), from = bin).state.tokens)
        assertEquals(listOf(num(5)), press(Digit(1), Digit(0), Digit(1), from = bin).state.tokens)
    }

    @Test
    fun `HEXでは16倍して桁を足す`() {
        val hex = ProgrammerState(radix = Radix.HEX)
        assertEquals(listOf(num(255)), press(Digit(15), Digit(15), from = hex).state.tokens)
    }

    @Test
    fun `語長を超える桁は受け付けない`() {
        val unsigned = ProgrammerState(radix = Radix.HEX, wordSize = WordSize.BYTE, signed = false)
        assertEquals(listOf(num(255)), press(Digit(15), Digit(15), Digit(15), from = unsigned).state.tokens)

        // HEX はビットの並びとして入力できるので、符号ありなら FF は −1 になる
        val signed = ProgrammerState(radix = Radix.HEX, wordSize = WordSize.BYTE, signed = true)
        assertEquals(listOf(num(-1)), press(Digit(15), Digit(15), from = signed).state.tokens)
    }

    @Test
    fun `DECの符号ありは符号ありの範囲まで`() {
        val byte = ProgrammerState(wordSize = WordSize.BYTE)
        assertEquals(listOf(num(127)), press(Digit(1), Digit(2), Digit(7), from = byte).state.tokens)
        assertEquals(listOf(num(12)), press(Digit(1), Digit(2), Digit(8), from = byte).state.tokens)
    }

    @Test
    fun `負の数には絶対値に桁を足す`() {
        assertEquals(listOf(num(-53)), press(Digit(5), Complement, Digit(3)).state.tokens)
    }

    @Test
    fun `削除は今の基数で1桁ずつ`() {
        val hex = ProgrammerState(radix = Radix.HEX)
        assertEquals(listOf(num(1)), press(Digit(1), Digit(15), Delete, from = hex).state.tokens)
        assertEquals(emptyList<ProgToken>(), press(Digit(1), Digit(15), Delete, Delete, from = hex).state.tokens)
        assertEquals(listOf(num(-12)), press(Delete, from = typed("0").copy(tokens = listOf(num(-123)))).state.tokens)
    }

    @Test
    fun `演算子は丸ごと消す`() {
        assertEquals(listOf(num(1)), press(Digit(1), Insert(ProgSymbol.NAND), Delete).state.tokens)
    }

    @Test
    fun `基数を切り替えても値は変わらない`() {
        val switched = press(SetRadix(Radix.HEX), from = typed("255")).state
        assertEquals(listOf(num(255)), switched.tokens)
        assertEquals(Radix.HEX, switched.radix)
    }

    @Test
    fun `語長を縮めると式の中の数と結果とANSを切り詰める`() {
        val shrunk = press(SetWordSize(WordSize.BYTE), from = ProgrammerState(tokens = listOf(num(0x101)))).state
        assertEquals(listOf(num(1)), shrunk.tokens)

        val done = press(Equals, from = typed("300")).state
        val byte = press(SetWordSize(WordSize.BYTE), from = done).state
        assertEquals(44L, byte.result)
        assertEquals(44L, byte.ans)
    }

    @Test
    fun `符号を切り替えると解釈が変わる`() {
        val state = ProgrammerState(tokens = listOf(num(-1)), wordSize = WordSize.BYTE, signed = true, ans = -1)
        val unsigned = press(ToggleSigned, from = state).state
        assertEquals(listOf(num(255)), unsigned.tokens)
        assertEquals(255L, unsigned.ans)
        assertEquals(false, unsigned.signed)
    }

    @Test
    fun `ビット反転は数で終わっていればその数を変える`() {
        assertEquals(listOf(num(7)), press(FlipBit(1), from = typed("5")).state.tokens)
    }

    @Test
    fun `ビット反転は演算子で終わっていればそのビットだけの数を足す`() {
        assertEquals(listOf(num(5), sym(ProgSymbol.PLUS), num(8)), press(FlipBit(3), from = typed("5 +")).state.tokens)
    }

    @Test
    fun `ビット反転はイコールの直後なら結果を反転して新しい式を始める`() {
        val done = press(Equals, from = typed("5")).state
        val flipped = press(FlipBit(0), from = done).state
        assertEquals(listOf(num(4)), flipped.tokens)
        assertNull(flipped.result)
    }

    @Test
    fun `語長の外のビットは反転しない`() {
        val byte = typed("5", wordSize = WordSize.BYTE)
        assertEquals(byte, press(FlipBit(8), from = byte).state)
    }

    @Test
    fun `2の補数は編集中の数を符号反転する`() {
        assertEquals(listOf(num(-5)), press(Complement, from = typed("5")).state.tokens)

        val done = press(Equals, from = typed("5")).state
        val negated = press(Complement, from = done).state
        assertEquals(listOf(num(-5)), negated.tokens)
        assertNull(negated.result)

        val endsWithOperator = typed("5 +")
        assertEquals(endsWithOperator, press(Complement, from = endsWithOperator).state)
    }

    @Test
    fun `イコールで計算し履歴用の計算を返す`() {
        val result = press(Equals, from = typed("FF AND F", Radix.HEX))
        assertEquals(15L, result.state.result)
        assertEquals(15L, result.state.ans)
        assertEquals(Calculation("FF AND F", "F (HEX)", CalculatorMode.PROGRAMMER), result.completed)
    }

    @Test
    fun `DECの結果は3桁区切りで保存する`() {
        assertEquals("1,000,000 (DEC)", press(Equals, from = typed("1000 × 1000")).completed?.result)
    }

    @Test
    fun `空の式と確定後のイコールは何もしない`() {
        assertNull(press(Equals).completed)
        val done = press(Equals, from = typed("1 + 1")).state
        assertNull(press(Equals, from = done).completed)
    }

    @Test
    fun `確定後の数字は新しい式で二項演算子はANSに続ける`() {
        val done = press(Equals, from = typed("2 + 3")).state
        assertEquals(listOf(num(7)), press(Digit(7), from = done).state.tokens)
        assertEquals(listOf(sym(ProgSymbol.ANS), sym(ProgSymbol.PLUS)), press(Insert(ProgSymbol.PLUS), from = done).state.tokens)
        assertEquals(listOf(sym(ProgSymbol.NOT)), press(Insert(ProgSymbol.NOT), from = done).state.tokens)
        assertEquals(10L, press(Insert(ProgSymbol.TIMES), Digit(2), Equals, from = done).state.result)
    }

    @Test
    fun `エラーのとき式は残り削除でエラーが消える`() {
        val failed = press(Equals, from = typed("1 ÷ 0"))
        assertEquals(ProgError.DIVISION_BY_ZERO, failed.state.error)
        assertNull(failed.completed)
        assertEquals(ProgLexer.tokenize("1 ÷ 0"), failed.state.tokens)

        val deleted = press(Delete, from = failed.state).state
        assertNull(deleted.error)
        assertEquals(ProgLexer.tokenize("1 ÷"), deleted.tokens)
    }

    @Test
    fun `ACは式を消し設定とANSは残す`() {
        val before = typed("1 + 2", Radix.HEX, WordSize.BYTE, signed = false).copy(ans = 9, error = ProgError.SYNTAX)
        val after = press(Clear, from = before).state
        assertEquals(emptyList<ProgToken>(), after.tokens)
        assertNull(after.error)
        assertEquals(Radix.HEX, after.radix)
        assertEquals(WordSize.BYTE, after.wordSize)
        assertEquals(false, after.signed)
        assertEquals(9L, after.ans)
    }

    @Test
    fun `先出しは式が成り立つ間だけ値を返す`() {
        assertEquals(5L, ProgrammerEngine.preview(typed("2 + 3")))
        assertNull(ProgrammerEngine.preview(typed("2 +")))
        assertNull(ProgrammerEngine.preview(ProgrammerState()))
    }

    @Test
    fun `いま編集している数`() {
        assertEquals(3L, ProgrammerEngine.currentOperand(typed("12 + 3")))
        assertEquals(0L, ProgrammerEngine.currentOperand(typed("12 +")))
        assertEquals(15L, ProgrammerEngine.currentOperand(press(Equals, from = typed("12 + 3")).state))
    }

    @Test
    fun `履歴にはANSを値に置き換えた式を残す`() {
        val result = press(Equals, from = typed("ANS AND F", Radix.HEX).copy(ans = 0xFF))
        assertEquals(Calculation("FF AND F", "F (HEX)", CalculatorMode.PROGRAMMER), result.completed)
        val negative = press(Equals, from = typed("ANS × 2").copy(ans = -5))
        assertEquals("(−5) × 2", negative.completed?.expression)
    }
}
