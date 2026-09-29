package click.e17.kalkron.ui.programmer

import click.e17.kalkron.domain.programmer.ProgFormat
import click.e17.kalkron.domain.programmer.ProgrammerEngine
import click.e17.kalkron.domain.programmer.ProgrammerState
import click.e17.kalkron.domain.programmer.Radix
import click.e17.kalkron.domain.programmer.WordSize
import click.e17.kalkron.ui.scientific.ResultKind

/** 表示部の下段に並べる、基数ごとの1行 */
data class RadixLine(
    val radix: Radix,
    val text: String,
    val selected: Boolean,
)

/**
 * プログラマーモードの画面が表示に必要とする情報だけを持つ状態。
 *
 * @param bits いま編集している数のビット。添字がビット番号（0 が最下位）、要素数は語長
 * @param enabledDigits 今の基数で押せる数字キー（0〜15）
 */
data class ProgrammerUiState(
    val expression: String = "",
    val result: String = "",
    val resultKind: ResultKind = ResultKind.EMPTY,
    val radixLines: List<RadixLine> = emptyList(),
    val bits: List<Boolean> = List(32) { false },
    val radix: Radix = Radix.DEC,
    val wordSize: WordSize = WordSize.DWORD,
    val signed: Boolean = true,
    val enabledDigits: Set<Int> = (0..9).toSet(),
)

/** ドメインの状態 → 画面用の状態 */
fun ProgrammerState.toUiState(): ProgrammerUiState {
    val (text, kind) = when {
        error != null -> error.message to ResultKind.ERROR
        result != null -> radix.grouped(result, wordSize, signed) to ResultKind.FINAL
        else -> ProgrammerEngine.preview(this)
            ?.let { radix.grouped(it, wordSize, signed) to ResultKind.PREVIEW }
            ?: ("" to ResultKind.EMPTY)
    }
    val operand = ProgrammerEngine.currentOperand(this)
    return ProgrammerUiState(
        expression = ProgFormat.expression(tokens, radix, wordSize, signed),
        result = text,
        resultKind = kind,
        radixLines = Radix.entries.map { RadixLine(it, it.grouped(operand, wordSize, signed), it == radix) },
        bits = List(wordSize.bits) { index -> (operand ushr index) and 1L == 1L },
        radix = radix,
        wordSize = wordSize,
        signed = signed,
        enabledDigits = (0 until radix.base).toSet(),
    )
}
