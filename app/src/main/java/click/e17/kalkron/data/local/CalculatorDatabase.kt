package click.e17.kalkron.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * アプリのローカル DB 本体。
 *
 * entities にテーブル（Entity）を列挙し、DAO を取得する抽象メソッドを定義する。
 * 実装クラスは KSP がビルド時に生成する。
 */
@Database(
    entities = [CalculationHistoryEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class CalculatorDatabase : RoomDatabase() {

    abstract fun calculationHistoryDao(): CalculationHistoryDao

    companion object {
        // DB インスタンスはアプリ全体で 1 つだけにする（複数作ると無駄かつ不整合の元）
        @Volatile
        private var instance: CalculatorDatabase? = null

        fun getInstance(context: Context): CalculatorDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CalculatorDatabase::class.java,
                    "calculator.db",
                )
                    // スキーマを変えるときは Migration を足して、既存の履歴を残す
                    .addMigrations(MIGRATION_1_2)
                    // アプリを古いバージョンに戻した場合だけは作り直す
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
