package com.example.androidbatchconverter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.LocaleList
import com.example.androidbatchconverter.model.ConversionOptions
import com.example.androidbatchconverter.model.RateControlMode
import java.util.Locale

/** 主题模式。 */
enum class ThemeMode(val labelRes: Int) {
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark),
    SYSTEM(R.string.theme_system),
}

/** 应用语言。 */
enum class Language(val labelRes: Int) {
    SYSTEM(R.string.language_system),
    CHINESE(R.string.language_chinese),
    ENGLISH(R.string.language_english),
}

/** 应用设置。 */
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: Language = Language.SYSTEM,
    val defaultInputFolder: String = DEFAULT_INPUT_FOLDER,
    val defaultOutputFolder: String = DEFAULT_OUTPUT_FOLDER,
    val rememberLastPath: Boolean = true,
    val conversionOptions: ConversionOptions = ConversionOptions(),
) {
    companion object {
        const val DEFAULT_INPUT_FOLDER = "/storage/emulated/0/Music/input"
        const val DEFAULT_OUTPUT_FOLDER = "/storage/emulated/0/Music/output"
    }
}

/**
 * 设置的持久化与语言应用。
 */
object AppSettings {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_INPUT = "default_input_folder"
    private const val KEY_OUTPUT = "default_output_folder"
    private const val KEY_REMEMBER_PATH = "remember_last_path"
    private const val KEY_LAST_FOLDER = "last_picked_folder_uri"
    private const val OPT_PREFIX = "conv_opt_"

    fun load(context: Context): Settings {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Settings(
            themeMode = enumValueOrDefault(p.getString(KEY_THEME, null), ThemeMode.SYSTEM),
            language = enumValueOrDefault(p.getString(KEY_LANGUAGE, null), Language.SYSTEM),
            defaultInputFolder = (p.getString(KEY_INPUT, "") ?: "").ifBlank { Settings.DEFAULT_INPUT_FOLDER },
            defaultOutputFolder = (p.getString(KEY_OUTPUT, "") ?: "").ifBlank { Settings.DEFAULT_OUTPUT_FOLDER },
            rememberLastPath = p.getBoolean(KEY_REMEMBER_PATH, true),
            conversionOptions = ConversionOptions(
                encoder = p.getString(OPT_PREFIX + "encoder", "") ?: "",
                rateControlMode = enumValueOrDefault(p.getString(OPT_PREFIX + "rate", null), RateControlMode.AUTO),
                bitrate = (p.getString(OPT_PREFIX + "bitrate", "") ?: "").ifBlank { "192k" },
                quality = p.getString(OPT_PREFIX + "quality", "") ?: "",
                sampleRate = (p.getString(OPT_PREFIX + "sample_rate", "") ?: "").ifBlank { "48000" },
                channels = (p.getString(OPT_PREFIX + "channels", "") ?: "").ifBlank { "2" },
                preset = p.getString(OPT_PREFIX + "preset", "") ?: "",
                compressionLevel = p.getString(OPT_PREFIX + "compression", "") ?: "",
                profile = p.getString(OPT_PREFIX + "profile", "") ?: "",
                sampleFormat = p.getString(OPT_PREFIX + "sample_format", "") ?: "",
                channelLayout = p.getString(OPT_PREFIX + "channel_layout", "") ?: "",
            ),
        )
    }

    fun save(context: Context, settings: Settings) {
        val o = settings.conversionOptions
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putString(KEY_LANGUAGE, settings.language.name)
            .putString(KEY_INPUT, settings.defaultInputFolder)
            .putString(KEY_OUTPUT, settings.defaultOutputFolder)
            .putBoolean(KEY_REMEMBER_PATH, settings.rememberLastPath)
            .putString(OPT_PREFIX + "encoder", o.encoder)
            .putString(OPT_PREFIX + "rate", o.rateControlMode.name)
            .putString(OPT_PREFIX + "bitrate", o.bitrate)
            .putString(OPT_PREFIX + "quality", o.quality)
            .putString(OPT_PREFIX + "sample_rate", o.sampleRate)
            .putString(OPT_PREFIX + "channels", o.channels)
            .putString(OPT_PREFIX + "preset", o.preset)
            .putString(OPT_PREFIX + "compression", o.compressionLevel)
            .putString(OPT_PREFIX + "profile", o.profile)
            .putString(OPT_PREFIX + "sample_format", o.sampleFormat)
            .putString(OPT_PREFIX + "channel_layout", o.channelLayout)
            .apply()
    }

    /** 上次选择的文件夹 URI（用于“记住上一个路径”）。 */
    fun lastFolder(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_LAST_FOLDER, null)

    /** 记录上次选择的文件夹，并尝试保留访问权限。 */
    fun saveLastFolder(context: Context, uri: Uri) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_FOLDER, uri.toString())
            .apply()
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        } catch (_: Exception) {
            // 某些 provider 不支持持久化授权，忽略即可。
        }
    }

    /** 语言 -> Locale；SYSTEM 返回 null 表示跟随系统。 */
    fun localeFor(language: Language): Locale? = when (language) {
        Language.SYSTEM -> null
        Language.CHINESE -> Locale.SIMPLIFIED_CHINESE
        Language.ENGLISH -> Locale.ENGLISH
    }

    /** 把持久化的语言应用到 Context，返回（可能被替换的）Context。 */
    fun applyLocale(context: Context): Context {
        val locale = localeFor(load(context).language) ?: return context
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(locale))
        return context.createConfigurationContext(config)
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        value?.let { runCatching { enumValueOf<T>(it) }.getOrDefault(default) } ?: default
}
