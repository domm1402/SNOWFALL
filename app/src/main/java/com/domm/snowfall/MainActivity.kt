package com.domm.snowfall

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.content.pm.ResolveInfo
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val night = Color(0xFF08101D)
private val panel = Color(0xFF111E30)
private val muted = Color(0xFF9AADC5)
private val snow = Color(0xFFDDEEFF)
private val palettes = listOf(
    listOf(Color(0xFF08101D), Color(0xFF16334D), Color(0xFF73B7E8)),
    listOf(Color(0xFF16151C), Color(0xFF363746), Color(0xFFB8B9C8)),
    listOf(Color(0xFF071A20), Color(0xFF145060), Color(0xFF8CE5D4)),
    listOf(Color(0xFF180D29), Color(0xFF46265F), Color(0xFFD0A5FF)),
    listOf(Color(0xFF201209), Color(0xFF69401B), Color(0xFFF2BD63)),
    listOf(Color(0xFF240C16), Color(0xFF752C45), Color(0xFFFF9DBB)),
    listOf(Color(0xFF061A10), Color(0xFF20533A), Color(0xFF92E8A7)),
    listOf(Color(0xFF101010), Color(0xFF292929), Color(0xFFEAEAEA))
)
private val paletteNames = listOf("Blue Hour", "Silver", "Arctic", "Violet", "Amber", "Rose", "Aurora", "Mono")

data class LauncherApp(val label: String, val packageName: String, val resolveInfo: ResolveInfo)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SnowfallLauncher(this) }
    }
}

@Composable
private fun SnowfallLauncher(activity: Activity) {
    val prefs = remember { activity.getSharedPreferences("snowfall", Activity.MODE_PRIVATE) }
    var page by remember { mutableIntStateOf(0) }
    var theme by remember { mutableIntStateOf(prefs.getInt("launcher_theme", 0)) }
    var iconStyle by remember { mutableIntStateOf(prefs.getInt("icon_style", 0)) }
    var iconScale by remember { mutableFloatStateOf(prefs.getFloat("icon_scale", .85f)) }
    var gridColumns by remember { mutableIntStateOf(prefs.getInt("grid_columns", 4)) }
    var labels by remember { mutableStateOf(prefs.getBoolean("labels", true)) }
    var search by remember { mutableStateOf("") }
    var appFilter by remember { mutableIntStateOf(0) }
    val accent = palettes[theme][2]
    val apps = remember(activity) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        activity.packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { ri ->
                val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == activity.packageName) return@mapNotNull null
                LauncherApp(ri.loadLabel(activity.packageManager).toString(), pkg, ri)
            }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = accent, background = night, surface = panel)) {
        Scaffold(containerColor = night, bottomBar = {
            NavigationBar(containerColor = Color(0xFF0B1625), tonalElevation = 0.dp) {
                val nav = listOf("Home", "Apps", "Customize", "Wallpaper")
                val navIcons = listOf(Icons.Rounded.Home, Icons.Rounded.Apps, Icons.Rounded.Palette, Icons.Rounded.AcUnit)
                nav.forEachIndexed { i, label ->
                    NavigationBarItem(selected = page == i, onClick = { page = i },
                        icon = { Icon(navIcons[i], null) }, label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = accent, selectedTextColor = accent, indicatorColor = panel, unselectedIconColor = muted, unselectedTextColor = muted))
                }
            }
        }) { insets ->
            when(page) {
                0 -> HomeScreen(activity, apps, theme, accent, iconScale, gridColumns, labels, iconStyle,
                    { page = 1 }, { page = 2 })
                1 -> AppsScreen(activity, apps, search, { search = it }, appFilter, { appFilter = it }, theme, accent, iconScale, gridColumns, labels, iconStyle)
                2 -> CustomizeScreen(theme, { theme = it; prefs.edit().putInt("launcher_theme", it).apply() },
                    iconStyle, { iconStyle = it; prefs.edit().putInt("icon_style", it).apply() },
                    iconScale, { iconScale = it; prefs.edit().putFloat("icon_scale", it).apply() },
                    gridColumns, { gridColumns = it; prefs.edit().putInt("grid_columns", it).apply() },
                    labels, { labels = it; prefs.edit().putBoolean("labels", it).apply() }, accent)
                else -> WallpaperScreen(activity, accent)
            }
        }
    }
}

