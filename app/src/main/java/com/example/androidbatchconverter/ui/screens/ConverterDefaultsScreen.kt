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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidbatchconverter.R
import com.example.androidbatchconverter.model.ConversionOptions
import com.example.androidbatchconverter.ui.components.ConversionOptionsFields

/**
 * 转换器详细参数默认配置页（从设置页进入）。
 */
@Composable
fun ConverterDefaultsScreen(
    options: ConversionOptions,
    onSave: (ConversionOptions) -> Unit,
) {
    var opts by remember { mutableStateOf(options) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.converter_defaults),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(16.dp))

        ConversionOptionsFields(options = opts, onOptionsChange = { opts = it })

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSave(opts) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save))
        }
    }
}
