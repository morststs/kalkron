package click.e17.kalkron.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Stitch のデザインシステム "Obsidian Cyber-Precision" の色トークン。
 *
 * 計測機器のような黒曜石の面に、ネオンのアクセントを最小限だけ置く配色。
 * 元のデザイン定義の 16 進値をそのまま写している（勝手に調整しない）。
 */

// --- 面（サーフェス） ---
/** 画面の一番下の地。純黒を避けた青みのある黒 */
val ObsidianCanvas = Color(0xFF050608)
val ObsidianSurface = Color(0xFF121316)
val ObsidianSurfaceLowest = Color(0xFF0D0E11)
val ObsidianSurfaceLow = Color(0xFF1B1B1F)
val ObsidianSurfaceContainer = Color(0xFF1F1F23)
val ObsidianSurfaceHigh = Color(0xFF292A2D)
val ObsidianSurfaceHighest = Color(0xFF343538)
val ObsidianSurfaceBright = Color(0xFF38393D)

/** HUD パネルの地（半透明で重ねる） */
val PanelGlass = Color(0xFF0A0D12)

/** キーキャップの地 */
val KeycapBase = Color(0xFF12161F)
val KeycapHover = Color(0xFF1A202C)
/** キーキャップ左上の小さな刻印の点 */
val KeycapMarker = Color(0xFF3B4252)

// --- アクセント ---
/** 演算・実行を表すエレクトリックシアン。このデザインの主役 */
val ElectricCyan = Color(0xFF00F0FF)
val CyanBright = Color(0xFFDBFCFF)
val CyanDim = Color(0xFF00DBE9)
val CyanDeep = Color(0xFF00363A)
val CyanContainerOn = Color(0xFF006970)

/** 消去・警告に使うアンバー */
val AmberTelemetry = Color(0xFFFFB300)
val AmberSoft = Color(0xFFFFD799)
val AmberDeep = Color(0xFF432C00)
val AmberContainerOn = Color(0xFF6A4800)

/** 科学計算系の装飾に使うイオンバイオレット */
val IonViolet = Color(0xFF7000FF)
val VioletSoft = Color(0xFFFAF3FF)
val VioletContainer = Color(0xFFE1D2FF)
val VioletDeep = Color(0xFF3C0090)

// --- 文字 ---
val OnSurfaceBright = Color(0xFFE3E2E6)
val OnSurfaceMuted = Color(0xFFB9CACB)
/** キーキャップの数字。真っ白にせず少し落とす */
val DigitText = Color(0xFFE2E8F0)
/** 補助情報（時刻・単位・注記） */
val MetaText = Color(0xFF64748B)

// --- 線 ---
/** ガラス面の上辺に入る 1px のハイライト（rgba(255,255,255,0.16)） */
val HairlineTop = Color(0x29FFFFFF)
/** 下辺に向かって消えていく側（rgba(255,255,255,0.02)） */
val HairlineBottom = Color(0x05FFFFFF)
/** 既定のヘアライン（rgba(255,255,255,0.08)） */
val Hairline = Color(0x14FFFFFF)
/** 操作中の枠（rgba(0,240,255,0.5)） */
val HairlineActive = Color(0x8000F0FF)
/** 押下時にキーの内側を満たすシアン（rgba(0,240,255,0.12)） */
val CyanPressFill = Color(0x1F00F0FF)

val Outline = Color(0xFF849495)
val OutlineVariant = Color(0xFF3B494B)

// --- エラー ---
val ErrorRed = Color(0xFFFFB4AB)
val ErrorDeep = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)
