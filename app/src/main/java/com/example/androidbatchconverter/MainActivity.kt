package com.example.androidbatchconverter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.example.androidbatchconverter.model.AudioFormat
import com.example.androidbatchconverter.model.Project
import com.example.androidbatchconverter.ui.screens.ConverterDefaultsScreen
import com.example.androidbatchconverter.ui.screens.CreateProjectScreen
import com.example.androidbatchconverter.ui.screens.ProjectDetailScreen
import com.example.androidbatchconverter.ui.screens.ProjectListScreen
import com.example.androidbatchconverter.ui.screens.SettingsScreen
import com.example.androidbatchconverter.ui.theme.AndroidBatchConverterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val storageGranted = mutableStateOf(true)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppSettings.applyLocale(newBase))
    }

    override fun onResume() {
        super.onResume()
        storageGranted.value = StoragePermission.isGranted(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageGranted.value = StoragePermission.isGranted(this)
        enableEdgeToEdge()
        setContent {
            var settings by remember { mutableStateOf(AppSettings.load(this)) }

            val darkTheme = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // 让状态栏/导航栏图标颜色跟随亮暗主题。
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !darkTheme
                    controller.isAppearanceLightNavigationBars = !darkTheme
                }
            }

            AndroidBatchConverterTheme(darkTheme = darkTheme, dynamicColor = false) {
                App(
                    settings = settings,
                    storageGranted = storageGranted.value,
                    onRequestAllFilesAccess = { StoragePermission.openAllFilesAccessSettings(this) },
                    onSettingsChange = { newSettings ->
                        val languageChanged = settings.language != newSettings.language
                        settings = newSettings
                        AppSettings.save(this, newSettings)
                        if (languageChanged) {
                            recreate()
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.settings_saved),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }
    }
}

/** 应用内的页面。 */
private sealed interface Screen {
    data object ProjectList : Screen
    data object CreateProject : Screen
    data object Settings : Screen
    data object ConverterDefaults : Screen
    data class ProjectDetail(val projectId: Long) : Screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App(
    settings: Settings,
    storageGranted: Boolean,
    onRequestAllFilesAccess: () -> Unit,
    onSettingsChange: (Settings) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val workPlace = remember { defaultWorkPlace(context.applicationContext) }

    val projects = remember { mutableStateListOf<Project>() }
    var screen by remember { mutableStateOf<Screen>(Screen.ProjectList) }

    var searchMode by remember { mutableStateOf<SearchMode?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteProjectId by remember { mutableStateOf<Long?>(null) }

    fun reloadProjects() {
        scope.launch {
            val list = workPlace.listProjects()
            projects.clear()
            projects.addAll(list)
        }
    }

    LaunchedEffect(Unit) { reloadProjects() }

    // 系统返回手势：子页面先退回列表；列表页不拦截，走系统默认（退出到桌面）。
    BackHandler(enabled = screen != Screen.ProjectList) {
        screen = Screen.ProjectList
    }

    // 存储权限门控：读写真实路径（如 /storage/emulated/0/...）需要。
    val requestLegacyStorage = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* 结果由 onResume 统一刷新 storageGranted */ }

    var storageDialogDismissed by remember { mutableStateOf(false) }
    if (!storageGranted && !storageDialogDismissed) {
        AlertDialog(
            onDismissRequest = { storageDialogDismissed = true },
            title = { Text(stringResource(R.string.storage_permission_title)) },
            text = { Text(stringResource(R.string.storage_permission_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            onRequestAllFilesAccess()
                        } else {
                            requestLegacyStorage.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    },
                ) {
                    Text(stringResource(R.string.grant))
                }
            },
            dismissButton = {
                TextButton(onClick = { storageDialogDismissed = true }) {
                    Text(stringResource(R.string.later))
                }
            },
        )
    }

    // 删除项目确认对话框。
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        val id = pendingDeleteProjectId
                        if (id != null) {
                            scope.launch {
                                workPlace.deleteProject(id)
                                reloadProjects()
                                screen = Screen.ProjectList
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    val filteredProjects = searchMode?.let { mode ->
        filterProjects(projects, mode, searchQuery)
    } ?: projects.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (val current = screen) {
                        Screen.ProjectList -> stringResource(R.string.title_main)
                        Screen.CreateProject -> stringResource(R.string.title_create_project)
                        Screen.Settings -> stringResource(R.string.title_settings)
                        Screen.ConverterDefaults -> stringResource(R.string.title_converter_defaults)
                        is Screen.ProjectDetail -> projects.find { it.id == current.projectId }?.title
                            ?: stringResource(R.string.title_project_detail)
                    }
                    Text(title)
                },
                navigationIcon = {
                    if (screen != Screen.ProjectList) {
                        IconButton(onClick = { screen = Screen.ProjectList }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    if (screen == Screen.ProjectList) {
                        // 主列表页：下拉菜单（搜索 + 设置）。
                        TopBarMenu(
                            onSearchByName = {
                                searchMode = SearchMode.NAME
                                screen = Screen.ProjectList
                            },
                            onSearchByPath = {
                                searchMode = SearchMode.PATH
                                screen = Screen.ProjectList
                            },
                            onSearchByRegex = {
                                searchMode = SearchMode.REGEX
                                screen = Screen.ProjectList
                            },
                            onOpenSettings = { screen = Screen.Settings },
                        )
                    } else if (screen is Screen.ProjectDetail) {
                        // 项目详情页：右上角删除图标。
                        IconButton(
                            onClick = {
                                pendingDeleteProjectId = (screen as Screen.ProjectDetail).projectId
                                showDeleteDialog = true
                            },
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete_project),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            if (screen == Screen.ProjectList) {
                FloatingActionButton(onClick = { screen = Screen.CreateProject }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_project))
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    val zoomIn = targetState is Screen.ProjectDetail ||
                        targetState == Screen.CreateProject
                    if (zoomIn) {
                        (fadeIn(animationSpec = tween(220)) +
                            scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                            ))
                            .togetherWith(fadeOut(animationSpec = tween(160)))
                    } else {
                        fadeIn(animationSpec = tween(220))
                            .togetherWith(fadeOut(animationSpec = tween(220)))
                    }
                },
                label = "screenTransition",
            ) { current ->
                when (current) {
                    Screen.ProjectList -> ProjectListScreen(
                        projects = filteredProjects,
                        searchMode = searchMode,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onClearSearch = {
                            searchMode = null
                            searchQuery = ""
                        },
                        onOpenProject = { project -> screen = Screen.ProjectDetail(project.id) },
                    )

                    Screen.CreateProject -> CreateProjectScreen(
                        defaultInputFolder = settings.defaultInputFolder,
                        defaultOutputFolder = settings.defaultOutputFolder,
                        rememberLastPath = settings.rememberLastPath,
                        isTitleUnique = { title -> !workPlace.titleExists(title) },
                        onCreate = { title, input, output ->
                            scope.launch {
                                workPlace.createProject(
                                    title, input, output,
                                    AudioFormat.MP3,
                                    settings.conversionOptions,
                                )
                                reloadProjects()
                                screen = Screen.ProjectList
                            }
                        },
                    )

                    Screen.Settings -> SettingsScreen(
                        settings = settings,
                        onChange = onSettingsChange,
                        onOpenConverterDefaults = { screen = Screen.ConverterDefaults },
                    )

                    Screen.ConverterDefaults -> ConverterDefaultsScreen(
                        options = settings.conversionOptions,
                        onSave = { opts ->
                            onSettingsChange(settings.copy(conversionOptions = opts))
                            screen = Screen.Settings
                        },
                    )

                    is Screen.ProjectDetail -> {
                        val project = projects.find { it.id == current.projectId }
                        if (project == null) {
                            Text(stringResource(R.string.project_not_found))
                        } else {
                            ProjectDetailScreen(
                                project = project,
                                listFiles = { folder -> workPlace.scanFolder(folder) },
                                onSaveSelection = { uris ->
                                    scope.launch {
                                        workPlace.setSelectedAudio(project.id, uris)
                                        reloadProjects()
                                    }
                                },
                                onSaveParams = { format, opts ->
                                    scope.launch {
                                        workPlace.updateProject(
                                            project.copy(outputFormat = format, options = opts),
                                        )
                                        reloadProjects()
                                    }
                                },
                                onStartConversion = {
                                    scope.launch {
                                        val result = workPlace.runConversion(project)
                                        val message = conversionMessage(context, result)
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 把转换结果转成提示文本（非 Composable，供协程内使用）。 */
private fun conversionMessage(context: Context, result: Result<Int>): String =
    result.fold(
        onSuccess = { context.getString(R.string.conversion_done, it) },
        onFailure = { context.getString(R.string.conversion_failed, it.message ?: "") },
    )

/** 顶栏右上角下拉菜单：三种搜索方式 + 设置。 */
@Composable
private fun TopBarMenu(
    onSearchByName: () -> Unit,
    onSearchByPath: () -> Unit,
    onSearchByRegex: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.menu_more))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.search_by_name)) },
                onClick = {
                    expanded = false
                    onSearchByName()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.search_by_path)) },
                onClick = {
                    expanded = false
                    onSearchByPath()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.search_by_regex)) },
                onClick = {
                    expanded = false
                    onSearchByRegex()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings)) },
                onClick = {
                    expanded = false
                    onOpenSettings()
                },
            )
        }
    }
}
