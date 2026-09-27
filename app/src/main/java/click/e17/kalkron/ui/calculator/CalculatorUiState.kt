package click.e17.kalkron.ui.calculator

import click.e17.kalkron.domain.CalculatorState

/**
 * 電卓画面が表示に必要とする情報だけを持つ状態クラス。
 *
 * ドメインの [CalculatorState] をそのまま UI に渡さないのがポイント。
 * UI は「何を表示するか」だけを知り、計算の途中経過（accumulator など）は知らない。
 */
data class CalculatorUiState(
    /** メインの大きな数字 */
    val display: String = "0",
    /** 上部に薄く出る途中式（例: "12 +"） */
    val expression: String = "",
    /** エラー表示中かどうか。UI はこれを見て文字色を変える */
    val isError: Boolean = false,
    /** 入力待ちの演算子の記号。無ければ null（ステータス帯の表示に使う） */
    val pendingOperator: String? = null,
    /** 現在入力中の桁数。入力上限との対比をステータス帯に出す */
    val digitCount: Int = 1,
)

/** ドメインの状態 → 画面用の状態への変換 */
fun CalculatorState.toUiState(): CalculatorUiState = CalculatorUiState(
    display = input,
    expression = expression,
    isError = isError,
    pendingOperator = pendingOperator?.symbol,
    digitCount = if (isError) 0 else input.count { it.isDigit() },
)
