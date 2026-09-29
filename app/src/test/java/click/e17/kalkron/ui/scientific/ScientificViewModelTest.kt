package click.e17.kalkron.ui.scientific

import click.e17.kalkron.MainDispatcherRule
import click.e17.kalkron.domain.CalculatorMode
import click.e17.kalkron.domain.scientific.ScientificAction
import click.e17.kalkron.domain.scientific.Symbol
import click.e17.kalkron.fake.FakeHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * 関数電卓の ViewModel のテスト。計算の中身は Engine のテストで確認済みなので、
 * 画面用の状態と履歴への保存を確かめる。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScientificViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeHistoryRepository(now = { 1_700_000_000_000L })
    private val viewModel = ScientificViewModel(repository)

    private fun type(vararg actions: ScientificAction) = actions.forEach(viewModel::onAction)

    @Test
    fun `初期状態は何も表示しない`() {
        assertEquals("", viewModel.uiState.value.expression)
        assertEquals(ResultKind.EMPTY, viewModel.uiState.value.resultKind)
    }

    @Test
    fun `入力中は結果を先出しし3桁区切りを入れる`() {
        type(
            ScientificAction.Digit(1), ScientificAction.Digit(2), ScientificAction.Digit(3), ScientificAction.Digit(4),
            ScientificAction.Insert(Symbol.TIMES), ScientificAction.Digit(1), ScientificAction.Digit(0),
        )
        val state = viewModel.uiState.value
        assertEquals("1,234 × 10", state.expression)
        assertEquals("12,340", state.result)
        assertEquals(ResultKind.PREVIEW, state.resultKind)
    }

    @Test
    fun `イコールで確定しSCIENTIFICとして保存する`() = runTest {
        type(ScientificAction.Digit(2), ScientificAction.Insert(Symbol.PLUS), ScientificAction.Digit(3), ScientificAction.Equals)

        assertEquals("5", viewModel.uiState.value.result)
        assertEquals(ResultKind.FINAL, viewModel.uiState.value.resultKind)
        val saved = repository.saved.single()
        assertEquals("2 + 3", saved.expression)
        assertEquals("5", saved.result)
        assertEquals(CalculatorMode.SCIENTIFIC, saved.mode)
    }

    @Test
    fun `イコールの連打で履歴は1件だけ`() = runTest {
        type(ScientificAction.Digit(1), ScientificAction.Equals, ScientificAction.Equals, ScientificAction.Equals)
        assertEquals(1, repository.saved.size)
    }

    @Test
    fun `エラーは文言を表示し保存しない`() = runTest {
        type(ScientificAction.Digit(1), ScientificAction.Insert(Symbol.DIVIDE), ScientificAction.Digit(0), ScientificAction.Equals)

        assertEquals("0 で割れません", viewModel.uiState.value.result)
        assertEquals(ResultKind.ERROR, viewModel.uiState.value.resultKind)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `2ndとメモリの状態を画面に渡す`() {
        type(ScientificAction.ToggleSecond)
        assertTrue(viewModel.uiState.value.secondActive)

        type(ScientificAction.ToggleSecond, ScientificAction.Digit(5), ScientificAction.Equals, ScientificAction.Store)
        assertTrue(viewModel.uiState.value.hasMemory)
    }
}
