# NeoBrutalauncher

NeoBrutalauncher is an Android home launcher built around a Neo-Brutalist design language.

## Current stable baseline

The current stable baseline is the successful design-system build represented by commit `48e67a64ac4c8a0c1811055574fa85ac21c5804c` (GitHub Actions Run #352).

The rollback branch `stable-neobrutal-baseline-20261008` points to the pre-design-system baseline at commit `2dcf1f312cebd4ec1ee95d98c647323d8279d227`. Use that branch when a later design change needs a clean rollback point.

The current stable build should be preserved for future development unless a change is explicitly requested.

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
- Neo-Brutalist visual design with hard borders, offset shadows, bold typography, and a controlled three-accent palette.
- Smooth page transitions between Home, Apps, and Live pages; app open/return animations are intentionally disabled.
- Configurable motion smoothness: SNAPPY, BALANCED, or FLUID.
- Optional Reduce Motion mode.
- App tile order is preserved and is not rearranged from app launch frequency.

### Home
- Large clock with 12-hour or 24-hour mode.
- Optional AM/PM display for 12-hour mode.
- Typography style selection with Default and Condensed options.
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

## Neo-Brutalist design system

- Core accent palette is intentionally limited to Yellow, Cyan, and Pink, with Ink/White/Paper used as structure and neutrals.
- Borders use reusable 2dp, 3dp, and 4dp levels; hard shadows use 4dp and 6dp standard levels.
- Layout rhythm uses a 4/8/16dp mobile spacing scale, adapted from the guide's 8px rhythm for a dense launcher surface.
- Typography is organized around Meta, Label, Body, Tile, Title, and Hero levels while retaining the user's Default/Condensed preference.
- Interactive buttons use the same hard-shadow press behavior; opening and returning from apps have no decorative launch/return animation.
- Brutal blocks resolve their default content color from the surface so bright accent tiles use Ink and dark structural surfaces use DarkWhite.

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
