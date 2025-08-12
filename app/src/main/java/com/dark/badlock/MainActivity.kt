package com.dark.badlock

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.dark.badlock.ui.theme.BadlockTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// --- UI THEME - FUTURISTIC & MODERN ---
val DarkBackground = Color(0xFF10121A)
val DarkSurface = Color(0xFF1C1E28)
val PrimaryAccent = Color(0xFF8A2BE2) // Electric Blue-Violet
val GreenAccent = Color(0xFF00FFA3) // Neon Mint
val UpdateYellow = Color(0xFFFFD600) // Vibrant Yellow
val TextPrimary = Color.White.copy(alpha = 0.9f)
val TextSecondary = Color.White.copy(alpha = 0.7f)


// --- DATA & STATE CLASSES ---
data class ModuleInfo(
    val name: String,
    val packageName: String,
    val category: String,
    val apkMirrorMainPage: String
)

data class InstalledModule(
    val name: String,
    val packageName: String,
    val versionName: String?,
    val latestVersion: String?,
    val mainActivity: String?,
    val isInstalled: Boolean,
    val category: String,
    val apkMirrorMainPage: String,
    val isUpdateAvailable: Boolean,
    val iconResId: Int?
)

sealed interface ModuleState {
    object Loading : ModuleState
    data class Success(val modules: Map<String, List<InstalledModule>>) : ModuleState
    data class Error(val message: String) : ModuleState
}


