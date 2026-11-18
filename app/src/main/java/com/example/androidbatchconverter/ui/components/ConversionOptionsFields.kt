package com.example.androidbatchconverter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidbatchconverter.R
import com.example.androidbatchconverter.model.ConversionOptions
import com.example.androidbatchconverter.model.RateControlMode

/**
 * 转换详细参数的编辑表单（项目详情页与“转换器默认配置”页共用）。
 */
@Composable
fun ConversionOptionsFields(
    options: ConversionOptions,
    onOptionsChange: (ConversionOptions) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OptionField(
            label = stringResource(R.string.opt_encoder),
            value = options.encoder,
            description = stringResource(R.string.opt_encoder_desc),
            onChange = { onOptionsChange(options.copy(encoder = it)) },
        )
        EnumDropdown(
            label = stringResource(R.string.opt_rate_control),
            selected = options.rateControlMode,
            options = RateControlMode.entries.map { it to stringResource(it.labelRes) },
            onSelect = { onOptionsChange(options.copy(rateControlMode = it)) },
            supportingText = stringResource(R.string.opt_rate_control_desc),
        )
        OptionField(
            label = stringResource(R.string.opt_bitrate),
            value = options.bitrate,
            description = stringResource(R.string.opt_bitrate_desc),
            onChange = { onOptionsChange(options.copy(bitrate = it)) },
        )
        OptionField(
            label = stringResource(R.string.opt_quality),
            value = options.quality,
            description = stringResource(R.string.opt_quality_desc),
            onChange = { onOptionsChange(options.copy(quality = it)) },
        )
        OptionField(
            label = stringResource(R.string.opt_sample_rate),
            value = options.sampleRate,
            description = stringResource(R.string.opt_sample_rate_desc),
            onChange = { onOptionsChange(options.copy(sampleRate = it)) },
        )
        OptionField(
            label = stringResource(R.string.opt_channels),
            value = options.channels,
            description = stringResource(R.string.opt_channels_desc),
            onChange = { onOptionsChange(options.copy(channels = it)) },
        )
        OptionField(
            label = stringResource(R.string.opt_preset),
            value = options.preset,
            description = stringResource(R.string.opt_preset_desc),
            onChange = { onOptionsChange(options.copy(preset = it)) },
        )
        OptionField(
            label = stringResource(R.string.opt_compression_level),
            value = options.compressionLevel,
            description = stringResource(R.string.opt_compression_level_desc),
            onChange = { onOptionsChange(options.copy(compressionLevel = it)) },
        )
        OptionField(
            label = stringResource(R.string.opt_profile),
            value = options.profile,
            description = stringResource(R.string.opt_profile_desc),
            onChange = { onOptionsChange(options.copy(profile = it)) },
        )
        OptionField(
            label = stringResource(R.string.opt_sample_format),
            value = options.sampleFormat,
            description = stringResource(R.string.opt_sample_format_desc),
            onChange = { onOptionsChange(options.copy(sampleFormat = it)) },
        )
        OptionField(
            label = stringResource(R.string.opt_channel_layout),
            value = options.channelLayout,
            description = stringResource(R.string.opt_channel_layout_desc),
            onChange = { onOptionsChange(options.copy(channelLayout = it)) },
        )
    }
}

@Composable
private fun OptionField(
    label: String,
    value: String,
    description: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        supportingText = { Text(description) },
        modifier = Modifier.fillMaxWidth(),
    )
}
