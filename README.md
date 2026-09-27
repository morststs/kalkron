# Kalkron

<p>黒曜石とネオンの計器のような、Android 向けの電卓アプリ。</p>

四則演算と、計算履歴の保存・閲覧ができます。内部計算に `BigDecimal` を使っているため、
`0.1 + 0.2` が `0.30000000000000004` になるような誤差が出ません。

## ダウンロード

[**最新版の APK をダウンロード**](../../releases/latest)

Google Play を経由しないため、インストール時に端末側で「提供元不明のアプリ」の許可が必要です。

## 機能

- 四則演算、`%`、符号反転、小数点、`AC` / `DEL`、連続計算
- 計算履歴をローカル DB に保存し、一覧表示・全件削除
- 12 桁までの入力、有効桁数 12 桁で丸め（`HALF_UP`）
- 0 除算はエラー表示にし、次の入力で復帰
- 縦向き・横向きに対応

## 動作環境

**Android 8.0（API 26）以降**。ダークテーマ固定です。

## 画面

下部ナビで 2 つの画面を切り替えます。

| STD | HIST |
|---|---|
| HUD 風の表示部に途中式と現在値を表示するキーパッド | 計算履歴をテープログとして新しい順に表示 |

## デザイン

UI は [Google Stitch](https://stitch.withgoogle.com/) で作成したデザインシステム
*Obsidian Cyber-Precision* に基づいています。

- 黒曜石の地（`#121316`）に、演算を表すエレクトリックシアン（`#00F0FF`）と、消去系のアンバー（`#FFB300`）
- 角丸 4px（HUD パネルのみ 8px）。影を落とさず、上辺が明るい 1px のヘアライン枠で立体感を出す
- 数字とキーは JetBrains Mono、見出しは Space Grotesk、本文は Geist

## 技術構成

| 項目 | 内容 |
|---|---|
| 言語 / UI | Kotlin / Jetpack Compose（Material3） |
| 構成 | MVVM + Repository |
| 永続化 | Room |
| 画面遷移 | Navigation Compose（型安全ルート） |
| AGP / Gradle / Kotlin | 9.4.1 / 9.7.1 / 2.3.21 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 |

## ビルド

Android Studio でこのディレクトリを開くか、次を実行します。

```
./gradlew assembleDebug   # デバッグ APK
./gradlew test            # ユニットテスト
```

リリース署名を行う場合は、リポジトリ直下に `keystore.properties` を置きます
（このファイルはリポジトリに含まれません）。

```properties
storeFile=/path/to/your.jks
storePassword=****
keyAlias=****
keyPassword=****
```

ファイルが無い環境でもビルドは通ります（その場合 release は未署名になります）。

## ライセンス

同梱しているフォント（JetBrains Mono / Space Grotesk / Geist）は
いずれも SIL Open Font License 1.1 です。ライセンス全文は
`app/src/main/assets/licenses/` に含まれています。
