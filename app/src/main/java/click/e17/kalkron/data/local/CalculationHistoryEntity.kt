package click.e17.kalkron.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import click.e17.kalkron.domain.CalculationRecord
import click.e17.kalkron.domain.CalculatorMode

/**
 * 計算履歴テーブルの 1 行を表す Room の Entity。
 *
 * @Entity を付けたクラスがそのまま SQLite のテーブル定義になる。
 */
@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    // autoGenerate = true にすると SQLite 側が連番を振ってくれる
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** 式（例: "12 + 3"） */
    @ColumnInfo(name = "expression")
    val expression: String,

    /** 結果（例: "15"） */
    @ColumnInfo(name = "result")
    val result: String,

    /** 保存時刻（エポックミリ秒） */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    /**
     * 計算モードの名前（CalculatorMode.name）。
     * 既定値はマイグレーションの SQL と一致させる必要がある（Room が起動時に照合する）
     */
    @ColumnInfo(name = "mode", defaultValue = "'STANDARD'")
    val mode: String = CalculatorMode.STANDARD.name,
)

/** Entity → ドメインモデルへの変換。未知のモード名は STANDARD として扱う */
fun CalculationHistoryEntity.toDomain(): CalculationRecord = CalculationRecord(
    id = id,
    expression = expression,
    result = result,
    createdAt = createdAt,
    mode = CalculatorMode.entries.firstOrNull { it.name == mode } ?: CalculatorMode.STANDARD,
)

/** ドメインモデル → Entity への変換 */
fun CalculationRecord.toEntity(): CalculationHistoryEntity = CalculationHistoryEntity(
    id = id,
    expression = expression,
    result = result,
    createdAt = createdAt,
    mode = mode.name,
)
