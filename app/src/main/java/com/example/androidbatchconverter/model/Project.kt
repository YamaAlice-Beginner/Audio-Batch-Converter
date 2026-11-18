package com.example.androidbatchconverter.model

/**
 * 一个待转换的音频文件。
 *
 * @param name 文件名（用于界面展示）
 * @param uri 文件的位置，可以是 Uri 字符串或真实路径。
 * @param sizeBytes 文件大小，单位字节，默认 0 表示未知。
 */
data class AudioFile(
    val name: String,
    val uri: String,
    val sizeBytes: Long = 0L,
)

/**
 * 批量音频转换项目。
 *
 * @param id 项目唯一标识
 * @param title 项目标题
 * @param inputFolder 输入文件夹位置
 * @param outputFolder 输出文件夹位置
 * @param outputFormat 输出音频格式
 * @param options 转换详细参数
 * @param selectedAudioUris 用户显式选择的音频文件；为空表示使用输入文件夹内的全部文件。
 */
data class Project(
    val id: Long,
    val title: String,
    val inputFolder: String,
    val outputFolder: String,
    val outputFormat: AudioFormat = AudioFormat.MP3,
    val options: ConversionOptions = ConversionOptions(),
    val selectedAudioUris: List<String> = emptyList(),
)
