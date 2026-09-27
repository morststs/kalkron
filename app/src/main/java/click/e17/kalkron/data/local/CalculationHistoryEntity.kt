package click.e17.kalkron.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import click.e17.kalkron.domain.CalculationRecord

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
)

/** Entity → ドメインモデルへの変換 */
fun CalculationHistoryEntity.toDomain(): CalculationRecord = CalculationRecord(
    id = id,
    expression = expression,
    result = result,
    createdAt = createdAt,
)

/** ドメインモデル → Entity への変換 */
fun CalculationRecord.toEntity(): CalculationHistoryEntity = CalculationHistoryEntity(
    id = id,
    expression = expression,
    result = result,
    createdAt = createdAt,
)
