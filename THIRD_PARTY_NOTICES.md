# Third-Party Notices

This project (**Audio Batch Converter**) includes or depends on the following
third-party components. Each component is governed by its own license, which
applies to that component.

## FFmpeg Kit — LGPL-3.0

- Component: `dev.ffmpegkit-maintained:ffmpeg-kit-audio` (community-maintained fork of FFmpegKit)
- License: **GNU Lesser General Public License v3.0 (LGPL-3.0)**
- Source: https://github.com/ffmpegkit-maintained/ffmpeg
- License text: https://github.com/ffmpegkit-maintained/ffmpeg/blob/main/LICENSE

The app bundles the FFmpeg Kit native libraries (`.so`). Under LGPL-3.0 you must
be able to replace those libraries; the app loads them dynamically, and the
corresponding source is available from the project linked above.

## FFmpeg

- License: **LGPL-2.1-or-later** (the `ffmpeg-kit-audio` build does not enable GPL components)
- Home: https://ffmpeg.org/
- Legal / license information: https://ffmpeg.org/legal.html

## AndroidX / Jetpack Compose / Material 3

- Components: `androidx.*`, `androidx.compose.*`
- License: **Apache License 2.0**
- https://www.apache.org/licenses/LICENSE-2.0

## AndroidX DocumentFile

- Component: `androidx.documentfile:documentfile`
- License: **Apache License 2.0**

## Kotlin / kotlinx

- Components: Kotlin standard library, kotlinx-coroutines
- License: **Apache License 2.0**

## Note on redistribution

If you fork or redistribute this project (including the built APK), please:

1. Keep the project `LICENSE` file.
2. Keep this `THIRD_PARTY_NOTICES.md` file.
3. Comply with the LGPL-3.0 obligations for the bundled FFmpeg Kit / FFmpeg
   libraries (dynamic linking is used, and the source is available at the link
   above).
