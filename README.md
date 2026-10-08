# **Neo Brutal Launcher**

> **A bold, focused Android home screen built around hard edges, useful information, and zero visual fluff.**

<p align="center">

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![Version](https://img.shields.io/badge/version-0.1.0-111111)](https://github.com/aldiandrew/NeoBrutalauncher/releases)
[![License](https://img.shields.io/badge/license-MIT-111111)](LICENSE)
[![Build](https://img.shields.io/github/actions/workflow/status/aldiandrew/NeoBrutalauncher/build.yml?branch=main&label=build)](https://github.com/aldiandrew/NeoBrutalauncher/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)

</p>

<p align="center">
  <a href="https://github.com/aldiandrew/NeoBrutalauncher/releases">
    <strong>⬇️ DOWNLOAD APK</strong>
  </a>
</p>

---

## 🧱 Sekilas Aplikasi

**Neo Brutal Launcher** adalah launcher Android open-source untuk pengguna yang menginginkan home screen yang terasa seperti sebuah interface yang dirancang, bukan kumpulan widget yang penuh distraksi.

Neo Brutal Launcher mengambil pendekatan **neo-brutalist**: border tegas, offset shadow, tipografi besar, warna blok yang kuat, dan hierarki yang mudah dipindai. Di atas visual tersebut, launcher tetap berfokus pada pekerjaan utama sebuah home app: **membuka aplikasi dengan cepat, menampilkan informasi penting, dan memberi kontrol penuh atas layout.**

Bukan launcher yang mencoba melakukan semuanya. Ini adalah launcher yang sengaja memilih hal-hal yang penting.

### Mengapa dibuat?

Home screen sering menjadi bagian Android yang paling sering dilihat, tetapi banyak launcher justru menambahkan semakin banyak lapisan: rekomendasi, panel, animasi, feed, dan elemen dekoratif.

Neo Brutal Launcher mengambil arah yang berbeda:

- **Home** untuk akses utama dan informasi ringkas.
- **Apps** untuk menemukan aplikasi dengan cepat.
- **Live** untuk informasi yang berubah seperti kalender, chat, musik, dan quote.
- **Settings** untuk membentuk launcher sesuai kebutuhan sendiri.

---

## ✦ Fitur Utama

### 🏠 Home
- 🕒 **Jam besar** dengan mode 12/24 jam dan opsi AM/PM.
- 🌤️ **Live weather** berbasis lokasi perangkat dengan suhu, kondisi, dan lokasi yang ringkas.
- 🔋 **Battery tile** untuk melihat kondisi baterai dengan cepat.
- 📶 **Network tile** untuk status konektivitas.
- 📌 **Pinned apps** yang dapat dipilih sendiri dan ditempatkan di Home.
- 🧩 **App tiles fleksibel** dengan pilihan icon, icon + text, atau text.
- 📐 **Ukuran dan posisi tile** yang dapat diatur.
- 🎨 **Custom wallpaper** untuk memberi identitas visual pada Home.

### 📚 Apps
- 🔎 **App drawer dengan pencarian**.
- 🔤 **Navigasi alfabet** untuk daftar aplikasi panjang.
- ⭐ **Pin/unpin aplikasi** langsung dari daftar Apps.
- 🚀 **Launch cepat** tanpa harus melewati halaman tambahan.

### ⚡ Live
- 📅 **Calendar tile** untuk informasi tanggal.
- 💬 **Live Chat tile** dengan pemilihan aplikasi.
- 🎵 **Music tile** dengan aplikasi musik terakhir yang digunakan, album art, progress, dan kontrol media.
- 💭 **Quote tile** dengan quote bawaan dan quote custom.
- 📝 **Notes / Tasks** untuk ringkasan aktivitas lokal.

### ⚙️ Personalisasi
- 🌓 **System / Light / Dark theme**.
- 🔤 **Typography style**.
- 🎛️ **Motion smoothness** dan opsi Reduce Motion.
- 🖼️ **Wallpaper picker**.
- 🎨 **Third-party icon pack** yang kompatibel dengan `appfilter.xml`.
- 💾 **Backup & restore** pengaturan launcher.

### 👋 Onboarding
Pada instalasi baru, launcher memperkenalkan fungsi utamanya dan memungkinkan pengguna **langsung memilih aplikasi favorit** sebelum masuk ke Home.

---

## 🚀 Highlights

### **Neo-Brutal, bukan Neo-Chaotic**
Visualnya kuat, tetapi struktur tetap jelas. Border, shadow, spacing, dan typography dibuat konsisten sehingga tampilan tetap mudah dipindai.

### **Cepat ke aplikasi yang penting**
Pinned apps dan layout tile membuat aplikasi yang paling sering dibuka tetap berada dalam jangkauan.

### **Live information tanpa feed tambahan**
Weather, calendar, chat notification, music, dan quote ditempatkan sebagai blok informasi yang bisa dibaca sekilas.

### **Open source dan transparan**
Kode tersedia di GitHub, lisensi proyek menggunakan MIT, dan dependensi/source pihak ketiga didokumentasikan.

### **Tidak bergantung pada root**
Launcher core menggunakan Android launcher APIs dan tidak memerlukan root atau Shizuku untuk fungsi dasarnya.

### **Fokus pada efisiensi**
Kode launcher menggunakan caching, memoization, lifecycle-aware work, dan pembatasan bitmap untuk mengurangi pekerjaan berulang dan tekanan memory pada penggunaan normal.

---

## 📸 Tangkapan Layar

> Ganti placeholder berikut dengan screenshot aktual dari build terbaru.

<p align="center">
  <img src="docs/screenshots/home.png" width="30%" alt="Home" />
  <img src="docs/screenshots/apps.png" width="30%" alt="Apps" />
  <img src="docs/screenshots/live.png" width="30%" alt="Live" />
</p>

<p align="center">
  <img src="docs/screenshots/settings.png" width="30%" alt="Settings" />
  <img src="docs/screenshots/onboarding.png" width="30%" alt="Onboarding" />
  <img src="docs/screenshots/pinned-apps.png" width="30%" alt="Pinned Apps" />
</p>

---

## 🛠️ Teknologi & Sumber

### Tech Stack
- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **AndroidX Core**
- **Activity Compose**
- **Lifecycle Runtime Compose**
- **Gradle**
- **JDK 17**

### Integrasi
- **Android Launcher APIs** untuk discovery dan launching aplikasi.
- **Android Notification Listener** untuk Live Chat dan Music.
- **Android Location APIs** untuk memperoleh lokasi weather.
- **Open-Meteo API** untuk data cuaca.
- **Icon Pack appfilter.xml** untuk kompatibilitas icon pack pihak ketiga.

### Font & Credits
Proyek menggunakan font open-source yang didokumentasikan dalam asset/license yang terkait di repository, termasuk **Anton** dan **Space Grotesk**.

Pola komponen dan pendekatan neo-brutalist digunakan sebagai referensi arsitektur/visual dari proyek open-source yang tercantum di [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Neo Brutal Launcher tidak mengklaim kepemilikan atas proyek, library, atau asset pihak ketiga tersebut.

Lihat [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) untuk detail lisensi dan audit source.

---

## 📦 Cara Instalasi

1. Buka halaman **[GitHub Releases](https://github.com/aldiandrew/NeoBrutalauncher/releases)**.
2. Unduh APK terbaru.
3. Instal APK di perangkat Android 8.0 atau lebih baru.
4. Jalankan **Neo Brutal Launcher**.
5. Pada instalasi pertama, ikuti onboarding dan pilih aplikasi favorit.
6. Jadikan Neo Brutal Launcher sebagai aplikasi **Home/Launcher default** Android.

> Android dapat meminta izin instalasi dari sumber tertentu saat memasang APK di luar Play Store.

---

## 🔐 Permissions

Neo Brutal Launcher menggunakan izin hanya untuk fitur yang membutuhkannya:

| Permission | Digunakan untuk |
|---|---|
| Location | Weather berdasarkan lokasi perangkat |
| Notification Listener | Music dan Live Chat |

Launcher core tidak membutuhkan root atau Accessibility Service.

---

## 🧪 Build & Development

Build utama menggunakan:

- Android SDK 36
- Kotlin 2.x
- Jetpack Compose
- Gradle 8.13
- JDK 17

APK release dibangun melalui **GitHub Actions**.

Build workflow:
[![Build](https://img.shields.io/github/actions/workflow/status/aldiandrew/NeoBrutalauncher/build.yml?branch=main&label=GitHub%20Actions)](https://github.com/aldiandrew/NeoBrutalauncher/actions/workflows/build.yml)

---

## 🗺️ Project Direction

Neo Brutal Launcher dikembangkan dengan prinsip sederhana:

> **Show the structure. Keep the function. Remove the noise.**

Fitur baru harus memperkuat fungsi launcher, bukan hanya menambah kompleksitas.

Baseline desain dan perubahan besar dijaga agar tidak mengubah pengalaman visual yang sudah stabil tanpa permintaan eksplisit.

---

## 📄 License

Neo Brutal Launcher tersedia di bawah **MIT License**.

Lihat [LICENSE](LICENSE) untuk teks lisensi lengkap.

Copyright © 2026 Insomdroid.

---

<p align="center">
  <strong>Built for people who want their home screen to feel like theirs.</strong>
</p>
