package com.example.androidbatchconverter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidbatchconverter.R
import com.example.androidbatchconverter.ui.components.FolderField
import kotlinx.coroutines.launch

/**
 * 创建项目页：编辑项目标题、输入文件夹位置、输出文件夹位置。
 * 输入/输出文件夹会预填设置页里保存的默认值；项目标题做唯一性检测。
 */
@Composable
fun CreateProjectScreen(
    defaultInputFolder: String,
    defaultOutputFolder: String,
    rememberLastPath: Boolean,
    isTitleUnique: suspend (title: String) -> Boolean,
    onCreate: (title: String, inputFolder: String, outputFolder: String) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var title by rememberSaveable { mutableStateOf("") }
    var inputFolder by rememberSaveable { mutableStateOf(defaultInputFolder) }
    var outputFolder by rememberSaveable { mutableStateOf(defaultOutputFolder) }

    var titleDuplicate by remember { mutableStateOf(false) }
    var checkingTitle by remember { mutableStateOf(false) }

    val canCreate = title.isNotBlank() && inputFolder.isNotBlank() && outputFolder.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.create_form_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                if (titleDuplicate) titleDuplicate = false
            },
            label = { Text(stringResource(R.string.project_title_label)) },
            singleLine = true,
            isError = titleDuplicate,
            modifier = Modifier.fillMaxWidth(),
        )
        if (titleDuplicate) {
            Text(
                text = stringResource(R.string.title_duplicate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(12.dp))

        FolderField(
            value = inputFolder,
            onValueChange = { inputFolder = it },
            label = stringResource(R.string.input_folder_label),
            rememberLastPath = rememberLastPath,
        )
        Spacer(Modifier.height(12.dp))

        FolderField(
            value = outputFolder,
            onValueChange = { outputFolder = it },
            label = stringResource(R.string.output_folder_label),
            rememberLastPath = rememberLastPath,
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val newTitle = title.trim()
                scope.launch {
                    checkingTitle = true
                    val unique = isTitleUnique(newTitle)
                    checkingTitle = false
                    if (!unique) {
                        titleDuplicate = true
                    } else {
                        onCreate(newTitle, inputFolder.trim(), outputFolder.trim())
                    }
                }
            },
            enabled = canCreate && !checkingTitle,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.create))
        }
    }
}
