package click.e17.kalkron.domain.programmer

import click.e17.kalkron.domain.Calculation
import click.e17.kalkron.domain.CalculatorMode
import java.math.BigInteger

/**
 * プログラマーモードの内部状態。
 *
 * @param tokens 入力中の式。数値トークンは語長で正規化済みの値を持つ
 * @param result = で確定した結果。確定直後だけ値が入る
 * @param error = で起きたエラー。エラー表示中だけ値が入る
 */
data class ProgrammerState(
    val tokens: List<ProgToken> = emptyList(),
    val radix: Radix = Radix.DEC,
    val wordSize: WordSize = WordSize.DWORD,
    val signed: Boolean = true,
    val ans: Long = 0L,
    val result: Long? = null,
    val error: ProgError? = null,
)

/** 操作を適用した結果。completed は = で確定した計算（履歴に保存する） */
data class ProgrammerResult(
    val state: ProgrammerState,
    val completed: Calculation? = null,
)

/**
 * プログラマーモードの入力の編集と評価。
 * STD・SCI と同じく「状態 + 操作 → 新しい状態」の純粋関数として書く。
 */
object ProgrammerEngine {

    /** = の直後に押すと、ANS の続きとして入力する二項演算子 */
    private val CONTINUATIONS = setOf(
        ProgSymbol.PLUS, ProgSymbol.MINUS, ProgSymbol.TIMES, ProgSymbol.DIVIDE, ProgSymbol.MOD,
        ProgSymbol.AND, ProgSymbol.OR, ProgSymbol.XOR, ProgSymbol.NAND, ProgSymbol.NOR,
        ProgSymbol.SHL, ProgSymbol.SHR, ProgSymbol.ROL, ProgSymbol.ROR,
    )

    fun reduce(state: ProgrammerState, action: ProgrammerAction): ProgrammerResult = when (action) {
        is ProgrammerAction.Digit -> ProgrammerResult(appendDigit(state, action.value))
        is ProgrammerAction.Insert -> ProgrammerResult(insert(state, action.symbol))
        ProgrammerAction.Complement -> ProgrammerResult(complement(state))
        is ProgrammerAction.FlipBit -> ProgrammerResult(flipBit(state, action.index))
        is ProgrammerAction.SetRadix -> ProgrammerResult(state.copy(radix = action.radix))
        is ProgrammerAction.SetWordSize -> ProgrammerResult(renormalize(state.copy(wordSize = action.wordSize)))
        ProgrammerAction.ToggleSigned -> ProgrammerResult(renormalize(state.copy(signed = !state.signed)))
        ProgrammerAction.Delete -> ProgrammerResult(delete(state))
        ProgrammerAction.Clear -> ProgrammerResult(state.copy(tokens = emptyList(), result = null, error = null))
        ProgrammerAction.Equals -> evaluate(state)
    }

    /** 入力途中の式を評価してみる。式として成り立たない・エラーになるときは null */
    fun preview(state: ProgrammerState): Long? {
        if (state.tokens.isEmpty() || state.result != null || state.error != null) return null
        return try {
            ProgEvaluator.evaluate(ProgParser.parse(state.tokens), state.context())
        } catch (e: ProgException) {
            null
        }
    }

    /**
     * いま編集している数。表示部の4行とビット反転グリッドに出す。
     * = の直後はその結果、式が数で終わっていればその数、それ以外は 0。
     */
    fun currentOperand(state: ProgrammerState): Long =
        state.result ?: (state.tokens.lastOrNull() as? ProgToken.Num)?.value ?: 0L

    /**
     * 入力の前処理。
     * = の直後なら、二項演算子は ANS の続きに、それ以外は新しい式にする。エラー表示中ならエラーを消す。
     */
    private fun prepare(state: ProgrammerState, continues: Boolean): ProgrammerState = when {
        state.result != null -> state.copy(
            tokens = if (continues) listOf(ProgToken.Sym(ProgSymbol.ANS)) else emptyList(),
            result = null,
        )
        state.error != null -> state.copy(error = null)
        else -> state
    }

    private fun insert(state: ProgrammerState, symbol: ProgSymbol): ProgrammerState {
        val prepared = prepare(state, continues = symbol in CONTINUATIONS)
        return prepared.copy(tokens = prepared.tokens + ProgToken.Sym(symbol))
    }

    private fun appendDigit(state: ProgrammerState, digit: Int): ProgrammerState {
        if (!state.radix.accepts(digit)) return state
        val prepared = prepare(state, continues = false)
        val last = prepared.tokens.lastOrNull() as? ProgToken.Num
            ?: return prepared.copy(tokens = prepared.tokens + ProgToken.Num(prepared.normalize(digit.toLong())))
        val next = appendTo(last.value, digit, prepared) ?: return prepared
        return prepared.copy(tokens = prepared.tokens.dropLast(1) + ProgToken.Num(next))
    }

