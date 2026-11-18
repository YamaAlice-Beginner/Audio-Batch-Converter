package com.example.androidbatchconverter.model

import com.example.androidbatchconverter.R

/** 输出音频格式。 */
enum class AudioFormat {
    MP3,
    WAV,
    FLAC,
    AAC,
    OGG,
    OPUS,
    M4A,
    ALAC,
}

/** 码率控制模式。 */
enum class RateControlMode(val labelRes: Int) {
    AUTO(R.string.rate_auto),
    CBR(R.string.rate_cbr),
    VBR(R.string.rate_vbr),
    ABR(R.string.rate_abr),
}

/**
 * 转换详细参数。空字符串表示“自动/默认”，由 ffmpeg 自行决定。
 */
data class ConversionOptions(
    val encoder: String = "",
    val rateControlMode: RateControlMode = RateControlMode.AUTO,
    val bitrate: String = "192k",
    val quality: String = "",
    val sampleRate: String = "48000",
    val channels: String = "2",
    val preset: String = "",
    val compressionLevel: String = "",
    val profile: String = "",
    val sampleFormat: String = "",
    val channelLayout: String = "",
)

/** 各格式的文件扩展名。 */
fun AudioFormat.extension(): String = when (this) {
    AudioFormat.MP3 -> "mp3"
    AudioFormat.WAV -> "wav"
    AudioFormat.FLAC -> "flac"
    AudioFormat.AAC -> "aac"
    AudioFormat.OGG -> "ogg"
    AudioFormat.OPUS -> "opus"
    AudioFormat.M4A -> "m4a"
    AudioFormat.ALAC -> "m4a"
}

/** 各格式的默认编码器。 */
fun AudioFormat.defaultEncoder(): String = when (this) {
    AudioFormat.MP3 -> "libmp3lame"
    AudioFormat.WAV -> "pcm_s16le"
    AudioFormat.FLAC -> "flac"
    AudioFormat.AAC -> "aac"
    AudioFormat.OGG -> "libvorbis"
    AudioFormat.OPUS -> "libopus"
    AudioFormat.M4A -> "aac"
    AudioFormat.ALAC -> "alac"
}

/**
 * 把详细参数翻译成 ffmpeg 参数（不含 -i 与输出路径）。
 */
fun ConversionOptions.toFfmpegArgs(format: AudioFormat): List<String> {
    val args = mutableListOf<String>()

    val enc = encoder.ifBlank { format.defaultEncoder() }
    args += listOf("-c:a", enc)

    when (rateControlMode) {
        RateControlMode.VBR ->
            if (quality.isNotBlank()) args += listOf("-q:a", quality)

        RateControlMode.CBR, RateControlMode.ABR, RateControlMode.AUTO ->
            if (bitrate.isNotBlank()) args += listOf("-b:a", bitrate)
    }

    if (sampleRate.isNotBlank()) args += listOf("-ar", sampleRate)
    if (channels.isNotBlank()) args += listOf("-ac", channels)
    if (preset.isNotBlank()) args += listOf("-preset", preset)
    if (compressionLevel.isNotBlank()) args += listOf("-compression_level", compressionLevel)
    if (profile.isNotBlank()) args += listOf("-profile:a", profile)
    if (sampleFormat.isNotBlank()) args += listOf("-sample_fmt", sampleFormat)
    if (channelLayout.isNotBlank()) args += listOf("-channel_layout", channelLayout)

    // 始终丢弃视频轨（音频转换器）
    args += "-vn"
    return args
}
