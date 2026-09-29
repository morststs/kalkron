package click.e17.kalkron.domain.scientific

import kotlin.math.PI

/**
 * 角度の単位。三角関数の引数と、逆三角関数の結果にだけ効く。
 * RAD: ラジアン（一周 2π）、DEG: 度（一周 360）、GRAD: グラード（一周 400）
 */
enum class AngleUnit(val label: String) {
    RAD("RAD"),
    DEG("DEG"),
    GRAD("GRAD"),
    ;

    fun toRadians(value: Double): Double = when (this) {
        RAD -> value
        DEG -> value * PI / 180
        GRAD -> value * PI / 200
    }

    fun fromRadians(radians: Double): Double = when (this) {
        RAD -> radians
        DEG -> radians * 180 / PI
        GRAD -> radians * 200 / PI
    }

    /**
     * 直角の奇数倍（tan が定義されない角度）かどうか。
     * ラジアンでは π/2 を正確に表せないため判定しない。
     */
    fun isOddRightAngle(value: Double): Boolean = when (this) {
        RAD -> false
        DEG -> (value - 90.0).mod(180.0) == 0.0
        GRAD -> (value - 100.0).mod(200.0) == 0.0
    }
}
