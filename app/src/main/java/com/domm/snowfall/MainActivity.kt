package com.domm.snowfall

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private val bg = Color(0xFF07111F)
private val surface = Color(0xFF101F32)
private val surface2 = Color(0xFF172B42)
private val ice = Color(0xFF9EDCFF)
private val pale = Color(0xFFDCEAF8)
private val dim = Color(0xFF8198B1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SnowfallApp(this) }
    }
}

@Composable
private fun SnowfallApp(activity: Activity) {
    val prefs = remember { activity.getSharedPreferences("snowfall", Activity.MODE_PRIVATE) }
    var tab by remember { mutableIntStateOf(0) }
    var snow by remember { mutableFloatStateOf(prefs.getFloat("snow", .68f)) }
    var wind by remember { mutableFloatStateOf(prefs.getFloat("wind", .42f)) }
    var size by remember { mutableFloatStateOf(prefs.getFloat("size", .5f)) }
    var glow by remember { mutableStateOf(prefs.getBoolean("glow", true)) }
    var twinkle by remember { mutableStateOf(prefs.getBoolean("twinkle", true)) }
    var moon by remember { mutableStateOf(prefs.getBoolean("moon", true)) }
    var clouds by remember { mutableStateOf(prefs.getBoolean("clouds", false)) }
    var ambient by remember { mutableStateOf(prefs.getBoolean("ambient", false)) }
    var snowfall by remember { mutableStateOf(prefs.getBoolean("snowfall", true)) }
    var preset by remember { mutableIntStateOf(prefs.getInt("preset", 0)) }
    var showHelp by remember { mutableStateOf(false) }

    fun persist() {
        prefs.edit().putFloat("snow", snow).putFloat("wind", wind).putFloat("size", size)
            .putBoolean("glow", glow).putBoolean("twinkle", twinkle).putBoolean("moon", moon)
            .putBoolean("clouds", clouds).putBoolean("ambient", ambient).putBoolean("snowfall", snowfall)
            .putInt("preset", preset).apply()
    }

    val palette = listOf(
        listOf(Color(0xFF07111F), Color(0xFF10243A), Color(0xFF244563)),
        listOf(Color(0xFF101622), Color(0xFF1B2536), Color(0xFF43516B)),
        listOf(Color(0xFF031722), Color(0xFF0C2E3B), Color(0xFF326577)),
        listOf(Color(0xFF111827), Color(0xFF242E42), Color(0xFF58677C))
    )

    MaterialTheme(colorScheme = darkColorScheme(primary = ice, background = bg, surface = surface)) {
        Scaffold(containerColor = bg, bottomBar = {
            NavigationBar(containerColor = Color(0xFF0A1726), tonalElevation = 0.dp) {
                listOf("Home", "Effects", "Looks", "Settings").forEachIndexed { index, name ->
                    val icons = listOf(Icons.Rounded.Home, Icons.Rounded.Waves, Icons.Rounded.Palette, Icons.Rounded.Settings)
                    NavigationBarItem(selected = tab == index, onClick = { tab = index },
                        icon = { Icon(icons[index], null) }, label = { Text(name) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = ice, selectedTextColor = ice, indicatorColor = surface2, unselectedIconColor = dim, unselectedTextColor = dim))
                }
            }
        }) { padding ->
            when (tab) {
                0 -> HomePage(activity, snow, wind, size, glow, twinkle, moon, clouds, palette[preset], { tab = 1 }, { snow = it; persist() }, { wind = it; persist() })
                1 -> EffectsPage(snow, wind, size, glow, twinkle, moon, clouds, ambient, snowfall,
                    { snow = it; persist() }, { wind = it; persist() }, { size = it; persist() },
                    { glow = it; persist() }, { twinkle = it; persist() }, { moon = it; persist() },
                    { clouds = it; persist() }, { ambient = it; persist() }, { snowfall = it; persist() })
                2 -> LooksPage(preset, palette, { preset = it; persist() })
                else -> SettingsPage(activity, showHelp, { showHelp = it }, { tab = 2 }, { reset ->
                    if (reset) {
                        snow = .68f; wind = .42f; size = .5f; glow = true; twinkle = true; moon = true; clouds = false; ambient = false; snowfall = true; preset = 0
                        persist()
                    }
                })
            }
        }
    }
}

