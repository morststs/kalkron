package click.e17.kalkron.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Stitch の rounded 定義。
 *
 * 「削り出しのアルミキーキャップ」を思わせる 4px の浅い角丸が基本で、
 * HUD パネルなど大きい面だけ 8px。丸ピル形状は使わない、というのが設計の意図。
 */
val CalculatorShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)
