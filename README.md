# NeoBrutalauncher

NeoBrutalauncher is an Android home launcher built around a Neo-Brutalist design language.

## Current stable baseline

The current stable baseline is the successful build represented by commit `5b4100e90f2cf30bc86a19b4ccadf47d753c2d70` (GitHub Actions Run #303).

The rollback branch `stable-base-reset-weather-music-20261008` points to this stable checkpoint. This baseline should be preserved for future development unless a change is explicitly requested.

## Current features

### Launcher
- Android HOME launcher activity.
- Installed-app discovery and launching.
- Dedicated Home, Apps, and Live pages.
- App drawer with app search.
- Pinned/favorite apps for Home tiles.
- Configurable number of Home app tiles.
- Configurable app tile content: icon, icon + text, or text.
- Per-tile position and size controls.
- Neo-Brutalist visual design with borders, offset shadows, bold typography, and saturated colors.
- Smooth animated transitions between Home, Apps, and Live pages.
- Configurable motion smoothness: SNAPPY, BALANCED, or FLUID.
- Optional Reduce Motion mode.
- App tile order is preserved and is not rearranged from app launch frequency.

### Home
- Large clock with 12-hour or 24-hour mode.
- Optional AM/PM display for 12-hour mode.
- Selectable typography styles.
- Optional local weather display with provider fallbacks for available device location providers.
- Custom image wallpaper with **Choose Image** and **Clear** controls.
- Home app tiles without the removed decorative line/marker.

### Live
- Live clock/date header.
- Calendar tile.
- Live chat notification tile with selectable app.
- Music tile using Android notification/media information.
- Music controls are disabled until the launcher has Notification Access.
- Album art is read from media metadata bitmap or URI when available.
- Recent local notes/tasks section.
- Rotating quote tile.
- Custom quote editor with up to 5 user-defined quotes; built-in quotes remain available.
- Notification access controls for Music and Live Chat features.

### Appearance and settings
- System, Light, and Dark theme modes.
- The launcher uses the base stable color system; no additional multi-theme or app icon-style system is enabled.
- Typography style selection.
- App tile content selection.
- Third-party icon pack support where compatible; launcher icon presentation uses the selected pack/original icon without a separate icon-style setting.
- Wallpaper selection from an image and wallpaper clearing.
- Backup and restore of launcher settings.
- Pinned app management.
- Live Chat app selection and clearing.
- Notification access management.

## Design principles

- Kotlin + Jetpack Compose.
- Simple, focused launcher experience.
- No root or Shizuku is required for the launcher core.
- No Accessibility Service is required by the launcher core.
- Hard borders and offset shadows instead of blurred elevation.
- Saturated colors and large typography.
- Usability comes before decoration.
- Removed features are not reintroduced unless explicitly requested.

## Permissions and integrations

The launcher can request:
- Coarse location for the optional weather feature.
- Android Notification Listener access for Music and Live Chat features.

These permissions are only used by the corresponding optional features.

## License policy

The application code in this repository is original project code unless a file explicitly says otherwise.

This project does not copy source code from third-party launcher projects. Public repositories may be used as architectural or visual references, but source code is only reused when its license permits it and the required notices are preserved.

Current audit is documented in THIRD_PARTY_NOTICES.md.

AndroidX, Jetpack Compose, Kotlin, Gradle, and other dependencies retain their respective upstream licenses.
