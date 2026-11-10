package com.example.audio_batch_converter

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import com.example.audio_batch_converter.ui.theme.AudioBatchConverterTheme
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private var inputFolderUri = mutableStateOf<Uri?>(null)
    private var outputFolderUri = mutableStateOf<Uri?>(null)

    private val inputFilePicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            if (uri != null) {
                inputFolderUri.value = uri
                Log.d("FileManager", "选择的文件夹：$uri")
            }
        }
    private val outputFilePicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            if (uri != null) {
                outputFolderUri.value = uri
                Log.d("FileManager", "选择的文件夹：$uri")
            }
        }
        @OptIn(ExperimentalMaterial3Api::class)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                AudioBatchConverterTheme {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                        TopAppBar(
                            title = {
                                Text("Audio Batch Converter", maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        )
                    }
                    )

                    { innerPadding ->
                        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("选择输入文件夹", Modifier.padding(start = 16.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            GetInputFolder()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("选择输出文件夹", Modifier.padding(start = 16.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            GetOutputFolder()
                            Spacer(modifier = Modifier.height(16.dp))
                            FormatSelector()
                            StartConvert()
                        }
                    }
                }
            }
        }

    // 这个函数用作选择输入文件夹
    @Composable
    fun GetInputFolder() {
        AudioBatchConverterTheme() {
            Row(
                Modifier
                    .clickable(onClick = {
                        inputFilePicker.launch(null)
                    })

                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
            )
            {
                Column(
                    Modifier.padding(16.dp)
                ) {
                    Text(
                        text = inputFolderUri.value?.toString()
                            ?: "尚未选择文件夹"
                    )
                }
            }
        }
    }

    // 这个函数用来选择输出文件夹
    @Composable
    fun GetOutputFolder() {
        AudioBatchConverterTheme() {
            Row(
                Modifier
                    .clickable(onClick = {
                        outputFilePicker.launch(null)
                    })
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
            )
            {
                Column(
                    Modifier.padding(16.dp)
                ) {
                    Text(
                        text = outputFolderUri.value?.toString()
                            ?: "尚未选择文件夹"
                    )
                }
            }
        }
    }

    // 这个函数用于选择输出的文件格式
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FormatSelector() {

        val options = listOf(".mp3", ".flac", ".wav", ".ogg")

        var selectedOption by remember {
            mutableStateOf(".mp3")
        }

        var expanded by remember {
            mutableStateOf(false)
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {

            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("输出格式") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {

                options.forEach { option ->

                    DropdownMenuItem(
                        text = {
                            Text(option)
                        },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    //这个函数是开始转换按钮
    @Composable
    fun StartConvert() {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {

                }
            ) {
                Text("开始转换")
            }
        }

    }
}
