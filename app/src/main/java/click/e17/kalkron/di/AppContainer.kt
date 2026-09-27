package click.e17.kalkron.di

import android.content.Context
import click.e17.kalkron.data.local.CalculatorDatabase
import click.e17.kalkron.data.repository.HistoryRepository
import click.e17.kalkron.data.repository.OfflineHistoryRepository

/**
 * アプリ全体で共有する依存オブジェクトの置き場（手動 DI コンテナ）。
 *
 * Hilt などの DI ライブラリを使うとこの組み立てを自動化できるが、
 * この規模なら「誰が誰を生成しているか」が一目で分かるこの方式で足りる。
 */
interface AppContainer {
    val historyRepository: HistoryRepository
}

/**
 * 本番用の実装。Room の DAO から Repository を組み立てる。
 * by lazy にしているので、実際に使われるまでインスタンスは作られない。
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val historyRepository: HistoryRepository by lazy {
        OfflineHistoryRepository(
            dao = CalculatorDatabase.getInstance(context).calculationHistoryDao()
        )
    }
}
