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
    version = 1,
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
                    // スキーマ変更時はテーブルを作り直す設定。計算履歴は失っても
                    // 再現可能な情報なので、Migration は定義していない。
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
