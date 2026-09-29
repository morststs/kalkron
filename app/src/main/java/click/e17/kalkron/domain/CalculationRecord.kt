package click.e17.kalkron.domain

/**
 * 保存済みの計算履歴 1 件を表すドメインモデル。
 *
 * DB の都合（テーブル名やカラム名）を持ち込まないように、
 * Room の Entity とは別の型として定義している。
 * こうしておくと「DB を Room から別の仕組みに差し替える」といった変更が
 * UI 層に波及しない。
 */
data class CalculationRecord(
    val id: Long = 0L,
    val expression: String,
    val result: String,
    val createdAt: Long,
    val mode: CalculatorMode = CalculatorMode.STANDARD,
)
