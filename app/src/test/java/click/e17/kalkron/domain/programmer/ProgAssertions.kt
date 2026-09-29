package click.e17.kalkron.domain.programmer

import org.junit.Assert.assertEquals
import org.junit.Assert.fail

/** block が指定の種類の ProgException を投げることを確かめる */
fun assertProgError(expected: ProgError, block: () -> Unit) {
    try {
        block()
        fail("ProgException($expected) が投げられるはずだが、正常に終わった")
    } catch (e: ProgException) {
        assertEquals(expected, e.error)
    }
}
