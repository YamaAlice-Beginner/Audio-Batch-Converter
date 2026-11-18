package com.example.androidbatchconverter

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.androidbatchconverter.model.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 扫描输入文件夹，返回其中的音频/视频文件（视频会由转换器只提取音频）。
 *
 * 支持两种输入：
 * - 真实文件路径（File 遍历）
 * - SAF 树 URI（DocumentFile 遍历）
 */
class FileSystemAudioFileScanner(private val context: Context) : AudioFileScanner {

    private val mediaExtensions = setOf(
        // 音频
        "mp3", "wav", "flac", "aac", "ogg", "opus", "m4a", "alac", "amr", "wma", "aiff", "ac3",
        // 视频（只提取音轨）
        "mp4", "mkv", "webm", "avi", "mov", "m4v", "ts", "flv", "wmv", "3gp", "mpg", "mpeg",
    )

    override suspend fun scan(inputFolder: String): Result<List<AudioFile>> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (inputFolder.startsWith("content://")) {
                    scanDocumentTree(inputFolder)
                } else {
                    scanDirectory(File(inputFolder))
                }
            }
        }

    private fun scanDirectory(dir: File): List<AudioFile> {
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles()
            ?.filter { it.isFile && isMedia(it.name) }
            ?.map { AudioFile(name = it.name, uri = it.absolutePath, sizeBytes = it.length()) }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    private fun scanDocumentTree(uriString: String): List<AudioFile> {
        val tree = DocumentFile.fromTreeUri(context, Uri.parse(uriString)) ?: return emptyList()
        return tree.listFiles()
            .filter { it.isFile && isMedia(it.name.orEmpty()) }
            .map { AudioFile(name = it.name.orEmpty(), uri = it.uri.toString(), sizeBytes = it.length()) }
            .sortedBy { it.name.lowercase() }
    }

    private fun isMedia(name: String): Boolean =
        name.substringAfterLast('.', "").lowercase() in mediaExtensions
}
