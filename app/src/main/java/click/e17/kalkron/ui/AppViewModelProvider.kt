package click.e17.kalkron.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import click.e17.kalkron.CalculatorApplication
import click.e17.kalkron.ui.calculator.CalculatorViewModel
import click.e17.kalkron.ui.history.HistoryViewModel
import click.e17.kalkron.ui.programmer.ProgrammerViewModel
import click.e17.kalkron.ui.scientific.ScientificViewModel

/**
 * ViewModel の生成方法をまとめた Factory。
 *
 * ViewModel はコンストラクタ引数（ここでは Repository）を取るため、
 * 既定の生成方法では作れない。そこで Factory を用意して
 * 「Application が持つ DI コンテナから Repository を取り出して渡す」処理を書く。
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            CalculatorViewModel(calculatorApplication().container.historyRepository)
        }
        initializer {
            HistoryViewModel(calculatorApplication().container.historyRepository)
        }
        initializer {
            ScientificViewModel(calculatorApplication().container.historyRepository)
        }
        initializer {
            ProgrammerViewModel(calculatorApplication().container.historyRepository)
        }
    }
}

/**
 * ViewModel 生成時に渡される CreationExtras から Application を取り出すヘルパー。
 */
private fun CreationExtras.calculatorApplication(): CalculatorApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CalculatorApplication
