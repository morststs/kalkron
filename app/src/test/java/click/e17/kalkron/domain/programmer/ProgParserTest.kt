package click.e17.kalkron.domain.programmer

import click.e17.kalkron.domain.programmer.ProgBinaryOp.ADD
import click.e17.kalkron.domain.programmer.ProgBinaryOp.AND
import click.e17.kalkron.domain.programmer.ProgBinaryOp.MULTIPLY
import click.e17.kalkron.domain.programmer.ProgBinaryOp.OR
import click.e17.kalkron.domain.programmer.ProgBinaryOp.SHL
import click.e17.kalkron.domain.programmer.ProgBinaryOp.XOR
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 構文解析のテスト。C 言語と同じ優先順位が構文木の形に表れることを確かめる。
 */
class ProgParserTest {

    private fun parse(source: String): ProgExpr = ProgParser.parse(ProgLexer.tokenize(source))
    private fun n(v: Long) = ProgExpr.Num(v)
    private fun bin(op: ProgBinaryOp, l: ProgExpr, r: ProgExpr) = ProgExpr.Binary(op, l, r)

    @Test
    fun `掛け算は足し算より先`() {
        assertEquals(bin(ADD, n(1), bin(MULTIPLY, n(2), n(3))), parse("1 + 2 × 3"))
    }

    @Test
    fun `足し算はシフトより先`() {
        assertEquals(bin(SHL, n(1), bin(ADD, n(2), n(3))), parse("1 << 2 + 3"))
    }

    @Test
    fun `ANDはXORより先でXORはORより先`() {
        assertEquals(bin(OR, n(1), bin(AND, n(2), n(3))), parse("1 OR 2 AND 3"))
        assertEquals(bin(OR, bin(XOR, n(1), n(2)), n(3)), parse("1 XOR 2 OR 3"))
        assertEquals(bin(XOR, n(1), bin(AND, n(2), n(3))), parse("1 XOR 2 AND 3"))
    }

    @Test
    fun `シフトはANDより先`() {
        assertEquals(bin(AND, n(1), bin(SHL, n(2), n(3))), parse("1 AND 2 << 3"))
    }

    @Test
    fun `同じ強さの演算子は左から`() {
        assertEquals(bin(ProgBinaryOp.SUBTRACT, bin(ProgBinaryOp.SUBTRACT, n(8), n(3)), n(2)), parse("8 − 3 − 2"))
    }

    @Test
    fun `単項のNOTとマイナスは最も強い`() {
        assertEquals(bin(AND, ProgExpr.Unary(ProgUnaryOp.NOT, n(1)), n(2)), parse("NOT 1 AND 2"))
        assertEquals(bin(MULTIPLY, ProgExpr.Unary(ProgUnaryOp.NEGATE, n(2)), n(3)), parse("−2 × 3"))
        assertEquals(ProgExpr.Unary(ProgUnaryOp.NOT, ProgExpr.Unary(ProgUnaryOp.NEGATE, n(1))), parse("NOT −1"))
    }

    @Test
    fun `括弧とANS`() {
        assertEquals(bin(AND, bin(OR, n(1), n(2)), ProgExpr.Ans), parse("(1 OR 2) AND ANS"))
    }

    @Test
    fun `正しくない式はエラー`() {
        listOf("", "1 +", "(1", "1)", "AND 1", "1 2", "()").forEach { source ->
            assertProgError(ProgError.SYNTAX) { parse(source) }
        }
    }
}
