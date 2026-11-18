package com.example.androidbatchconverter

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.androidbatchconverter.model.AudioFile
import com.example.androidbatchconverter.model.AudioFormat
import com.example.androidbatchconverter.model.ConversionOptions
import com.example.androidbatchconverter.model.extension
import com.example.androidbatchconverter.model.toFfmpegArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * 基于 FFmpeg Kit（dev.ffmpegkit-maintained 维护分支）的批量音频转换实现。
 *
 * 输入支持音频与视频（通过 -vn 丢弃视频轨、只转音频），输出覆盖 MP3/WAV/FLAC/AAC/OGG/OPUS/M4A/ALAC。
 * 输出目录既支持真实路径，也支持 SAF 树 URI（先转到临时真实目录，再写回树内）。
 */
class FfmpegAudioConverter(private val context: Context) : AudioConverter {

    override suspend fun convert(
        files: List<AudioFile>,
        outputFolder: String,
        format: AudioFormat,
        options: ConversionOptions,
        onProgress: (done: Int, total: Int) -> Unit,
    ): Result<Int> {
        if (files.isEmpty()) return Result.success(0)

        return withContext(Dispatchers.IO) {
            // content:// 树 URI 会先落到临时真实目录，转完再写回树内。
            val (workDir, exportTree) = resolveOutput(outputFolder)
            if (!workDir.exists() && !workDir.mkdirs()) {
                return@withContext Result.failure(IOException("无法创建输出目录：${workDir.absolutePath}"))
            }

            var success = 0
            var firstError: Throwable? = null

            files.forEachIndexed { index, file ->
                try {
                    val resolved = resolveInput(file)
                    val outFile = File(workDir, outputName(resolved.name, format))
                    val args = listOf("-y", "-i", resolved.path) + options.toFfmpegArgs(format) + listOf(outFile.absolutePath)
                    val session = FFmpegKit.executeWithArguments(args.toTypedArray())
                    if (ReturnCode.isSuccess(session.returnCode)) {
                        if (exportTree != null && outFile.exists()) {
                            exportToTree(exportTree, outFile)
                        }
                        success++
                    } else if (firstError == null) {
                        firstError = IOException(session.allLogsAsString ?: "ffmpeg 执行失败")
                    }
                } catch (e: Exception) {
                    if (firstError == null) firstError = e
                }
                onProgress(index + 1, files.size)
            }

            if (success == 0 && firstError != null) Result.failure(firstError) else Result.success(success)
        }
    }

    /** 解析输出目录：返回 (真实工作目录, 需要回写的 SAF 树或 null)。 */
    private fun resolveOutput(folder: String): Pair<File, DocumentFile?> {
        return if (folder.startsWith("content://")) {
            val tree = DocumentFile.fromTreeUri(context, Uri.parse(folder))
            val workDir = File(context.cacheDir, "ffmpeg_output").apply { mkdirs() }
            workDir to tree
        } else {
            File(folder) to null
        }
    }

    /** 把转好的文件写回 SAF 树目录。 */
    private fun exportToTree(tree: DocumentFile, file: File) {
        val ext = file.name.substringAfterLast('.', "").lowercase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        val target = tree.createFile(mime, file.name)
            ?: throw IOException("无法在输出目录创建文件：${file.name}")
        context.contentResolver.openOutputStream(target.uri)?.use { out ->
            file.inputStream().use { input -> input.copyTo(out) }
        } ?: throw IOException("无法写入输出文件：${file.name}")
    }

    /** 解析输入：返回 (真实文件路径, 干净的文件名)。 */
    private fun resolveInput(file: AudioFile): ResolvedInput {
        val uri = file.uri
        if (!uri.startsWith("content://")) {
            val name = file.name.ifBlank { File(uri).name }.ifBlank { "input_${System.currentTimeMillis()}" }
            return ResolvedInput(uri, name)
        }

        val contentUri = Uri.parse(uri)
        val name = queryDisplayName(contentUri)
            ?: decodeName(contentUri)
            ?: file.name.ifBlank { "input_${System.currentTimeMillis()}" }

        val dir = File(context.cacheDir, "ffmpeg_input").apply { mkdirs() }
        val out = File(dir, name)
        context.contentResolver.openInputStream(contentUri)?.use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IOException("无法读取输入文件：$name")
        return ResolvedInput(out.absolutePath, name)
    }

    /** 通过 ContentResolver 查询真实文件名（如 song.mp3）。 */
    private fun queryDisplayName(uri: Uri): String? {
        val cursor = context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null,
        ) ?: return null
        return cursor.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else null
            } else {
                null
            }
        }
    }

    /** 从 content URI 的最后一段解码出文件名（兜底）。 */
    private fun decodeName(uri: Uri): String? {
        val last = uri.lastPathSegment ?: return null
        return Uri.decode(last).substringAfterLast('/').ifBlank { null }
    }

    private fun outputName(fileName: String, format: AudioFormat): String {
        val base = fileName.substringBeforeLast('.').ifBlank { fileName }
        return "$base.${format.extension()}"
    }

    private data class ResolvedInput(val path: String, val name: String)
}
