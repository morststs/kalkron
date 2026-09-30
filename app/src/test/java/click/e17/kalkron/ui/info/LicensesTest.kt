package click.e17.kalkron.ui.info

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 情報画面に出すライセンス一覧のテスト。
 * 一覧が参照するライセンス文のファイルが、実際に assets に入っていることを確かめる
 * （ファイル名の書き間違いがあると、画面で全文を開いたときに初めて気づくことになるため）。
 */
class LicensesTest {

    /** 単体テストの作業ディレクトリは app/ なので、そこからの相対パスで assets を探す */
    private val assets = File("src/main/assets")

    @Test
    fun `一覧が参照するライセンス文はすべてassetsにある`() {
        LICENSES.forEach { entry ->
            val file = File(assets, entry.licenseAsset)
            assertTrue("${entry.name}: ${file.path} がありません", file.isFile && file.length() > 0)
        }
    }

    @Test
    fun `同梱フォント3つとApache License のライブラリを載せている`() {
        val names = LICENSES.map { it.name }
        listOf("JetBrains Mono", "Space Grotesk", "Geist").forEach { font ->
            assertTrue("$font が一覧にありません", font in names)
        }
        assertTrue(LICENSES.any { it.licenseName == "Apache License 2.0" })
    }

    @Test
    fun `先頭はKalkron自身のMITライセンスで全文はリポジトリのLICENSEと同じ`() {
        val first = LICENSES.first()
        assertEquals("Kalkron", first.name)
        assertEquals("MIT License", first.licenseName)
        // assets の写しがリポジトリ直下の LICENSE（作業ディレクトリ app/ の1つ上）とずれていないこと
        assertEquals(File("../LICENSE").readText(), File(assets, first.licenseAsset).readText())
    }
}
