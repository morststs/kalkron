package click.e17.kalkron.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Stitch のデザインシステムをそのまま写した配色。
 *
 * このデザインはネオンの発光を暗い面の上で成立させる前提のため、
 * ダーク固定とし、ライトテーマとダイナミックカラー（壁紙連動）は使わない。
 */
private val ObsidianColorScheme = darkColorScheme(
    primary = CyanBright,
    onPrimary = CyanDeep,
    primaryContainer = ElectricCyan,
    onPrimaryContainer = CyanContainerOn,
    inversePrimary = CyanContainerOn,

    secondary = AmberSoft,
    onSecondary = AmberDeep,
    secondaryContainer = AmberTelemetry,
    onSecondaryContainer = AmberContainerOn,

    tertiary = VioletSoft,
    onTertiary = VioletDeep,
    tertiaryContainer = VioletContainer,
    onTertiaryContainer = IonViolet,

    // 地をキーより暗くしないとキーが沈んで見えるため、canvas を使う
    background = ObsidianCanvas,
    onBackground = OnSurfaceBright,

    surface = ObsidianCanvas,
    onSurface = OnSurfaceBright,
    surfaceVariant = ObsidianSurfaceHighest,
    onSurfaceVariant = OnSurfaceMuted,
    surfaceTint = CyanDim,
    surfaceBright = ObsidianSurfaceBright,
    surfaceDim = ObsidianSurface,
    surfaceContainerLowest = ObsidianSurfaceLowest,
    surfaceContainerLow = ObsidianSurfaceLow,
    surfaceContainer = ObsidianSurfaceContainer,
    surfaceContainerHigh = ObsidianSurfaceHigh,
    surfaceContainerHighest = ObsidianSurfaceHighest,

    outline = Outline,
    outlineVariant = OutlineVariant,

    error = ErrorRed,
    onError = ErrorDeep,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
)

@Composable
fun CalculatorTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = Typography,
        shapes = CalculatorShapes,
        content = content,
    )
}
