package com.example.androidbatchconverter.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.androidbatchconverter.AppSettings
import com.example.androidbatchconverter.R

/**
 * 带“浏览”按钮的文件夹输入框，创建项目页与设置页共用。
 * 支持“记住上一个路径”：勾选后，下次打开选择器会定位到上次选择的文件夹。
 */
@Composable
fun FolderField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    rememberLastPath: Boolean,
) {
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            onValueChange(uri.toString())
            if (rememberLastPath) {
                AppSettings.saveLastFolder(context, uri)
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            TextButton(
                onClick = {
                    val initial = if (rememberLastPath) {
                        AppSettings.lastFolder(context)?.let(Uri::parse)
                    } else {
                        null
                    }
                    picker.launch(initial)
                },
            ) {
                Text(stringResource(R.string.browse))
            }
        },
    )
}
