package click.e17.kalkron.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: 履歴に計算モードの列を足す。
 *
 * 既存の行は標準モードで計算したものなので、既定値 'STANDARD' で埋める。
 * 既定値は CalculationHistoryEntity の @ColumnInfo(defaultValue) と一致させること。
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE calculation_history ADD COLUMN mode TEXT NOT NULL DEFAULT 'STANDARD'"
        )
    }
}