@Composable
private fun HomePage(activity: Activity, snow: Float, wind: Float, size: Float, glow: Boolean, twinkle: Boolean, moon: Boolean, clouds: Boolean, colors: List<Color>, toEffects: () -> Unit, setSnow: (Float) -> Unit, setWind: (Float) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Spacer(Modifier.height(24.dp))
        Header()
        Spacer(Modifier.height(26.dp))
        Text("Your winter.", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text("A little quiet. A lot of snow.", color = dim, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(310.dp).clip(RoundedCornerShape(28.dp))) {
            SnowScene(snow, wind, size, glow, twinkle, moon, clouds, colors)
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                Text("WINTER EDITION   •   LIVE", color = pale.copy(alpha = .7f), letterSpacing = 2.sp, style = MaterialTheme.typography.labelSmall)
                Text("Quietly frozen.", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Live snowfall wallpaper", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("Bring the snow to your home screen", color = dim, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { openWallpaper(activity) }, shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = ice, contentColor = bg)) { Text("APPLY", fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(14.dp))
        Panel {
            Text("Quick controls", color = pale, fontWeight = FontWeight.SemiBold)
            SliderControl("Snow intensity", snow, setSnow, "Soft", "Blizzard")
            SliderControl("Wind", wind, setWind, "Still", "Breezy")
            TextButton(onClick = toEffects) { Text("More atmosphere settings  →", color = ice) }
        }
        Spacer(Modifier.height(18.dp))
        Text("Wallpaper changes are applied through Android's wallpaper picker.", color = dim, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(surface2), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.AcUnit, null, tint = ice, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("SNOWFALL", color = pale, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, fontSize = 14.sp)
            Text("WINTER PERSONALISATION", color = dim, fontSize = 9.sp, letterSpacing = 1.6.sp)
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(surface).padding(horizontal = 10.dp, vertical = 7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(ice)); Spacer(Modifier.width(6.dp)); Text("WINTER", color = pale, fontSize = 10.sp, letterSpacing = 1.sp) }
        }
    }
}

@Composable
private fun EffectsPage(snow: Float, wind: Float, size: Float, glow: Boolean, twinkle: Boolean, moon: Boolean, clouds: Boolean, ambient: Boolean, snowfall: Boolean,
    setSnow: (Float) -> Unit, setWind: (Float) -> Unit, setSize: (Float) -> Unit, setGlow: (Boolean) -> Unit, setTwinkle: (Boolean) -> Unit, setMoon: (Boolean) -> Unit, setClouds: (Boolean) -> Unit, setAmbient: (Boolean) -> Unit, setSnowfall: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Spacer(Modifier.height(24.dp)); Header(); Spacer(Modifier.height(24.dp))
        Text("Atmosphere", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Fine tune the scene. Make it yours.", color = dim)
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(24.dp))) { SnowScene(snow, wind, size, glow, twinkle, moon, clouds, listOf(Color(0xFF07111F), Color(0xFF10243A), Color(0xFF244563))) }
        Spacer(Modifier.height(16.dp))
        Panel {
            SliderControl("Snow density", snow, setSnow, "Few flakes", "Heavy snow")
            SliderControl("Wind strength", wind, setWind, "Calm", "Strong")
            SliderControl("Flake size", size, setSize, "Fine", "Large")
        }
        Spacer(Modifier.height(12.dp))
        Panel {
            ToggleRow("Snowfall", "Falling snow particle layer", snowfall, setSnowfall, Icons.Rounded.AcUnit)
            ToggleRow("Moonlight", "Add a moon to the sky", moon, setMoon, Icons.Rounded.NightsStay)
            ToggleRow("Soft glow", "Atmospheric light bloom", glow, setGlow, Icons.Rounded.LightMode)
            ToggleRow("Twinkle", "Random shimmer on snow", twinkle, setTwinkle, Icons.Rounded.AutoAwesome)
            ToggleRow("Cloud layer", "Slow drifting clouds", clouds, setClouds, Icons.Rounded.Cloud)
            ToggleRow("Ambient motion", "Gentle background movement", ambient, setAmbient, Icons.Rounded.Animation)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LooksPage(preset: Int, palettes: List<List<Color>>, select: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Spacer(Modifier.height(24.dp)); Header(); Spacer(Modifier.height(26.dp))
        Text("Looks", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Choose your winter palette.", color = dim)
        Spacer(Modifier.height(18.dp))
        val names = listOf("Blue hour", "Silver frost", "Arctic teal", "Midnight")
        val descriptions = listOf("Deep blue evening sky", "Cool greys and pale moonlight", "Frozen northern sea", "Dark winter night")
        names.forEachIndexed { i, name ->
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(88.dp, 96.dp).clip(RoundedCornerShape(16.dp))) {
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(palettes[i])))
                        MiniSnow()
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(descriptions[i], color = dim, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(9.dp))
                        Row { palettes[i].forEach { color -> Box(Modifier.padding(end = 5.dp).size(20.dp).clip(RoundedCornerShape(50)).background(color)) } }
                    }
                    if (preset == i) Icon(Icons.Rounded.CheckCircle, null, tint = ice) else TextButton(onClick = { select(i) }) { Text("SELECT", color = ice) }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SettingsPage(activity: Activity, help: Boolean, setHelp: (Boolean) -> Unit, openLooks: () -> Unit, reset: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Spacer(Modifier.height(24.dp)); Header(); Spacer(Modifier.height(26.dp))
        Text("Settings", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text("App preferences and shortcuts.", color = dim)
        Spacer(Modifier.height(18.dp))
        Panel {
            ToggleRow("Haptic feedback", "Toggle preference placeholder", help, setHelp, Icons.Rounded.Vibration)
            Row(Modifier.fillMaxWidth().clickable { openLooks() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Palette, null, tint = ice); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Winter palettes", color = pale); Text("Choose the scene colours", color = dim, style = MaterialTheme.typography.bodySmall) }; Icon(Icons.Rounded.ChevronRight, null, tint = dim)
            }
            Row(Modifier.fillMaxWidth().clickable { reset(true) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.RestartAlt, null, tint = ice); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Reset all controls", color = pale); Text("Restore the default winter settings", color = dim, style = MaterialTheme.typography.bodySmall) }; Icon(Icons.Rounded.ChevronRight, null, tint = dim)
            }
        }
        Spacer(Modifier.height(12.dp))
        Panel {
            Text("About Snowfall", color = pale, fontWeight = FontWeight.SemiBold)
            Text("A winter ambience and wallpaper experience. Snowfall applies its animated wallpaper through Android's built-in live wallpaper picker.", color = dim, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("Version 1.0 • Winter edition", color = dim, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(surface).padding(16.dp), content = content)
}

@Composable
private fun SliderControl(title: String, value: Float, onChange: (Float) -> Unit, low: String, high: String) {
    Column(Modifier.padding(top = 6.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text(title, color = pale, fontWeight = FontWeight.Medium); Spacer(Modifier.weight(1f)); Text("${(value * 100).toInt()}%", color = ice, style = MaterialTheme.typography.labelMedium) }
        Slider(value = value, onValueChange = onChange, colors = SliderDefaults.colors(thumbColor = ice, activeTrackColor = ice, inactiveTrackColor = surface2))
        Row { Text(low, color = dim, style = MaterialTheme.typography.labelSmall); Spacer(Modifier.weight(1f)); Text(high, color = dim, style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
private fun ToggleRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ice, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, color = pale, fontWeight = FontWeight.Medium); Text(description, color = dim, style = MaterialTheme.typography.bodySmall) }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SnowScene(snow: Float, wind: Float, sizeFactor: Float, glow: Boolean, twinkle: Boolean, moon: Boolean, clouds: Boolean, palette: List<Color>) {
    val animation = rememberInfiniteTransition(label = "winter")
    val fall by animation.animateFloat(0f, 1f, infiniteRepeatable(tween(10500, easing = LinearEasing)), label = "fall")
    val cloudMove by animation.animateFloat(0f, 1f, infiniteRepeatable(tween(24000, easing = LinearEasing)), label = "cloud")
    val sparkle by animation.animateFloat(0f, 1f, infiniteRepeatable(tween(1900)), label = "sparkle")
    val particles = remember { List(230) { Triple(Random.nextFloat(), Random.nextFloat(), .5f + Random.nextFloat() * 2.8f) } }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(palette))) {
        Canvas(Modifier.fillMaxSize()) {
            if (moon) {
                val center = androidx.compose.ui.geometry.Offset(this.size.width * .77f, this.size.height * .22f)
                drawCircle(Color(0xFFE5F1FF).copy(alpha = .88f), this.size.width * .065f, center)
                if (glow) drawCircle(Color(0xFFE2F1FF).copy(alpha = .10f), this.size.width * .12f, center)
            }
            if (clouds) {
                val x = (cloudMove * this.size.width * .65f)
                val cloudPaint = Color(0xFFBBD5EB).copy(alpha = .10f)
                drawCircle(cloudPaint, this.size.width * .09f, androidx.compose.ui.geometry.Offset(x, this.size.height * .22f))
                drawCircle(cloudPaint, this.size.width * .07f, androidx.compose.ui.geometry.Offset(x + this.size.width*.08f, this.size.height*.20f))
            }
            particles.take((particles.size * snow).toInt()).forEach { (x, y, r) ->
                val yy = (y + fall * (.15f + wind * .6f)) % 1f
                val xx = (x + kotlin.math.sin((yy * 6.5f + cloudMove * 2).toDouble()).toFloat() * wind * .04f + wind * fall * .08f) % 1f
                val shimmer = if (twinkle) .65f + .35f * kotlin.math.abs(kotlin.math.sin((sparkle * 6 + x * 30).toDouble()).toFloat()) else .85f
                drawCircle(Color.White.copy(alpha = shimmer), (r + sizeFactor * 1.8f) * 1.25f, androidx.compose.ui.geometry.Offset(xx * this.size.width, yy * this.size.height))
            }
            if (glow) drawRect(Brush.radialGradient(listOf(Color(0xFF9EDCFF).copy(alpha = .12f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(this.size.width*.5f, this.size.height*.35f), radius = this.size.width*.7f))
        }
    }
}

@Composable
private fun MiniSnow() {
    val particles = remember { List(45) { androidx.compose.ui.geometry.Offset(Random.nextFloat(), Random.nextFloat()) } }
    Canvas(Modifier.fillMaxSize()) { particles.forEach { drawCircle(Color.White.copy(alpha = .45f), 1.3f, androidx.compose.ui.geometry.Offset(it.x * size.width, it.y * size.height)) } }
}

private fun openWallpaper(activity: Activity) {
    try {
        activity.startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(activity, SnowWallpaperService::class.java)))
    } catch (_: Exception) {
        try { activity.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)) } catch (_: Exception) {}
    }
}
