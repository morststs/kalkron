package click.e17.kalkron.ui.programmer

import click.e17.kalkron.MainDispatcherRule
import click.e17.kalkron.domain.CalculatorMode
import click.e17.kalkron.domain.programmer.ProgSymbol
import click.e17.kalkron.domain.programmer.ProgrammerAction
import click.e17.kalkron.domain.programmer.Radix
import click.e17.kalkron.fake.FakeHistoryRepository
import click.e17.kalkron.ui.scientific.ResultKind
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * プログラマーモードの ViewModel のテスト。画面用の状態と履歴への保存を確かめる。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgrammerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeHistoryRepository(now = { 1_700_000_000_000L })
    private val viewModel = ProgrammerViewModel(repository)

    private fun type(vararg actions: ProgrammerAction) = actions.forEach(viewModel::onAction)

    @Test
    fun `初期状態はDECで32ビット`() {
        val state = viewModel.uiState.value
        assertEquals(listOf("0", "0", "0", "0"), state.radixLines.map { it.text })
        assertEquals(Radix.DEC, state.radixLines.single { it.selected }.radix)
        assertEquals(32, state.bits.size)
        assertTrue(state.bits.none { it })
        assertEquals((0..9).toSet(), state.enabledDigits)
        assertEquals(ResultKind.EMPTY, state.resultKind)
    }

    @Test
    fun `入力すると4つの基数とビットが更新される`() {
        type(ProgrammerAction.Digit(1), ProgrammerAction.Digit(0))
        val state = viewModel.uiState.value
        assertEquals(listOf("A", "10", "12", "1010"), state.radixLines.map { it.text })
        assertEquals(listOf(false, true, false, true), state.bits.take(4))
        assertEquals("10", state.result)
        assertEquals(ResultKind.PREVIEW, state.resultKind)
    }

    @Test
    fun `基数を切り替えると押せる数字キーが変わる`() {
        type(ProgrammerAction.SetRadix(Radix.BIN))
        assertEquals(setOf(0, 1), viewModel.uiState.value.enabledDigits)
        type(ProgrammerAction.SetRadix(Radix.HEX))
        assertEquals((0..15).toSet(), viewModel.uiState.value.enabledDigits)
    }

    @Test
    fun `イコールで確定しPROGRAMMERとして保存する`() = runTest {
        type(ProgrammerAction.Digit(2), ProgrammerAction.Insert(ProgSymbol.PLUS), ProgrammerAction.Digit(3), ProgrammerAction.Equals)

        assertEquals("5", viewModel.uiState.value.result)
        assertEquals(ResultKind.FINAL, viewModel.uiState.value.resultKind)
        val saved = repository.saved.single()
        assertEquals("2 + 3", saved.expression)
        assertEquals("5 (DEC)", saved.result)
        assertEquals(CalculatorMode.PROGRAMMER, saved.mode)
    }

    @Test
    fun `イコールの連打で履歴は1件だけ`() = runTest {
        type(ProgrammerAction.Digit(1), ProgrammerAction.Equals, ProgrammerAction.Equals)
        assertEquals(1, repository.saved.size)
    }

    @Test
    fun `エラーは文言を表示し保存しない`() = runTest {
        type(ProgrammerAction.Digit(1), ProgrammerAction.Insert(ProgSymbol.DIVIDE), ProgrammerAction.Digit(0), ProgrammerAction.Equals)
        assertEquals("0 で割れません", viewModel.uiState.value.result)
        assertEquals(ResultKind.ERROR, viewModel.uiState.value.resultKind)
        assertTrue(repository.saved.isEmpty())
    }
}
