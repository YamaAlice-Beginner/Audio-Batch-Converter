package com.example.androidbatchconverter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidbatchconverter.R
import com.example.androidbatchconverter.SearchMode
import com.example.androidbatchconverter.isRegexValid
import com.example.androidbatchconverter.model.Project
import com.example.androidbatchconverter.ui.theme.AndroidBatchConverterTheme


/**
 * 项目列表页：单列展示项目；启用搜索时顶部显示搜索框。
 * “创建项目”入口由外层 Scaffold 的右下角 FAB 提供。
 */
@Composable
fun ProjectListScreen(
    projects: List<Project>,
    searchMode: SearchMode?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onOpenProject: (Project) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        if (searchMode != null) {
            SearchBar(
                mode = searchMode,
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClear = onClearSearch,
            )
            Spacer(Modifier.height(12.dp))
        }

        if (projects.isEmpty()) {
            val hint = if (searchMode != null && searchQuery.isNotBlank()) {
                stringResource(R.string.no_search_results)
            } else {
                stringResource(R.string.empty_projects_hint)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // 预留底部空间，避免右下角 FAB 遮住最后一项。
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onOpenProject(project) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    mode: SearchMode,
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(mode.hintRes)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onClear) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.clear_search),
                )
            }
        },
    )
    if (mode == SearchMode.REGEX && query.isNotBlank() && !isRegexValid(query)) {
        Text(
            text = stringResource(R.string.regex_invalid),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.project_input_folder, project.inputFolder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.project_output_folder, project.outputFolder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectListScreenPreview() {
    AndroidBatchConverterTheme {
        ProjectListScreen(
            projects = listOf(
                Project(1L, "示例项目", "/storage/emulated/0/Music", "/storage/emulated/0/Output"),
            ),
            searchMode = null,
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            onOpenProject = {},
        )
    }
}
