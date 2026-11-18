package com.example.androidbatchconverter.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidbatchconverter.Language
import com.example.androidbatchconverter.R
import com.example.androidbatchconverter.Settings
import com.example.androidbatchconverter.ThemeMode
import com.example.androidbatchconverter.ui.components.EnumDropdown
import com.example.androidbatchconverter.ui.components.FolderField

/**
 * 设置页：默认输入/输出文件夹、记住上一个路径、转换器默认配置、主题、语言。
 */
@Composable
fun SettingsScreen(
    settings: Settings,
    onChange: (Settings) -> Unit,
    onOpenConverterDefaults: () -> Unit,
) {
    var defaultInputFolder by remember { mutableStateOf(settings.defaultInputFolder) }
    var defaultOutputFolder by remember { mutableStateOf(settings.defaultOutputFolder) }
    var themeMode by remember { mutableStateOf(settings.themeMode) }
    var language by remember { mutableStateOf(settings.language) }
    var rememberLastPath by remember { mutableStateOf(settings.rememberLastPath) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // 默认文件夹
        Text(
            text = stringResource(R.string.settings_default_folders),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        FolderField(
            value = defaultInputFolder,
            onValueChange = { defaultInputFolder = it },
            label = stringResource(R.string.settings_default_input),
            rememberLastPath = rememberLastPath,
        )
        Spacer(Modifier.height(8.dp))
        FolderField(
            value = defaultOutputFolder,
            onValueChange = { defaultOutputFolder = it },
            label = stringResource(R.string.settings_default_output),
            rememberLastPath = rememberLastPath,
        )

        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { rememberLastPath = !rememberLastPath }
                .padding(vertical = 4.dp),
        ) {
            Checkbox(
                checked = rememberLastPath,
                onCheckedChange = { rememberLastPath = it },
            )
            Text(stringResource(R.string.remember_last_path))
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // 转换器详细参数默认配置（点击进入详情页）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenConverterDefaults)
                .padding(vertical = 12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.converter_defaults),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.converter_defaults_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider()
        Spacer(Modifier.height(24.dp))

        // 主题
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        EnumDropdown(
            label = stringResource(R.string.settings_theme),
            selected = themeMode,
            options = ThemeMode.entries.map { it to stringResource(it.labelRes) },
            onSelect = { themeMode = it },
        )

        Spacer(Modifier.height(16.dp))

        // 语言
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        EnumDropdown(
            label = stringResource(R.string.settings_language),
            selected = language,
            options = Language.entries.map { it to stringResource(it.labelRes) },
            onSelect = { language = it },
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onChange(
                    settings.copy(
                        defaultInputFolder = defaultInputFolder.trim(),
                        defaultOutputFolder = defaultOutputFolder.trim(),
                        themeMode = themeMode,
                        language = language,
                        rememberLastPath = rememberLastPath,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save))
        }
    }
}
