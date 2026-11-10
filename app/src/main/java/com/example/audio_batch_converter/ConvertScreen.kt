package com.example.audio_batch_converter

import android.media.MediaMuxer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

//@Composable
//fun ConverterScreen() {
//
//    var inputFolder by remember {
//        mutableStateOf<Uri?>(null)
//    }
//
//    var outputFolder by remember {
//        mutableStateOf<Uri?>(null)
//    }
//
//    var selectedFormat by remember {
//        mutableStateOf(MediaMuxer.OutputFormat.MP3)
//    }
//
//    val inputPicker = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.OpenDocumentTree()
//    ) { uri ->
//        inputFolder = uri
//    }
//
//    val outputPicker = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.OpenDocumentTree()
//    ) { uri ->
//        outputFolder = uri
//    }
//
//    // 下面放置你的界面
//}