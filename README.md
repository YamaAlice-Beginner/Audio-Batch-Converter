# Audio Batch Converter

An Android app for **batch audio conversion**, powered by [FFmpeg](https://ffmpeg.org/).

[中文说明](#中文说明)

## Features

- **Batch conversion** — convert a whole folder of audio/video files at once.
- **Input**: all common audio formats **and video** (the audio track is extracted).
- **Output**: MP3, WAV, FLAC, AAC, OGG, OPUS, M4A, ALAC.
- **Per-project encoding parameters**: encoder, bitrate, sample rate, channels, quality, preset, compression level, profile, rate control mode, sample format, channel layout.
- **Project management** — create, search (by name / path / regex), open, delete.
- **File selection** — pick some files from the input folder, or use all of them by default.
- **Theme** — light / dark / follow system.
- **Language** — Chinese / English.
- **Default folders** and an optional **"remember last path"**.
- **Persistence** — projects are saved as JSON in app-private external storage.

## Download

Get the latest APK from the [Releases](../../releases/latest) page.

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- [FFmpeg Kit](https://github.com/ffmpegkit-maintained/ffmpeg) — community-maintained fork (`dev.ffmpegkit-maintained:ffmpeg-kit-audio`)
- AndroidX + Android Storage Access Framework

## Build

Requires JDK 17+ and the Android SDK.

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

For a release build you need your own signing keystore (see `app/build.gradle.kts`).

## Project structure

```
app/src/main/java/com/example/androidbatchconverter/
├── MainActivity.kt              # app shell, top bar, navigation
├── Settings.kt                  # persisted settings (theme, language, folders, defaults)
├── WorkPlace.kt                 # business facade + project repository interface
├── PersistentProjectRepository.kt
├── FfmpegAudioConverter.kt      # FFmpeg conversion
├── FileSystemAudioFileScanner.kt
├── StoragePermission.kt
├── Search.kt
├── model/                       # Project, AudioFile, AudioFormat, ConversionOptions
└── ui/screens/                  # list / create / detail / settings / converter-defaults
```

## Notes

- The `ffmpeg-kit-audio` fork ships **arm64-v8a** and **x86_64** native libraries only.
- Reading/writing the default real paths (`/storage/emulated/0/Music/...`) needs the **"All files access"** permission on Android 11+. Folders picked through the system folder picker (SAF) work without it.

## 中文说明

**Audio Batch Converter** 是一个基于 FFmpeg 的 Android 批量音频转换工具。

- 支持输入音频与视频（自动提取音轨），输出 MP3 / WAV / FLAC / AAC / OGG / OPUS / M4A / ALAC。
- 每个项目可自定义编码器、比特率、采样率、声道数、质量、预设、压缩等级、profile、码率控制模式、采样格式、声道布局。
- 项目列表支持按名称 / 路径 / 正则搜索；可创建、打开、删除项目。
- 支持亮色 / 暗色 / 跟随系统主题，中文 / English 界面。
- 可设置默认输入/输出文件夹，并可「记住上一个路径」。
- 项目以 JSON 形式保存在应用专属外部存储中。

**下载**：见 [Releases](../../releases/latest)。

**构建**：`./gradlew assembleDebug`（需要 JDK 17+ 与 Android SDK）。

## License

The application source code is released under the **MIT License** — see [LICENSE](LICENSE).

Third-party components have their own licenses. Notably, the bundled **FFmpeg Kit / FFmpeg** libraries are **LGPL-3.0**. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

If you fork this project, please keep `LICENSE` and `THIRD_PARTY_NOTICES.md`.

### 许可证说明（中文）

- 应用源码采用 **MIT 许可证**（见 [LICENSE](LICENSE)），允许自由使用、修改、分发（需保留版权声明）。
- 内置的 **FFmpeg Kit / FFmpeg** 为 **LGPL-3.0**，属于动态链接的第三方库，源码见 [FFmpegKit](https://github.com/ffmpegkit-maintained/ffmpeg)。分发时请保留 `LICENSE` 与 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
