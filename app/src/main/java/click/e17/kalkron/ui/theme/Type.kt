package click.e17.kalkron.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import click.e17.kalkron.R

/**
 * Stitch のデザインシステムが指定する 3 書体。
 *
 * いずれも可変フォント（1 ファイルに太さの軸 wght を持つ）なので、
 * FontVariation でウェイトを指定しないと既定の太さのまま表示されてしまう。
 * 例えば Space Grotesk の既定は 300（Light）なので、見出しの 600 は指定が必須。
 */
private fun variableFont(resId: Int, weight: Int) = Font(
    resId = resId,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** 数字・キーキャップ・ログ用の等幅書体 */
val JetBrainsMono = FontFamily(
    variableFont(R.font.jetbrains_mono, 400),
    variableFont(R.font.jetbrains_mono, 500),
    variableFont(R.font.jetbrains_mono, 600),
)

/** 見出し・モード表示用の幾何学的な書体 */
val SpaceGrotesk = FontFamily(
    variableFont(R.font.space_grotesk, 500),
    variableFont(R.font.space_grotesk, 600),
    variableFont(R.font.space_grotesk, 700),
)

/** 本文・説明用の書体 */
val Geist = FontFamily(
    variableFont(R.font.geist, 400),
    variableFont(R.font.geist, 500),
)

/**
 * Stitch の typography 定義を Material3 の役割に対応させたもの。
 * 数値は元のデザイン定義（px / em）をそのまま sp / em に読み替えている。
 */
val Typography = Typography(
    // display-lg: 計算結果の大きな数字
    displayLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight(500),
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.04).em,
    ),
    // display-lg-mobile: 画面が狭いときの数字
    displayMedium = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight(500),
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.03).em,
    ),
    // headline-lg / headline-md: 画面タイトル
    headlineLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight(600),
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight(600),
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.01).em,
    ),
    // 履歴の結果表示など、少し大きめの等幅
    headlineSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight(500),
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.02).em,
    ),
    // body-lg / body-md: 説明文
    bodyLarge = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight(400),
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight(400),
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // label-*: キーキャップ、チップ、テレメトリ表示。字間を広めに取るのが特徴
    labelLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.06.em,
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight(600),
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.12.em,
    ),
)
