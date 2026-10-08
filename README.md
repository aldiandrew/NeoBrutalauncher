# NeoBrutalauncher

NeoBrutalauncher is an Android home launcher built around a Neo-Brutalist design language.

## Current stable baseline

The current stable baseline is the latest successful feature build represented by the current `main` branch state.

This baseline should be preserved for future development unless a change is explicitly requested.

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

### Home
- Large clock with 12-hour or 24-hour mode.
- Optional AM/PM display for 12-hour mode.
- Selectable typography styles.
- Optional local weather display.
- Custom image wallpaper with **Choose Image** and **Clear** controls.
- Home app tiles without the removed decorative line/marker.

### Live
- Live clock/date header.
- Calendar tile.
- Live chat notification tile with selectable app.
- Music tile using Android notification/media information.
- Recent local notes/tasks section.
- Rotating quote tile.
- Notification access controls for Music and Live Chat features.

### Appearance and settings
- System, Light, and Dark theme modes.
- Typography style selection.
- App tile content selection.
- Third-party icon pack support where compatible.
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
