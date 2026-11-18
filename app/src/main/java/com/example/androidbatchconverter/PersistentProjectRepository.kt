package com.example.androidbatchconverter

import android.content.Context
import com.example.androidbatchconverter.model.AudioFormat
import com.example.androidbatchconverter.model.ConversionOptions
import com.example.androidbatchconverter.model.Project
import com.example.androidbatchconverter.model.RateControlMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 基于文件的持久化项目仓库。
 *
 * 项目数据以 JSON 形式保存在应用专属外部存储目录：
 * /storage/emulated/0/Android/data/<packageName>/files/projects.json
 * （即 context.getExternalFilesDir(null)，无需任何存储权限）
 */
class PersistentProjectRepository(context: Context) : ProjectRepository {

    private val storageDir = context.getExternalFilesDir(null) ?: context.filesDir
    private val file = File(storageDir, "projects.json")
    private val projects = LinkedHashMap<Long, Project>()
    private var nextId = 1L

    init {
        load()
    }

    private fun load() {
        if (!file.exists()) return
        try {
            val root = JSONObject(file.readText())
            val arr = root.getJSONArray("projects")
            for (i in 0 until arr.length()) {
                val p = projectFromJson(arr.getJSONObject(i))
                projects[p.id] = p
                if (p.id >= nextId) nextId = p.id + 1
            }
        } catch (_: Exception) {
            // 文件损坏时忽略，按空仓库启动。
        }
    }

    private fun save() {
        try {
            val arr = JSONArray()
            projects.values.forEach { arr.put(projectToJson(it)) }
            file.writeText(JSONObject().put("projects", arr).toString())
        } catch (_: Exception) {
            // 写入失败时忽略（内存中仍保留数据，下次再尝试落盘）。
        }
    }

    override suspend fun listProjects(): List<Project> = projects.values.toList()

    override suspend fun createProject(
        title: String,
        inputFolder: String,
        outputFolder: String,
        outputFormat: AudioFormat,
        options: ConversionOptions,
    ): Project {
        val project = Project(
            id = nextId++,
            title = title,
            inputFolder = inputFolder,
            outputFolder = outputFolder,
            outputFormat = outputFormat,
            options = options,
        )
        projects[project.id] = project
        save()
        return project
    }

    override suspend fun getProject(projectId: Long): Project? = projects[projectId]

    override suspend fun titleExists(title: String): Boolean =
        projects.values.any { it.title.trim().equals(title.trim(), ignoreCase = true) }

    override suspend fun updateSelectedAudio(projectId: Long, audioUris: List<String>): Project? {
        val current = projects[projectId] ?: return null
        val updated = current.copy(selectedAudioUris = audioUris)
        projects[projectId] = updated
        save()
        return updated
    }

    override suspend fun updateProject(project: Project): Project? {
        if (!projects.containsKey(project.id)) return null
        projects[project.id] = project
        save()
        return project
    }

    override suspend fun deleteProject(projectId: Long) {
        projects.remove(projectId)
        save()
    }

    private fun projectToJson(p: Project): JSONObject = JSONObject().apply {
        put("id", p.id)
        put("title", p.title)
        put("inputFolder", p.inputFolder)
        put("outputFolder", p.outputFolder)
        put("outputFormat", p.outputFormat.name)
        put("options", optionsToJson(p.options))
        put("selectedAudioUris", JSONArray(p.selectedAudioUris))
    }

    private fun optionsToJson(o: ConversionOptions): JSONObject = JSONObject().apply {
        put("encoder", o.encoder)
        put("rateControlMode", o.rateControlMode.name)
        put("bitrate", o.bitrate)
        put("quality", o.quality)
        put("sampleRate", o.sampleRate)
        put("channels", o.channels)
        put("preset", o.preset)
        put("compressionLevel", o.compressionLevel)
        put("profile", o.profile)
        put("sampleFormat", o.sampleFormat)
        put("channelLayout", o.channelLayout)
    }

    private fun projectFromJson(o: JSONObject): Project {
        val uriArr = o.optJSONArray("selectedAudioUris")
        val uris = if (uriArr == null) emptyList()
        else (0 until uriArr.length()).map { uriArr.getString(it) }
        return Project(
            id = o.getLong("id"),
            title = o.getString("title"),
            inputFolder = o.getString("inputFolder"),
            outputFolder = o.getString("outputFolder"),
            outputFormat = enumOrDefault(o.optString("outputFormat"), AudioFormat.MP3),
            options = optionsFromJson(o.optJSONObject("options")),
            selectedAudioUris = uris,
        )
    }

    private fun optionsFromJson(o: JSONObject?): ConversionOptions {
        if (o == null) return ConversionOptions()
        return ConversionOptions(
            encoder = o.optString("encoder"),
            rateControlMode = enumOrDefault(o.optString("rateControlMode"), RateControlMode.AUTO),
            bitrate = o.optString("bitrate", "192k"),
            quality = o.optString("quality"),
            sampleRate = o.optString("sampleRate", "48000"),
            channels = o.optString("channels", "2"),
            preset = o.optString("preset"),
            compressionLevel = o.optString("compressionLevel"),
            profile = o.optString("profile"),
            sampleFormat = o.optString("sampleFormat"),
            channelLayout = o.optString("channelLayout"),
        )
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
        runCatching { enumValueOf<T>(name) }.getOrDefault(default)
}