// --- MODULE DEFINITIONS ---
object GoodLockModules {
    // Unchanged
    val modules = listOf(
        ModuleInfo("Home Up", "com.samsung.android.app.homestar", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/home-up/"),
        ModuleInfo("LockStar", "com.samsung.systemui.lockstar", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/lockstar/"),
        ModuleInfo("MultiStar", "com.samsung.android.multistar", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/samsung-multistar/"),
        ModuleInfo("QuickStar", "com.samsung.android.qstuner", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/quickstar/"),
        ModuleInfo("SoundAssistant", "com.samsung.android.soundassistant", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/soundassistant/"),
        ModuleInfo("Keys Cafe", "com.samsung.android.keyscafe", "Make up", "https://www.apkmirror.com/apk/good-lock-labs/keys-cafe/"),
        ModuleInfo("Theme Park", "com.samsung.android.themedesigner", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/samsung-theme-park/"),
        ModuleInfo("Nice Shot", "com.samsung.android.app.captureplugin", "Make up", "https://www.apkmirror.com/apk/samsung-electronics/nice-shot/"),
        ModuleInfo("Wonderland", "com.samsung.android.wonderland.wallpaper", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/wonderland/"),
        ModuleInfo("Pentastic", "com.samsung.android.pentastic", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/pentastic/"),
        ModuleInfo("Clockface", "com.samsung.android.app.clockface", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/samsung-clockface/"),
        ModuleInfo("Edge lighting+", "com.samsung.android.edgelightingplus", "Make up", "https://www.apkmirror.com/apk/good-lock-labs/edge-lighting/"),
        ModuleInfo("Edge touch", "com.samsung.android.app.edgetouch", "Make up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/edge-touch/"),
        ModuleInfo("Display Assistant", "com.samsung.android.displayassistant", "Make up", "https://www.apkmirror.com/apk/galaxy-labs/display-assistant-beta/"),
        ModuleInfo("Routines+", "com.samsung.android.app.routineplus", "Life up", "https://www.apkmirror.com/apk/good-lock-labs/samsung-routine/"),
        ModuleInfo("RegiStar", "com.samsung.android.app.galaxyregistry", "Life up", "https://www.apkmirror.com/apk/good-lock-labs/registar/"),
        ModuleInfo("Camera Assistant", "com.samsung.android.app.cameraassistant", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/camera-assistant/"),
        ModuleInfo("Nice Catch", "com.samsung.android.app.goodcatch", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/nice-catch/"),
        ModuleInfo("Good Lock", "com.samsung.android.goodlock", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/good-lock-2018/"),
        ModuleInfo("Battery Guardian", "com.samsung.android.statsd", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/battery-guardian/"),
        ModuleInfo("File Guardian", "com.android.samsung.icebox", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/file-guardian/"),
        ModuleInfo("Memory Guardian", "com.samsung.android.memoryguardian", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/memory-guardian/"),
        ModuleInfo("App Booster", "com.samsung.android.appbooster", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/app-booster/"),
        ModuleInfo("Thermal Guardian", "com.samsung.android.thermalguardian", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/thermal-guardian/"),
        ModuleInfo("Media File Guardian", "com.samsung.android.mediaguardian", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/media-file-guardian/"),
        ModuleInfo("One Hand Operation+", "com.samsung.android.sidegesturepad", "Life up", "https://www.apkmirror.com/apk/samsung-electronics-co-ltd-co-ltd/one-hand-operation/"),
    )
}

// --- CORE LOGIC ---

suspend fun fetchLatestVersionFromRssFeed(url: String): String? {
    val feedUrl = if (url.endsWith("/")) "${url}feed/" else "$url/feed/"
    return withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(feedUrl).timeout(15000).get()
            val title = doc.selectFirst("item > title")?.text()
            if (title.isNullOrEmpty()) return@withContext null
            val regex = """\s(\d+(\.\d+)+(\.\d+)*)""".toRegex()
            val matchResult = regex.find(title)
            matchResult?.groups?.get(1)?.value?.trim()
        } catch (e: Exception) {
            throw e
        }
    }
}

fun isUpdateAvailable(moduleName: String, installedVersion: String?, latestVersion: String?): Boolean {
    if (installedVersion.isNullOrEmpty() || latestVersion.isNullOrEmpty()) return false
    try {
        val installedParts = installedVersion.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }
        val latestParts = latestVersion.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }
        val maxParts = maxOf(installedParts.size, latestParts.size)
        for (i in 0 until maxParts) {
            val installed = installedParts.getOrElse(i) { 0 }
            val latest = latestParts.getOrElse(i) { 0 }
            if (latest > installed) return true
            if (latest < installed) return false
        }
    } catch (e: Exception) {
        return false
    }
    return false
}

suspend fun loadData(context: Context): ModuleState {
    val packageManager = context.packageManager
    return withContext(Dispatchers.IO) {
        try {
            val allModules = coroutineScope {
                GoodLockModules.modules.map { moduleInfo ->
                    async {
                        val latestVersion = try {
                            fetchLatestVersionFromRssFeed(moduleInfo.apkMirrorMainPage)
                        } catch (e: Exception) { null }

                        val isInstalled = try {
                            packageManager.getPackageInfo(moduleInfo.packageName, 0); true
                        } catch (e: Exception) { false }

                        var installedVersion: String? = null
                        var mainActivity: String? = null
                        if (isInstalled) {
                            try {
                                val pkgInfo = packageManager.getPackageInfo(moduleInfo.packageName, PackageManager.GET_ACTIVITIES)
                                installedVersion = pkgInfo.versionName
                                mainActivity = pkgInfo.activities?.firstOrNull()?.name
                            } catch (e: Exception) { /* Silently fail */ }
                        }

                        val resourceName = moduleInfo.name.lowercase().replace(" ", "_").replace("+", "")
                        val iconResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName).let { if (it == 0) null else it }

                        InstalledModule(
                            name = moduleInfo.name,
                            packageName = moduleInfo.packageName,
                            versionName = installedVersion,
                            latestVersion = latestVersion,
                            mainActivity = mainActivity,
                            isInstalled = isInstalled,
                            category = moduleInfo.category,
                            apkMirrorMainPage = moduleInfo.apkMirrorMainPage,
                            isUpdateAvailable = isUpdateAvailable(moduleInfo.name, installedVersion, latestVersion),
                            iconResId = iconResId
                        )
                    }
                }.map { it.await() }
            }

            val groupedAndSorted = allModules.groupBy { it.category }
                .mapValues { entry ->
                    entry.value.sortedWith(
                        compareByDescending<InstalledModule> { it.isUpdateAvailable }
                            .thenByDescending { it.isInstalled }
                            .thenBy { it.name }
                    )
                }
            ModuleState.Success(groupedAndSorted)
        } catch (e: Exception) {
            when(e) {
                is UnknownHostException, is SocketTimeoutException -> ModuleState.Error("Could not connect to server. Please check your internet connection.")
                else -> ModuleState.Error("An unexpected error occurred.")
            }
        }
    }
}

// --- MAIN ACTIVITY & UI ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        setContent {
            BadlockTheme {
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        window.statusBarColor = Color.Transparent.toArgb()
                        window.navigationBarColor = Color.Transparent.toArgb()
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                    }
                }
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var moduleState by remember { mutableStateOf<ModuleState>(ModuleState.Loading) }

    fun refreshData() {
        coroutineScope.launch {
            moduleState = ModuleState.Loading
            moduleState = loadData(context)
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    // OPTIMIZATION: Create stable, remembered lambdas for all actions.
    // This prevents them from being recreated for every item during scroll,
    // which is the primary cause of jank.
    val onModuleClick = remember<(InstalledModule) -> Unit> {
        { module ->
            if (module.isInstalled) launchModule(context, module)
            else openUrl(context, module.apkMirrorMainPage)
        }
    }
    val onWebsiteClick = remember<(String) -> Unit> {
        { url -> openUrl(context, url) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Badlock", fontWeight = FontWeight.Bold, color = TextPrimary) },
                actions = {
                    IconButton(onClick = { refreshData() }, enabled = moduleState != ModuleState.Loading) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = moduleState) {
                is ModuleState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryAccent)
                    }
                }
                is ModuleState.Error -> {
                    ErrorScreen(errorMessage = state.message, onRetry = { refreshData() })
                }
                is ModuleState.Success -> {
                    val updatableModules = remember(state.modules) {
                        state.modules.values.flatten().filter { it.isUpdateAvailable }
                    }
                    val tabs = listOf("Make up", "Life up", "Updates")
                    val pagerState = rememberPagerState(pageCount = { tabs.size })

                    Column {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f),
                            beyondBoundsPageCount = 1
                        ) { page ->
                            val pageTitle = tabs[page]
                            val modulesToShow = when (pageTitle) {
                                "Updates" -> updatableModules
                                else -> state.modules[pageTitle] ?: emptyList()
                            }
                            // OPTIMIZATION: Pass the stable lambdas to the list.
                            ModuleList(
                                modules = modulesToShow,
                                showEmptyMessage = (pageTitle == "Updates"),
                                onModuleClick = onModuleClick,
                                onWebsiteClick = onWebsiteClick
                            )
                        }

                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = DarkBackground,
                            indicator = {},
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = pagerState.currentPage == index
                                val tabColor by animateColorAsState(
                                    targetValue = if (isSelected) DarkSurface else Color.Transparent,
                                    animationSpec = tween(durationMillis = 300)
                                )

                                Tab(
                                    selected = isSelected,
                                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                                    modifier = Modifier
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(tabColor),
                                    text = {
                                        if (title == "Updates" && updatableModules.isNotEmpty()) {
                                            BadgedBox(
                                                badge = { Badge(containerColor = PrimaryAccent) { Text("${updatableModules.size}") } }
                                            ) { Text(title, fontWeight = FontWeight.SemiBold) }
                                        } else {
                                            Text(title, fontWeight = FontWeight.SemiBold)
                                        }
                                    },
                                    icon = {
                                        val icon = when(title) {
                                            "Updates" -> Icons.Default.SystemUpdate
                                            "Make up" -> Icons.Default.Palette
                                            else -> Icons.Default.Style
                                        }
                                        Icon(icon, contentDescription = title)
                                    },
                                    selectedContentColor = PrimaryAccent,
                                    unselectedContentColor = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// OPTIMIZATION: ModuleList now accepts the stable lambdas to pass down.
@Composable
fun ModuleList(
    modules: List<InstalledModule>,
    showEmptyMessage: Boolean = false,
    onModuleClick: (InstalledModule) -> Unit,
    onWebsiteClick: (String) -> Unit
) {
    if (modules.isEmpty() && showEmptyMessage) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = "All up to date",
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("All Clear!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("All your modules are up-to-date.", color = TextSecondary, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = modules, key = { it.packageName }) { module ->
                ModuleCard(
                    module = module,
                    onModuleClick = { onModuleClick(module) },
                    onWebsiteClick = { onWebsiteClick(module.apkMirrorMainPage) }
                )
            }
        }
    }
}

// OPTIMIZATION: ModuleCard now accepts stable lambdas instead of creating its own.
@Composable
fun ModuleCard(
    module: InstalledModule,
    onModuleClick: () -> Unit,
    onWebsiteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onModuleClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(brush = Brush.verticalGradient(colors = listOf(PrimaryAccent.copy(alpha = 0.3f), Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = module.iconResId?.let { painterResource(id = it) } ?: painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "${module.name} icon",
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(module.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                VersionInfo(module)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (module.isInstalled) {
                    if (module.isUpdateAvailable) {
                        Button(
                            onClick = onWebsiteClick,
                            colors = ButtonDefaults.buttonColors(containerColor = UpdateYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) { Text("Update", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    }
                } else {
                    Button(
                        onClick = onWebsiteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) { Text("Install", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                }
                IconButton(onClick = onWebsiteClick) {
                    Icon(Icons.Default.Public, contentDescription = "Go to Website", tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun VersionInfo(module: InstalledModule) {
    val versionText = if (module.isInstalled) "v${module.versionName ?: "N/A"}" else "Not Installed"
    Text(versionText, color = TextSecondary, fontSize = 12.sp, maxLines = 1)

    if (module.latestVersion != null) {
        val color = if (module.isUpdateAvailable) UpdateYellow else TextSecondary
        Text("Latest: v${module.latestVersion}", color = color, fontSize = 12.sp, maxLines = 1)
    } else if (module.isInstalled) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudOff, contentDescription = "Error fetching version", tint = TextSecondary, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text("Update check failed", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun ErrorScreen(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SignalWifiOff,
            contentDescription = "Connection Error",
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Connection Error", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(errorMessage, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)) {
            Text("Retry")
        }
    }
}


// --- UTILITY FUNCTIONS ---

fun launchModule(context: Context, module: InstalledModule) {
    try {
        module.mainActivity?.let {
            context.startActivity(Intent().apply {
                component = ComponentName(module.packageName, it)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } ?: Toast.makeText(context, "${module.name} cannot be opened directly.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Could not launch ${module.name}.", Toast.LENGTH_SHORT).show()
    }
}

fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No browser found.", Toast.LENGTH_SHORT).show()
    }
}
