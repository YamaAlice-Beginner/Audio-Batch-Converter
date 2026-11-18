package com.example.androidbatchconverter

import com.example.androidbatchconverter.model.Project

/** 项目搜索方式。 */
enum class SearchMode(val hintRes: Int) {
    NAME(R.string.search_hint_name),
    PATH(R.string.search_hint_path),
    REGEX(R.string.search_hint_regex),
}

/** 判断正则表达式是否合法。 */
fun isRegexValid(query: String): Boolean = runCatching { Regex(query) }.isSuccess

/**
 * 按指定方式过滤项目列表。
 *
 * - NAME：匹配项目标题
 * - PATH：匹配输入或输出文件夹
 * - REGEX：对标题、输入、输出做正则匹配；表达式非法时返回空列表。
 */
fun filterProjects(projects: List<Project>, mode: SearchMode, query: String): List<Project> {
    if (query.isBlank()) return projects
    return when (mode) {
        SearchMode.NAME -> projects.filter {
            it.title.contains(query, ignoreCase = true)
        }

        SearchMode.PATH -> projects.filter {
            it.inputFolder.contains(query, ignoreCase = true) ||
                it.outputFolder.contains(query, ignoreCase = true)
        }

        SearchMode.REGEX -> {
            val regex = runCatching { Regex(query, RegexOption.IGNORE_CASE) }.getOrNull()
                ?: return emptyList()
            projects.filter {
                regex.containsMatchIn(it.title) ||
                    regex.containsMatchIn(it.inputFolder) ||
                    regex.containsMatchIn(it.outputFolder)
            }
        }
    }
}
