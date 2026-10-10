# Neo Brutal Launcher

An open-source Android launcher with a bold neo-brutalist interface, configurable Home tiles, a searchable app drawer, and a Live page.

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/license-MIT-111111)](LICENSE)
[![Build](https://img.shields.io/github/actions/workflow/status/aldiandrew/NeoBrutalauncher/build.yml?branch=main&label=build)](https://github.com/aldiandrew/NeoBrutalauncher/actions/workflows/build.yml)

## Current features

- **Home:** live clock and date, rotating quote, configurable app tiles, battery and network tiles, optional weather, music controls, and a second distinct quote tile.
- **Tile layout:** supported app tile sizes are 1×1, 2×1, 3×1, and 4×1. Tile order and sizes persist.
- **Apps:** searchable alphabetical app drawer, letter scrubber, app launch and pin management.
- **Live:** calendar, selected-app notification summary, media controls, quote and weather tiles.
- **Settings:** system/light/dark theme mode, design palette, default/condensed typography, wallpaper, icon packs, pinned apps, quotes, motion smoothness, and backup/restore.
- **Gestures:** optional Settings category swiping, double-tap the Home clock to open Settings, and long-press the Home clock to open Apps. These gestures require no special Android permission.
- **Quotes:** ten built-in quotes plus up to five custom quotes. The main and lower Home quote tiles display different entries.
- **Efficiency:** app discovery is cached, Compose state is remembered where useful, media artwork is bounded, and no background polling is added for gesture handling.

## Requirements and permissions

- Android 8.0 (API 26) or newer.
- Location permission is optional and used for weather.
- Notification access is optional and used by Music and Live notification features.
- Core launcher and listed gestures do not require root, Shizuku, or Accessibility Service.

## Build

The Android app is written in Kotlin with Jetpack Compose and Material 3. Release APKs are produced by GitHub Actions.

- Android SDK / target SDK: 36
- Minimum SDK: 26
- JDK: 17

See [GitHub Actions](https://github.com/aldiandrew/NeoBrutalauncher/actions) for build status and artifacts.

## Screenshots

Screenshots maintained in [docs/screenshots](docs/screenshots/).

## Third-party notices

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for applicable license and attribution information.

## License

MIT. See [LICENSE](LICENSE).
