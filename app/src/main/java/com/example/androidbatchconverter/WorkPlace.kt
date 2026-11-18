package com.example.androidbatchconverter

import android.content.Context
import com.example.androidbatchconverter.model.AudioFile
import com.example.androidbatchconverter.model.AudioFormat
import com.example.androidbatchconverter.model.ConversionOptions
import com.example.androidbatchconverter.model.Project

/**
 * 项目存取接口。
 */
interface ProjectRepository {

    /** 返回全部项目，按创建顺序排列。 */
    suspend fun listProjects(): List<Project>

    /** 新建项目并返回带 id 的完整项目。 */
    suspend fun createProject(
        title: String,
        inputFolder: String,
        outputFolder: String,
        outputFormat: AudioFormat,
        options: ConversionOptions,
    ): Project

    /** 按 id 查询项目，不存在时返回 null。 */
    suspend fun getProject(projectId: Long): Project?

    /** 判断同名项目是否已存在（标题不区分大小写、忽略首尾空白）。 */
    suspend fun titleExists(title: String): Boolean

    /** 更新项目显式选择的音频文件列表（空列表表示使用全部文件），返回更新后的项目。 */
    suspend fun updateSelectedAudio(projectId: Long, audioUris: List<String>): Project?

    /** 更新整个项目（用于修改输出格式、转换参数等）。 */
    suspend fun updateProject(project: Project): Project?

    /** 删除项目。 */
    suspend fun deleteProject(projectId: Long)
}

/**
 * 音频文件扫描接口。
 */
interface AudioFileScanner {

    /** 扫描 [inputFolder]，返回其中的音频/视频文件列表。 */
    suspend fun scan(inputFolder: String): Result<List<AudioFile>>
}

/**
 * 批量音频转换接口。
 *
 * @return 成功时返回转换成功的文件数量。
 */
interface AudioConverter {

    suspend fun convert(
        files: List<AudioFile>,
        outputFolder: String,
        format: AudioFormat,
        options: ConversionOptions,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): Result<Int>
}

/**
 * 工作台（WorkPlace）：批量音频转换的业务门面。
 */
class WorkPlace(
    val repository: ProjectRepository,
    val scanner: AudioFileScanner,
    val converter: AudioConverter,
) {

    suspend fun listProjects(): List<Project> = repository.listProjects()

    suspend fun getProject(projectId: Long): Project? = repository.getProject(projectId)

    suspend fun titleExists(title: String): Boolean = repository.titleExists(title)

    suspend fun createProject(
        title: String,
        inputFolder: String,
        outputFolder: String,
        outputFormat: AudioFormat,
        options: ConversionOptions,
    ): Project = repository.createProject(title, inputFolder, outputFolder, outputFormat, options)

    suspend fun setSelectedAudio(projectId: Long, audioUris: List<String>): Project? =
        repository.updateSelectedAudio(projectId, audioUris)

    suspend fun updateProject(project: Project): Project? = repository.updateProject(project)

    suspend fun deleteProject(projectId: Long) = repository.deleteProject(projectId)

    suspend fun scanFolder(inputFolder: String): Result<List<AudioFile>> = scanner.scan(inputFolder)

    /**
     * 运行一次转换。
     *
     * 当项目未显式选择文件时，自动扫描输入文件夹内的全部文件；
     * 否则只转换用户选择的文件。
     */
    suspend fun runConversion(
        project: Project,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): Result<Int> {
        val files = if (project.selectedAudioUris.isEmpty()) {
            scanner.scan(project.inputFolder).getOrElse { return Result.failure(it) }
        } else {
            project.selectedAudioUris.map { uri ->
                AudioFile(name = uri.substringAfterLast('/'), uri = uri)
            }
        }
        return converter.convert(files, project.outputFolder, project.outputFormat, project.options, onProgress)
    }
}

/**
 * 构建供界面启动使用的工作台。
 */
fun defaultWorkPlace(context: Context): WorkPlace = WorkPlace(
    repository = PersistentProjectRepository(context.applicationContext),
    scanner = FileSystemAudioFileScanner(context.applicationContext),
    converter = FfmpegAudioConverter(context.applicationContext),
)
