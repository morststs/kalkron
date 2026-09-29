package click.e17.kalkron.data.repository

import click.e17.kalkron.data.local.CalculationHistoryDao
import click.e17.kalkron.data.local.toDomain
import click.e17.kalkron.data.local.toEntity
import click.e17.kalkron.domain.Calculation
import click.e17.kalkron.domain.CalculationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room を使った [HistoryRepository] の実装。
 *
 * ここが「DB の型（Entity）」と「アプリが扱う型（ドメインモデル）」の変換点になる。
 *
 * @param now 現在時刻を返す関数。引数にしておくとテストで時刻を固定できる
 */
class OfflineHistoryRepository(
    private val dao: CalculationHistoryDao,
    private val now: () -> Long = System::currentTimeMillis,
) : HistoryRepository {

    override fun observeHistory(limit: Int): Flow<List<CalculationRecord>> =
        dao.observeAll(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun save(calculation: Calculation) {
        dao.insert(
            CalculationRecord(
                expression = calculation.expression,
                result = calculation.result,
                createdAt = now(),
                mode = calculation.mode,
            ).toEntity()
        )
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
