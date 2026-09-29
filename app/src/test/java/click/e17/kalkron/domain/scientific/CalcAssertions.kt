package click.e17.kalkron.domain.scientific

import org.junit.Assert.assertEquals
import org.junit.Assert.fail

/** block が指定の種類の CalcException を投げることを確かめる */
fun assertCalcError(expected: CalcError, block: () -> Unit) {
    try {
        block()
        fail("CalcException($expected) が投げられるはずだが、正常に終わった")
    } catch (e: CalcException) {
        assertEquals(expected, e.error)
    }
}
