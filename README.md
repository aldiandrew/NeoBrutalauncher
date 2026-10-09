# **Neo Brutal Launcher**

> **A bold, focused Android home screen built around hard edges, live information, and fast access to the apps that matter.**

<p align="center">

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![Version](https://img.shields.io/badge/version-0.1.0-111111)](https://github.com/aldiandrew/NeoBrutalauncher/releases)
[![License](https://img.shields.io/badge/license-MIT-111111)](LICENSE)
[![Build](https://img.shields.io/github/actions/workflow/status/aldiandrew/NeoBrutalauncher/build.yml?branch=main&label=build)](https://github.com/aldiandrew/NeoBrutalauncher/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)](https://developer.android.com/compose)

</p>

<p align="center">
  <a href="https://github.com/aldiandrew/NeoBrutalauncher/releases">
    <strong>⬇️ DOWNLOAD APK</strong>
  </a>
  &nbsp;&nbsp;•&nbsp;&nbsp;
  <a href="https://github.com/aldiandrew/NeoBrutalauncher/issues">Report an issue</a>
</p>

---

## 🧱 Overview

**Neo Brutal Launcher** is an open-source Android launcher built for people who want a home screen that feels intentional rather than overloaded.

Its visual language is unapologetically **neo-brutalist**: hard borders, offset shadows, bold typography, saturated blocks, and visible structure. The interface is deliberately dense, but the hierarchy stays simple enough to understand at a glance.

The launcher focuses on four spaces:

- **Home** — your clock, weather, status tiles, pinned apps, music, and tasks.
- **Apps** — a searchable app drawer with alphabetical letter headers and a fast alphabet scrubber.
- **Live** — calendar, chat notifications, music, notes/tasks, and rotating quotes.
- **Settings** — themes, typography, motion, icon packs, wallpaper, pinned apps, backup, permissions, and more.

### Why was it built?

Your home screen is one of the most frequently viewed parts of your phone. It should help you get somewhere quickly, not compete for attention.

Neo Brutal Launcher takes the opposite approach to feature-heavy launchers: **strong visual identity, useful information, and less noise.**

---

## ✦ Features

### 🏠 Home

- 🕒 **Large live clock** with 12-hour or 24-hour mode and optional AM/PM.
- 🌤️ **Local weather tile** with location, temperature, weather condition, and a compact layout designed to stay readable.
- 🔋 **Battery tile** for quick battery status and charging state.
- 📶 **Network tile** for Wi-Fi/mobile/network state.
- 📌 **Pinned apps** with a configurable number of Home app slots.
- 🧩 **Flexible app tiles** with icon, icon + text, or text-only content modes.
- 📐 **Resizable and movable tiles** with persistent layout settings.
- ✅ **Tasks tile** with a compact 4×3 Home layout.
- 🎵 **Music tile** with media controls and the last-used music app retained after the media app leaves Recents.
- 🖼️ **Custom wallpaper** with image selection and clearing.
- 👋 **First-run onboarding** with favorite-app selection and permission setup.

### 📚 Apps

- 🔎 **App search** for fast filtering.
- 🔤 **Alphabetical grouping** with visible letter headers such as A, B, C, and so on.
- 🧭 **Alphabet scrubber** for jumping through large app lists quickly.
- ⭐ **Pin / unpin apps** directly from the app list.
- 🚀 **Fast launch** with long-press actions for app management.

Example:

```text
A  ──────────────────
ANDROID
AURORA

B  ──────────────────
BRAVE
BRIMO

C  ──────────────────
CALENDAR
CAMERA
```

### ⚡ Live

- 📅 **Live calendar tile**.
- 💬 **Live Chat tile** with selectable notification app.
- 🎵 **Live Music tile** with album art, progress, and playback controls.
- 💭 **Rotating quote tile** with support for custom quotes.
- 📝 **Recent Notes / Tasks** summary.

### ⚙️ Personalization

- 🌓 **System, Light, and Dark themes**.
- 🎨 **Consistent neo-brutalist styling** across Light and Dark mode.
- 🔤 **Typography styles**.
- 🎛️ **Motion smoothness** with Reduce Motion support.
- 👻 **Hide Status Bar** option for a cleaner launcher surface.
- 🎨 **Third-party icon pack support** for compatible `appfilter.xml` packs.
- 📌 **Pinned app management** with an in-settings app picker.
- 🖼️ **Wallpaper picker**.
- 💾 **Backup and restore** for launcher settings and layout.

### 👋 Onboarding

On a fresh installation, Neo Brutal Launcher guides you through:

1. Welcome
2. Permission setup
3. Favorite app selection
4. Final setup

Location access powers the optional weather feature. Notification access powers Music and Live Chat.

---

## 🚀 Highlights

### **Neo-Brutal, not neo-chaotic**

The interface is loud by design, but the structure is controlled. Reusable borders, shadows, spacing, and typography keep the experience coherent.

### **Your important apps stay close**

Pinned apps let you decide which applications belong on Home, while tile size and position controls let you shape the layout around your workflow.

### **Live information without another feed**

Weather, calendar, notifications, music, notes, tasks, and quotes are presented as focused blocks instead of a full-screen content feed.

### **Open source by default**

The source is publicly available on GitHub. Project licensing and third-party references are documented in the repository.

### **No root required for launcher core**

The launcher core relies on Android's standard launcher APIs. Root, Shizuku, and Accessibility Service are not required for the core launcher experience.

### **Designed with efficiency in mind**

Repeated work is reduced through caching and Compose memoization, media progress is lifecycle-aware, and album-art/wallpaper bitmap sizes are controlled to reduce unnecessary memory pressure.

---

## 📸 Screenshots

<p align="center">
  <img src="docs/screenshots/Screenshot_20261009-122639_Neo%20Brutal%20Launcher.png" width="30%" alt="Neo Brutal Launcher screenshot 1" />
  <img src="docs/screenshots/Screenshot_20261009-122649_Neo%20Brutal%20Launcher.png" width="30%" alt="Neo Brutal Launcher screenshot 2" />
  <img src="docs/screenshots/Screenshot_20261009-122703_Neo%20Brutal%20Launcher.png" width="30%" alt="Neo Brutal Launcher screenshot 3" />
</p>

<p align="center">
  <img src="docs/screenshots/Screenshot_20261009-122714_Neo%20Brutal%20Launcher.png" width="30%" alt="Neo Brutal Launcher screenshot 4" />
  <img src="docs/screenshots/Screenshot_20261009-122737_Neo%20Brutal%20Launcher.png" width="30%" alt="Neo Brutal Launcher screenshot 5" />
  <img src="docs/screenshots/Screenshot_20261009-122815_Neo%20Brutal%20Launcher.png" width="30%" alt="Neo Brutal Launcher screenshot 6" />
</p>

---

## 🛠️ Technology & Credits

### Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **AndroidX Core**
- **Activity Compose**
- **Lifecycle Runtime Compose**
- **Gradle**
- **JDK 17**

### Android integrations

- **Android Launcher APIs** for installed-app discovery and launching.
- **Notification Listener Service** for Music and Live Chat.
- **Android Location APIs** for weather location lookup.
- **Open-Meteo API** for weather data.
- **Icon Pack `appfilter.xml`** compatibility for supported third-party icon packs.

### Fonts

The project uses open-source font assets including **Anton** and **Space Grotesk**, with their sources tracked in the repository build configuration.

### Third-party references

Neo Brutal Launcher documents external repositories and licensing considerations in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Public repositories are treated as references unless their licenses explicitly permit source reuse. No third-party project's code, branding, or assets are presented as original Neo Brutal Launcher work.

---

## 📦 Installation

1. Open the **[GitHub Releases](https://github.com/aldiandrew/NeoBrutalauncher/releases)** page.
2. Download the latest APK.
3. Install the APK on an Android 8.0+ device.
4. Open **Neo Brutal Launcher**.
5. Complete the onboarding flow and choose your favorite apps.
6. Set Neo Brutal Launcher as your Android **default Home app**.

> Android may ask you to allow installation from the source you used to obtain the APK.

---

## 🔐 Permissions

Neo Brutal Launcher requests access only for the features that need it:

| Access | Purpose |
|---|---|
| Location | Local weather |
| Notification Access | Music and Live Chat |

The launcher core does not require root, Shizuku, or an Accessibility Service.

---

## 🧪 Build & Development

Current build configuration:

- **Android SDK:** 36
- **Minimum SDK:** 26
- **Target SDK:** 36
- **Kotlin:** 2.x
- **Jetpack Compose:** Material 3
- **Gradle:** 8.13
- **JDK:** 17

Release APKs are built with **GitHub Actions**.

[![Build](https://img.shields.io/github/actions/workflow/status/aldiandrew/NeoBrutalauncher/build.yml?branch=main&label=GitHub%20Actions)](https://github.com/aldiandrew/NeoBrutalauncher/actions/workflows/build.yml)

---

## 🗺️ Project Philosophy

> **Show the structure. Keep the function. Remove the noise.**

Neo Brutal Launcher is intentionally opinionated.

New features should improve the launcher experience rather than add complexity for its own sake. Stable visual behavior should not be changed without a deliberate reason.

---

## 📄 License

Neo Brutal Launcher is released under the **MIT License**.

See [LICENSE](LICENSE) for the complete license text.

Copyright © 2026 Aldi Andrew.

---

<p align="center">
  <strong>Built for people who want their home screen to feel like theirs.</strong>
</p>
