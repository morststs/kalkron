package click.e17.kalkron.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 計算履歴テーブルへのアクセス方法を定義する DAO（Data Access Object）。
 *
 * interface を書くだけで、KSP が実装クラスをビルド時に生成してくれる。
 */
@Dao
interface CalculationHistoryDao {

    /**
     * 履歴を新しい順に取得する。
     *
     * 戻り値を Flow にしておくと、テーブルが更新されるたびに
     * Room が自動で新しいリストを流してくれる（購読しているだけで画面が最新になる）。
     */
    @Query("SELECT * FROM calculation_history ORDER BY created_at DESC LIMIT :limit")
    fun observeAll(limit: Int): Flow<List<CalculationHistoryEntity>>

    /** 履歴を 1 件追加する。suspend なので呼び出し側はコルーチン内で使う */
    @Insert
    suspend fun insert(entity: CalculationHistoryEntity)

    /** 履歴を全件削除する */
    @Query("DELETE FROM calculation_history")
    suspend fun deleteAll()
}
