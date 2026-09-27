package click.e17.kalkron.data.repository

import click.e17.kalkron.domain.Calculation
import click.e17.kalkron.domain.CalculationRecord
import kotlinx.coroutines.flow.Flow

/**
 * 計算履歴の取得・保存の窓口。
 *
 * ViewModel はこの interface だけを知っていればよく、
 * 「Room なのか、メモリなのか、サーバーなのか」を意識しない。
 * テストでは本物の代わりに Fake 実装を差し込める（FakeHistoryRepository 参照）。
 */
interface HistoryRepository {

    /** 履歴を新しい順に購読する */
    fun observeHistory(limit: Int = DEFAULT_LIMIT): Flow<List<CalculationRecord>>

    /** 完了した計算を 1 件保存する */
    suspend fun save(calculation: Calculation)

    /** 履歴を全件削除する */
    suspend fun clear()

    companion object {
        const val DEFAULT_LIMIT = 100
    }
}
