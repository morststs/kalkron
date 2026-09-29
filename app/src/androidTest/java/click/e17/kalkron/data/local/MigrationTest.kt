package click.e17.kalkron.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * DB マイグレーションのテスト。端末またはエミュレータ上で実行する（計装テスト）。
 * v1 のスキーマで DB を作り、行を入れてから v2 に上げ、行が残ることを確かめる。
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CalculatorDatabase::class.java,
    )

    @Test
    fun v1の履歴はv2でSTANDARDとして残る() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL(
                "INSERT INTO calculation_history (expression, result, created_at) " +
                    "VALUES ('12 + 3', '15', 1000)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2)

        db.query("SELECT expression, result, mode FROM calculation_history").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("12 + 3", cursor.getString(0))
            assertEquals("15", cursor.getString(1))
            assertEquals("STANDARD", cursor.getString(2))
        }
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
