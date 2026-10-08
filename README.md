# NB Launcher

NB Launcher is an Android home launcher built around a controlled Neo-Brutalist design language.

## Stable baseline

The branch `stable-nb-launcher-20261008` points to the latest successful build of this upgrade batch. That stable checkpoint should be preserved for future development unless a change is explicitly requested.

A pre-upgrade rollback checkpoint is available at `stable-before-next-upgrades-20261008`, based on the previous successful build `2dcf1f312cebd4ec1ee95d98c647323d8279d227`.

## Current features

### Launcher
- Android HOME launcher activity.
- Installed-app discovery and launching.
- Dedicated Home, Apps, and Live pages.
- App drawer with app search and alphabetical scrubber.
- Pinned apps with selection from both Apps and Settings.
- Configurable Home app slots: 3, 5, or 7.
- Configurable app tile content: icon, icon + text, or text.
- Per-tile position and size controls.
- Neo-Brutalist visual system with hard borders, offset shadows, bold typography, and a controlled accent palette.
- Smooth page transitions between Home, Apps, and Live.
- Smooth Activity-level app open transition and a short launcher return settle motion.
- Configurable motion smoothness: SNAPPY, BALANCED, or FLUID.
- Optional Reduce Motion mode.

### Home
- Large clock with 12-hour or 24-hour mode.
- Optional AM/PM display for 12-hour mode.
- Typography settings expose only Default and Condensed.
- Optional local weather display with provider fallbacks for available device location providers.
- Custom image wallpaper with Choose Image and Clear controls.
- Home app tiles without the removed decorative line/marker.
- Pinned apps are explicitly prioritized on Home and remain independent from Home slot count.

### Apps
- Alphabetical app list with search.
- Pin/unpin actions.
- Pinning an app immediately clears any Home exclusion for that app.
- Long-press app actions for pinning, opening, app info, and uninstall where supported.

### Live
- Live clock/date header.
- Calendar tile.
- Live chat notification tile with selectable app.
- Music tile using Android notification/media information.
- Music controls are disabled until Notification Access is granted.
- Album art from media metadata bitmap or URI when available.
- Tapping an active Music tile opens the application currently providing the media session.
- Music playback progress uses the shared square/hard-edged progress component.
- Recent local notes/tasks section.
- Rotating quote tile.
- Custom quote editor with up to 5 user-defined quotes while built-in quotes remain available.

### Appearance and settings
- System, Light, and Dark appearance modes.
- Controlled Neo-Brutalist palette: Yellow, Cyan, and Pink with Ink/White/Paper neutrals.
- Typography settings: Default and Condensed.
- Third-party icon pack support where compatible.
- Wallpaper selection from an image and wallpaper clearing.
- Pinned app picker under Pinned Apps.
- Optional built-in status bar hiding on the launcher window.
- Backup and restore of launcher settings.
- Live Chat app selection and clearing.
- Notification access management.

### First run
- Five-step welcome/onboarding flow for new installs.
- Welcome, principles/permissions, basic preferences, feature overview, and final setup.
- Skip is available after the first step.
- Existing installations are not interrupted by onboarding after an upgrade.

### Branding
- User-facing launcher name: NB Launcher.
- Adaptive launcher icon with a geometric NB mark using the launcher palette.
- Launcher application and Music access labels use NB branding.

## Neo-Brutalist design system

- Limited accent palette and strong Ink/White/Paper structural colors.
- Hard rectangular geometry with zero-radius surfaces.
- Reusable border and shadow levels with no blur-based elevation.
- A 4/8/16dp mobile spacing rhythm adapted from the guide's 8px rhythm.
- Strong display typography paired with readable body typography.
- Pressed states use physical offset/shadow behavior.
- Bright surfaces resolve their text to Ink; dark structural surfaces use DarkWhite.
- Decorative elements are used selectively instead of being repeated on every tile.

## Implementation principles

- Kotlin + Jetpack Compose.
- No root or Shizuku is required for the launcher core.
- No Accessibility Service is required by the launcher core.
- Optional features request only their corresponding Android permissions.
- External app launch animations use Android Activity transition APIs so app launch is not delayed by a Compose animation.
- App return feedback is short and local to the launcher window.
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
