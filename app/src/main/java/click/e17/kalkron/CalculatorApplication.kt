package click.e17.kalkron

import android.app.Application
import click.e17.kalkron.di.AppContainer
import click.e17.kalkron.di.DefaultAppContainer

/**
 * アプリのエントリポイント。
 *
 * AndroidManifest.xml の android:name で指定しており、
 * プロセス起動時に 1 度だけ生成される。ここで DI コンテナを作って保持する。
 */
class CalculatorApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
