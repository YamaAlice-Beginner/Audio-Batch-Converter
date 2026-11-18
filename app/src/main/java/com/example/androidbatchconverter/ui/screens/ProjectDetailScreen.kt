package com.example.androidbatchconverter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidbatchconverter.R
import com.example.androidbatchconverter.model.AudioFile
import com.example.androidbatchconverter.model.AudioFormat
import com.example.androidbatchconverter.model.ConversionOptions
import com.example.androidbatchconverter.model.Project
import com.example.androidbatchconverter.ui.components.ConversionOptionsFields
import com.example.androidbatchconverter.ui.components.EnumDropdown
import kotlinx.coroutines.launch

/**
 * 项目详情页：从输入文件夹内选择文件、设置输出格式与转换详细参数、开始转换。
 */
@Composable
fun ProjectDetailScreen(
    project: Project,
    listFiles: suspend (inputFolder: String) -> Result<List<AudioFile>>,
    onSaveSelection: (List<String>) -> Unit,
    onSaveParams: (AudioFormat, ConversionOptions) -> Unit,
    onStartConversion: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var selectedUris by remember(project.id) { mutableStateOf(project.selectedAudioUris) }
    var outputFormat by remember(project.id) { mutableStateOf(project.outputFormat) }
    var options by remember(project.id) { mutableStateOf(project.options) }
    var detailedExpanded by remember { mutableStateOf(false) }

    // 输入文件夹内的文件浏览
    var folderFiles by remember(project.id) { mutableStateOf<List<AudioFile>>(emptyList()) }
    var showBrowser by remember { mutableStateOf(false) }
    var browsing by remember { mutableStateOf(false) }
    var browseError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = project.title,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(8.dp))
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

        Spacer(Modifier.height(16.dp))

        // 输出格式
        EnumDropdown(
            label = stringResource(R.string.output_format),
            selected = outputFormat,
            options = AudioFormat.entries.map { it to it.name },
            onSelect = { outputFormat = it },
        )

        Spacer(Modifier.height(8.dp))

        // 详细参数（可折叠）
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { detailedExpanded = !detailedExpanded }
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.detailed_params),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (detailedExpanded) "▾" else "▸",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (detailedExpanded) {
            ConversionOptionsFields(options = options, onOptionsChange = { options = it })
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = { onSaveParams(outputFormat, options) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save_params))
        }
        HorizontalDivider()

        Spacer(Modifier.height(16.dp))

        // 输入文件夹内的文件
        Text(
            text = stringResource(R.string.audio_files),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))

        if (selectedUris.isEmpty()) {
            Text(stringResource(R.string.no_files_selected))
        } else {
            Text(stringResource(R.string.selected_files_count, selectedUris.size))
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        browsing = true
                        browseError = null
                        listFiles(project.inputFolder).fold(
                            onSuccess = {
                                folderFiles = it
                                showBrowser = true
                            },
                            onFailure = { browseError = it.message },
                        )
                        browsing = false
                    }
                },
            ) {
                Text(stringResource(R.string.select_from_folder))
            }
            if (selectedUris.isNotEmpty()) {
                TextButton(onClick = { selectedUris = emptyList() }) {
                    Text(stringResource(R.string.use_all_files))
                }
            }
        }

        browseError?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (showBrowser) {
            Spacer(Modifier.height(8.dp))
            if (folderFiles.isEmpty()) {
                Text(stringResource(R.string.no_media_found))
            } else {
                folderFiles.forEach { file ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedUris = if (selectedUris.contains(file.uri)) {
                                    selectedUris.filterNot { it == file.uri }
                                } else {
                                    selectedUris + file.uri
                                }
                            }
                            .padding(vertical = 4.dp),
                    ) {
                        Checkbox(
                            checked = selectedUris.contains(file.uri),
                            onCheckedChange = null,
                        )
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { showBrowser = false }) {
                Text(stringResource(R.string.done))
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { onSaveSelection(selectedUris) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save_selection))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onStartConversion,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.start_convert))
        }
    }
}
