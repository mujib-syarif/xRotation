# xRotation

Pengatur rotasi layar untuk Android. Memaksa orientasi layar ke salah satu dari empat mode, termasuk untuk app atau game lain (misalnya game yang biasanya lanskap tetapi ingin dimainkan portrait).

Dioptimalkan untuk Infinix HOT 11s NFC. Tema: Minimal Monochrome, Glassmorphism, Data-Driven / Modular.

## Fitur

- Portrait atas
- Portrait bawah
- Lanskap kiri
- Lanskap kanan
- Statistik: rotasi hari ini, lama aktif, grafik 7 hari
- Notifikasi dengan tombol Matikan

## Cara kerja

Layanan latar depan memasang jendela overlay 1x1 piksel yang tidak terlihat. Orientasi jendela itu menentukan orientasi seluruh layar. Karena itu aplikasi butuh izin "Tampil di atas aplikasi lain".

## Unduh APK

1. Buka tab Releases di repo ini.
2. Unduh `xRotation.apk` dari rilis terbaru.
3. Izinkan instal dari sumber tidak dikenal, lalu instal.

APK dibangun otomatis oleh GitHub Actions setiap ada push ke `main`.

## Catatan

- Mode portrait bawah (terbalik) bisa diabaikan pada beberapa ponsel karena pengaturan bawaan sistemnya.
- Tampilan game tetap diatur oleh game itu sendiri. Pada mode yang tidak sesuai, tampilan bisa terlihat kecil.

## Build lokal

Butuh JDK 17, Android SDK 34, dan Gradle 8.9:

```
gradle :app:assembleDebug
```

---

©mujib_syarif