    /** 数に桁を1つ足す。入力できる上限を超えるなら null */
    private fun appendTo(value: Long, digit: Int, state: ProgrammerState): Long? {
        val size = state.wordSize
        return if (state.radix == Radix.DEC && state.signed) {
            // DEC の符号ありは、符号ありの範囲で絶対値に桁を足す（−5 に 3 → −53）
            val current = BigInteger.valueOf(value)
            val candidate = if (value >= 0) {
                current * BigInteger.TEN + BigInteger.valueOf(digit.toLong())
            } else {
                current * BigInteger.TEN - BigInteger.valueOf(digit.toLong())
            }
            val min = BigInteger.valueOf(size.minSigned())
            val max = BigInteger.valueOf(size.maxSigned())
            if (candidate < min || candidate > max) null else candidate.toLong()
        } else {
            // それ以外は、ビットの並びとして語長いっぱいまで入力できる
            val pattern = BigInteger(java.lang.Long.toUnsignedString(size.pattern(value)))
            val candidate = pattern * BigInteger.valueOf(state.radix.base.toLong()) + BigInteger.valueOf(digit.toLong())
            val maxUnsigned = BigInteger.ONE.shiftLeft(size.bits) - BigInteger.ONE
            if (candidate > maxUnsigned) null else state.normalize(candidate.toLong())
        }
    }

    /** 2's: いま編集している数を符号反転する */
    private fun complement(state: ProgrammerState): ProgrammerState {
        state.result?.let { result ->
            return state.copy(tokens = listOf(ProgToken.Num(state.normalize(-result))), result = null)
        }
        val cleared = state.copy(error = null)
        val last = cleared.tokens.lastOrNull() as? ProgToken.Num ?: return state
        return cleared.copy(tokens = cleared.tokens.dropLast(1) + ProgToken.Num(cleared.normalize(-last.value)))
    }

    /** ビット反転グリッドのマスをタップしたときの処理 */
    private fun flipBit(state: ProgrammerState, index: Int): ProgrammerState {
        if (index !in 0 until state.wordSize.bits) return state
        val bit = 1L shl index
        state.result?.let { result ->
            return state.copy(tokens = listOf(ProgToken.Num(state.normalize(result xor bit))), result = null)
        }
        val cleared = state.copy(error = null)
        val last = cleared.tokens.lastOrNull() as? ProgToken.Num
            ?: return cleared.copy(tokens = cleared.tokens + ProgToken.Num(cleared.normalize(bit)))
        return cleared.copy(tokens = cleared.tokens.dropLast(1) + ProgToken.Num(cleared.normalize(last.value xor bit)))
    }

    /** ⌫: 数値は今の基数で1桁、それ以外のトークンは丸ごと消す */
    private fun delete(state: ProgrammerState): ProgrammerState {
        val cleared = state.copy(result = null, error = null)
        val last = cleared.tokens.lastOrNull() ?: return cleared
        if (last !is ProgToken.Num) return cleared.copy(tokens = cleared.tokens.dropLast(1))

        val digits = cleared.radix.plain(last.value, cleared.wordSize, cleared.signed).removePrefix("-")
        if (digits.length <= 1) return cleared.copy(tokens = cleared.tokens.dropLast(1))

        val shortened = if (cleared.radix == Radix.DEC && cleared.signed) {
            last.value / 10
        } else {
            cleared.normalize(java.lang.Long.divideUnsigned(cleared.wordSize.pattern(last.value), cleared.radix.base.toLong()))
        }
        return cleared.copy(tokens = cleared.tokens.dropLast(1) + ProgToken.Num(shortened))
    }

    /** 語長・符号の変更後に、式の中の数・ANS・結果を新しい設定で正規化し直す */
    private fun renormalize(state: ProgrammerState): ProgrammerState = state.copy(
        tokens = state.tokens.map { if (it is ProgToken.Num) ProgToken.Num(state.normalize(it.value)) else it },
        ans = state.normalize(state.ans),
        result = state.result?.let { state.normalize(it) },
    )

    private fun evaluate(state: ProgrammerState): ProgrammerResult {
        // 空の式、または確定済み（= の連打）なら何もしない
        if (state.tokens.isEmpty() || state.result != null) return ProgrammerResult(state)
        return try {
            val value = ProgEvaluator.evaluate(ProgParser.parse(state.tokens), state.context())
            val calculation = Calculation(
                expression = ProgFormat.expression(state.tokens, state.radix, state.wordSize, state.signed),
                result = ProgFormat.result(value, state.radix, state.wordSize, state.signed),
                mode = CalculatorMode.PROGRAMMER,
            )
            ProgrammerResult(state.copy(result = value, ans = value, error = null), completed = calculation)
        } catch (e: ProgException) {
            ProgrammerResult(state.copy(error = e.error))
        }
    }

    private fun ProgrammerState.context() = ProgContext(wordSize, signed, ans)

    private fun ProgrammerState.normalize(value: Long) = wordSize.normalize(value, signed)
}
