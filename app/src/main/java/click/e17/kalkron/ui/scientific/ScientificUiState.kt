package click.e17.kalkron.ui.scientific

import click.e17.kalkron.domain.scientific.AngleUnit
import click.e17.kalkron.domain.scientific.ScientificEngine
import click.e17.kalkron.domain.scientific.ScientificFormat
import click.e17.kalkron.domain.scientific.ScientificState
import click.e17.kalkron.ui.format.groupDigits
import click.e17.kalkron.ui.format.groupExpression

/** 結果の行に何を出しているか。見た目（色・大きさ）を切り替えるのに使う */
enum class ResultKind {
    /** 何も出さない */
    EMPTY,

    /** 入力中の先出し（薄く表示） */
    PREVIEW,

    /** = で確定した結果 */
    FINAL,

    /** エラーの文言 */
    ERROR,
}

/**
 * 関数電卓の画面が表示に必要とする情報だけを持つ状態。
 */
data class ScientificUiState(
    val expression: String = "",
    val result: String = "",
    val resultKind: ResultKind = ResultKind.EMPTY,
    val angleUnit: AngleUnit = AngleUnit.RAD,
    val secondActive: Boolean = false,
    val hyperbolicActive: Boolean = false,
    val hasMemory: Boolean = false,
)

/** ドメインの状態 → 画面用の状態。3桁区切りはここで入れる */
fun ScientificState.toUiState(): ScientificUiState {
    val (text, kind) = when {
        error != null -> error.message to ResultKind.ERROR
        result != null -> groupDigits(ScientificFormat.number(result)) to ResultKind.FINAL
        else -> ScientificEngine.preview(this)
            ?.let { groupDigits(ScientificFormat.number(it)) to ResultKind.PREVIEW }
            ?: ("" to ResultKind.EMPTY)
    }
    return ScientificUiState(
        expression = groupExpression(ScientificFormat.expression(tokens)),
        result = text,
        resultKind = kind,
        angleUnit = angleUnit,
        secondActive = second,
        hyperbolicActive = hyperbolic,
        hasMemory = memory != null,
    )
}