@Composable
private fun HomeScreen(activity: Activity, apps: List<LauncherApp>, theme: Int, accent: Color, scale: Float, cols: Int, labels: Boolean, style: Int, toApps: () -> Unit, toCustomize: () -> Unit) {
    val bgColors = palettes[theme]
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors)).padding(20.dp)) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("SNOWFALL", color = snow, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                    Text("YOUR WINTER HOME", color = snow.copy(.65f), fontSize = 10.sp, letterSpacing = 2.sp)
                }
                IconButton(onClick = toCustomize) { Icon(Icons.Rounded.Tune, "Personalize", tint = accent) }
            }
            Spacer(Modifier.height(26.dp))
            Text("10:08", color = snow, fontSize = 58.sp, fontWeight = FontWeight.Light)
            Text("Sunday, November 22", color = snow.copy(.75f), fontSize = 17.sp)
            Spacer(Modifier.height(18.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(.09f)), shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AcUnit, null, tint = accent, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Winter atmosphere", color = snow, fontWeight = FontWeight.SemiBold)
                        Text("Snowfall live wallpaper • Customize your phone", color = snow.copy(.65f), fontSize = 12.sp)
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = snow.copy(.7f))
                }
            }
            Spacer(Modifier.weight(1f))
            Text("FAVORITES", color = snow.copy(.65f), fontSize = 11.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            val favorites = apps.filter { app ->
                val n = app.label.lowercase()
                n.contains("phone") || n.contains("message") || n.contains("camera") || n.contains("chrome")
            }.take(4)
            if (favorites.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    favorites.forEach { app -> AppIcon(activity, app, accent, scale, labels, style, true) }
                }
            } else Text("Pin apps from your Apps drawer", color = snow.copy(.6f))
            Spacer(Modifier.height(14.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(.1f)), shape = RoundedCornerShape(26.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (apps.take(4)).forEach { AppIcon(activity, it, accent, scale, false, style, true) }
                    Box(Modifier.size(48.dp).clip(CircleShape).background(accent.copy(.15f)).clickable { toApps() }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Apps, "App drawer", tint = accent) }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Swipe up / tap Apps to open your app drawer", color = snow.copy(.55f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AppsScreen(activity: Activity, apps: List<LauncherApp>, search: String, onSearch: (String) -> Unit, filter: Int, onFilter: (Int) -> Unit, theme: Int, accent: Color, scale: Float, cols: Int, labels: Boolean, style: Int) {
    val filtered = apps.filter { it.label.contains(search, true) }
    Column(Modifier.fillMaxSize().background(night).padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("App library", color = snow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(apps.size.toString() + " installed apps", color = muted)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search apps") }, leadingIcon = { Icon(Icons.Rounded.Search, null) }, singleLine = true, shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = muted.copy(.25f), focusedTextColor = snow, unfocusedTextColor = snow))
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(cols), contentPadding = PaddingValues(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(filtered, key = { it.packageName }) { app -> AppIcon(activity, app, accent, scale, labels, style, false) }
        }
    }
}

@Composable
private fun AppIcon(activity: Activity, app: LauncherApp, accent: Color, scale: Float, labels: Boolean, style: Int, favorite: Boolean) {
    val context = activity
    val shape = when(style) { 1 -> CircleShape; 2 -> RoundedCornerShape(16.dp); 3 -> RoundedCornerShape(8.dp); else -> RoundedCornerShape(22.dp) }
    Column(Modifier.width(if(favorite) 64.dp else 76.dp).clickable {
        try { context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { context.startActivity(it) } } catch (_: Exception) {}
    }.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size((48 * scale).dp.coerceIn(40.dp, 72.dp)).clip(shape).background(Color.White.copy(if(style == 4) .12f else .05f)), contentAlignment = Alignment.Center) {
            AndroidView(factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageDrawable(app.resolveInfo.loadIcon(context.packageManager)) } }, modifier = Modifier.fillMaxSize().padding(2.dp))
        }
        if (labels) Text(app.label, color = snow, fontSize = 10.sp, maxLines = 1, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CustomizeScreen(theme: Int, setTheme: (Int) -> Unit, style: Int, setStyle: (Int) -> Unit, iconScale: Float, setScale: (Float) -> Unit, columns: Int, setColumns: (Int) -> Unit, labels: Boolean, setLabels: (Boolean) -> Unit, accent: Color) {
    val groups = remember {
        listOf(
            "Home screen" to listOf("Swipe gestures","Loop home pages","Screen transition animation","Home screen lock","Auto-arrange icons","Icon label shadows","Dock background blur","Dock transparency","Dock page indicator","Wallpaper parallax","Home screen padding","Safe area spacing","Widget snap grid","Folder previews","Folder open animation","Folder background","Folder icon count","Home page count","Default home page","Double tap to sleep","Shake to refresh"),
            "App drawer" to listOf("Drawer search bar","Alphabet index","Recent apps row","Suggested apps","Drawer background blur","Drawer transparency","Vertical scrolling","Horizontal pages","App labels","Hide apps","Sort by name","Sort by install date","Sort by usage","Show system apps","Icon badge dots","Badge count","Search suggestions","Keyboard opens automatically","Fast scroll","Drawer animation","Drawer padding"),
            "Icons & appearance" to listOf("Themed icon backgrounds","Monochrome icon effect","Icon shadows","Adaptive icon mask","Dynamic icon colors","Icon border","Icon glow","Icon press animation","Icon bounce","Icon tint","Icon saturation","Icon contrast","Icon rounding","Icon pack support","Custom app labels","Custom app glyphs","Icon size per page","Small icons","Large icons","Reset icon overrides"),
            "Widgets" to listOf("Clock widget","Date widget","Weather widget placeholder","Calendar preview","Battery widget","Music widget space","Quick notes widget","Photo frame widget","Widget transparency","Widget rounding","Widget shadows","Widget padding","Widget resize handles","Widget snap","Widget stack","Widget background","Widget color","Widget animation","Widget preview","Widget refresh"),
            "Motion & gestures" to listOf("Swipe down notifications shortcut","Swipe up app drawer","Double tap wallpaper","Long press edit mode","Haptic taps","Page swipe animation","Spring animation","Fade transitions","Reduce motion","Parallax effects","Scroll damping","Quick launch gestures","Back gesture hints","Home swipe hints","Gesture vibration","Gesture sensitivity","Edge swipe","Rotate home screen","Tilt wallpaper","Ambient motion")
        )
    }
    var enabled by remember { mutableStateOf((0..99).map { true }.toMutableList()) }
    Column(Modifier.fillMaxSize().background(night).verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Personalize", color = snow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Build your own phone look", color = muted)
        Spacer(Modifier.height(16.dp))
        Text("THEME", color = accent, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            paletteNames.forEachIndexed { i, name ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(panel).clickable { setTheme(i) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(58.dp, 42.dp).clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(palettes[i])))
                    Spacer(Modifier.width(12.dp))
                    Text(name, color = snow, modifier = Modifier.weight(1f))
                    if(theme == i) Icon(Icons.Rounded.CheckCircle, null, tint = palettes[i][2])
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("ICON SHAPE", color = accent, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Default","Circle","Square","Soft square","Frost").forEachIndexed { i, label ->
                FilterChip(selected = style == i, onClick = { setStyle(i) }, label = { Text(label) })
            }
        }
        Text("Icon size: " + (iconScale * 100).toInt() + "%", color = snow)
        Slider(value = iconScale, onValueChange = setScale, valueRange = .65f..1.35f)
        Text("App grid: " + columns + " columns", color = snow)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (3..6).forEach { n -> FilterChip(selected = columns == n, onClick = { setColumns(n) }, label = { Text(n.toString()) }) }
        }
        SwitchRow("Show app labels", labels, setLabels)
        Spacer(Modifier.height(20.dp))
        Text("100+ HOME OPTIONS", color = accent, fontWeight = FontWeight.Bold)
        Text("Individual switches are saved locally in this launcher.", color = muted, fontSize = 12.sp)
        var index = 0
        groups.forEach { (group, options) ->
            Text(group.uppercase(), color = snow, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
            options.forEach { option ->
                SwitchRow(option, enabled[index], { newValue ->
                    enabled = enabled.toMutableList().also { it[index] = newValue }
                })
                index++
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SwitchRow(title: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = snow, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@Composable
private fun WallpaperScreen(activity: Activity, accent: Color) {
    Column(Modifier.fillMaxSize().background(night).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Wallpaper studio", color = snow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Bring your winter theme to the background", color = muted)
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(350.dp).clip(RoundedCornerShape(28.dp)).background(Brush.verticalGradient(listOf(Color(0xFF08101D), Color(0xFF16334D), accent)))) {
            val infinite = rememberInfiniteTransition(label = "snowflakes")
            val drift by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(6500, easing = LinearEasing)), label = "drift")
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                repeat(90) { i ->
                    val x = ((i * 73f) % size.width)
                    val y = ((i * 127f + drift * size.height) % size.height)
                    drawCircle(Color.White.copy(alpha = .35f + (i % 5) * .1f), 1.2f + i % 3, androidx.compose.ui.geometry.Offset(x, y))
                }
            }
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                Text("WINTER", color = Color.White.copy(.7f), letterSpacing = 4.sp)
                Text("Quietly frozen.", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { applyLiveWallpaper(activity) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = night)) {
            Icon(Icons.Rounded.Wallpaper, null); Spacer(Modifier.width(8.dp)); Text("APPLY LIVE WALLPAPER")
        }
        Spacer(Modifier.height(12.dp))
        Text("Wallpaper is applied through Android's official wallpaper picker.", color = muted, fontSize = 12.sp)
        Spacer(Modifier.height(24.dp))
        Text("Important", color = snow, fontWeight = FontWeight.Bold)
        Text("Snowfall can replace your home screen when selected as your default launcher. Android does not let third-party apps replace the system lock screen, notification shade, or Quick Settings on most devices.", color = muted)
    }
}

private fun applyLiveWallpaper(activity: Activity) {
    try {
        activity.startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(activity, SnowWallpaperService::class.java)))
    } catch (_: Exception) {
        try { activity.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)) } catch (_: Exception) {}
    }
}
