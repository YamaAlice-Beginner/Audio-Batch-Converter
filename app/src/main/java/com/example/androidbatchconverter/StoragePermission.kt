package com.example.androidbatchconverter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * 共享存储（真实路径）访问权限。
 *
 * - Android 11+（API 30+）：需要“所有文件访问”MANAGE_EXTERNAL_STORAGE，跳转系统授权页。
 * - Android 10 及以下（API < 30）：申请 WRITE_EXTERNAL_STORAGE（危险权限）。
 */
object StoragePermission {

    /** 是否已获得访问共享存储（真实路径）的权限。 */
    fun isGranted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }

    /** Android 11+ 打开“所有文件访问”授权页。 */
    fun openAllFilesAccessSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }
}
