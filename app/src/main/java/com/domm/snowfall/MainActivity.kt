package com.domm.snowfall

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.content.pm.ResolveInfo
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val bg = Color(0xFF07111F)
private val muted = Color(0xFF9BB1CA)
private val palettes = listOf(
    listOf(Color(0xFF07111F),Color(0xFF16334D),Color(0xFF73B7E8)),
    listOf(Color(0xFF16151C),Color(0xFF363746),Color(0xFFB8B9C8)),
    listOf(Color(0xFF071A20),Color(0xFF145060),Color(0xFF8CE5D4)),
    listOf(Color(0xFF180D29),Color(0xFF46265F),Color(0xFFD0A5FF)),
    listOf(Color(0xFF201209),Color(0xFF69401B),Color(0xFFF2BD63)),
    listOf(Color(0xFF240C16),Color(0xFF752C45),Color(0xFFFF9DBB)),
    listOf(Color(0xFF061A10),Color(0xFF20533A),Color(0xFF92E8A7)),
    listOf(Color(0xFF101010),Color(0xFF292929),Color(0xFFEAEAEA))
)
private val themeNames = listOf("Blue Hour","Silver Frost","Arctic","Violet","Amber","Rose","Aurora","Mono")
data class PhoneApp(val label:String,val packageName:String,val info:ResolveInfo)

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContent { SnowfallPhone(this) }
    }
}

@Composable
private fun SnowfallPhone(activity:Activity){
    val prefs=remember{activity.getSharedPreferences("snowfall",Activity.MODE_PRIVATE)}
    var tab by remember{mutableIntStateOf(0)}
    var theme by remember{mutableIntStateOf(prefs.getInt("theme",0).coerceIn(0,7))}
    var size by remember{mutableFloatStateOf(prefs.getFloat("iconSize",.9f))}
    var columns by remember{mutableIntStateOf(prefs.getInt("grid",4).coerceIn(3,6))}
    var shape by remember{mutableIntStateOf(prefs.getInt("shape",0))}
    var labels by remember{mutableStateOf(prefs.getBoolean("labels",true))}
    var monochrome by remember{mutableStateOf(prefs.getBoolean("monochrome",false))}
    var query by remember{mutableStateOf("")}
    val accent=palettes[theme][2]
    val apps=remember(activity){
        activity.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)
        .mapNotNull{ri->val pkg=ri.activityInfo?.packageName?:return@mapNotNull null
            if(pkg==activity.packageName)return@mapNotNull null
            PhoneApp(ri.loadLabel(activity.packageManager).toString(),pkg,ri)}
        .distinctBy{it.packageName}.sortedBy{it.label.lowercase()}
    }
    MaterialTheme(colorScheme=darkColorScheme(primary=accent,background=bg,surface=Color(0xFF142237))){
        Scaffold(containerColor=bg,bottomBar={
            NavigationBar(containerColor=Color(0xFF0B1726)){
                listOf("Home","Apps","Themes","Snow").forEachIndexed{i,n->
                    val icons=listOf(Icons.Rounded.Home,Icons.Rounded.Apps,Icons.Rounded.Palette,Icons.Rounded.AcUnit)
                    NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icons[i],null)},label={Text(n)},
                    colors=NavigationBarItemDefaults.colors(selectedIconColor=accent,selectedTextColor=accent,indicatorColor=Color(0xFF142237),unselectedIconColor=muted,unselectedTextColor=muted))
                }
            }
        }){ pad->
            when(tab){
                0->HomePage(activity,apps,theme,accent,size,labels,shape,monochrome,{tab=1},{tab=2})
                1->AppsPage(activity,apps,query,{query=it},accent,size,columns,labels,shape,monochrome)
                2->ThemePage(theme,{theme=it;prefs.edit().putInt("theme",it).apply()},size,{size=it;prefs.edit().putFloat("iconSize",it).apply()},columns,{columns=it;prefs.edit().putInt("grid",it).apply()},shape,{shape=it;prefs.edit().putInt("shape",it).apply()},labels,{labels=it;prefs.edit().putBoolean("labels",it).apply()},monochrome,{monochrome=it;prefs.edit().putBoolean("monochrome",it).apply()},accent)
                else->WallpaperPage(activity,accent)
            }
        }
    }
}

@Composable
private fun HomePage(activity:Activity,apps:List<PhoneApp>,theme:Int,accent:Color,iconScale:Float,labels:Boolean,shape:Int,monochrome:Boolean,goApps:()->Unit,goThemes:()->Unit){
    val date=java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM"))
    val hour=java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    val favorites=apps.filter{a->val n=a.label.lowercase();n.contains("phone")||n.contains("camera")||n.contains("message")||n.contains("chrome")}.take(4)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(palettes[theme][0],palettes[theme][1],bg)))){
        Canvas(Modifier.fillMaxSize()){ repeat(70){i-> drawCircle(Color.White.copy(.12f+(i%5)*.04f),1f+(i%2),androidx.compose.ui.geometry.Offset((i*91f)%size.width,(i*157f)%size.height)) } }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp, vertical=26.dp)){
            Row(verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){
                    Text("SNOWFALL",color=Color.White,fontWeight=FontWeight.Bold,letterSpacing=4.sp,fontSize=13.sp)
                    Text("WINTER EDITION  /  01",color=Color.White.copy(.52f),fontSize=9.sp,letterSpacing=2.sp)
                }
                Text("✳",color=accent,fontSize=22.sp)
                IconButton(onClick=goThemes){Icon(Icons.Rounded.Tune,"Personalize",tint=Color.White.copy(.8f))}
            }
            Spacer(Modifier.height(38.dp))
            Text(hour,color=Color.White,fontSize=66.sp,fontWeight=FontWeight.ExtraLight,letterSpacing=(-2).sp)
            Text(date.uppercase(),color=Color.White.copy(.62f),fontSize=11.sp,letterSpacing=1.5.sp)
            Spacer(Modifier.height(30.dp))
            Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(listOf(palettes[theme][1],palettes[theme][0],Color(0xFF07101A))))){
                Canvas(Modifier.fillMaxSize()){ repeat(90){i->drawCircle(Color.White.copy(.28f+(i%4)*.08f),1f+(i%3),androidx.compose.ui.geometry.Offset((i*71f)%360f,(i*139f)%220f)) };drawCircle(accent.copy(.09f),80f,androidx.compose.ui.geometry.Offset(270f,50f)) }
                Column(Modifier.align(Alignment.BottomStart).padding(22.dp)){
                    Text("SEASONAL WALLPAPER",color=Color.White.copy(.65f),fontSize=9.sp,letterSpacing=2.sp)
                    Text("Quietly frozen.",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Light)
                    Text("Live snow • Ambient light",color=Color.White.copy(.7f),fontSize=11.sp)
                }
                Text("↗",color=Color.White.copy(.8f),modifier=Modifier.align(Alignment.TopEnd).padding(18.dp).clickable{goThemes()},fontSize=18.sp)
            }
            Spacer(Modifier.height(30.dp))
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("FAVORITES",color=Color.White.copy(.6f),fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.weight(1f))
                Text("EDIT",color=accent,fontSize=10.sp,letterSpacing=1.sp,modifier=Modifier.clickable{goApps()})
            }
            Spacer(Modifier.height(15.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){ favorites.forEach{AppIcon(activity,it,accent,iconScale,labels,shape,monochrome)} }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().clickable{goApps()}){
                Text("APPLICATIONS",color=Color.White.copy(.8f),fontSize=11.sp,letterSpacing=2.sp,modifier=Modifier.weight(1f))
                Text("Browse all  ·  "+apps.size,color=Color.White.copy(.6f),fontSize=11.sp)
                Icon(Icons.Rounded.ArrowForward,null,tint=accent,modifier=Modifier.padding(start=8.dp))
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth().height(68.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(.045f)).clickable{goApps()}.padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){
                apps.take(4).forEach{AppIcon(activity,it,accent,.78f,false,shape)}
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.Search,null,tint=Color.White.copy(.7f),modifier=Modifier.padding(end=4.dp))
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("ATMOSPHERE",color=Color.White.copy(.55f),fontSize=9.sp,letterSpacing=2.sp,modifier=Modifier.weight(1f))
                Text(themeNames[theme].uppercase(),color=accent,fontSize=10.sp,letterSpacing=1.sp,modifier=Modifier.clickable{goThemes()})
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                Text("Snowfall",color=Color.White.copy(.7f),fontSize=12.sp)
                Text("•",color=accent)
                Text("Soft glow",color=Color.White.copy(.7f),fontSize=12.sp)
                Text("•",color=accent)
                Text("Slow drift",color=Color.White.copy(.7f),fontSize=12.sp)
            }
        }
    }
}
@Composable
private fun AppsPage(activity:Activity,apps:List<PhoneApp>,query:String,onQuery:(String)->Unit,accent:Color,size:Float,grid:Int,labels:Boolean,shape:Int,monochrome:Boolean){
    Column(Modifier.fillMaxSize().background(bg).padding(horizontal=16.dp)){
        Spacer(Modifier.height(22.dp));Text("App drawer",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text(apps.size.toString()+" installed apps",color=muted);Spacer(Modifier.height(12.dp))
        OutlinedTextField(query,onQuery,modifier=Modifier.fillMaxWidth(),placeholder={Text("Search apps")},leadingIcon={Icon(Icons.Rounded.Search,null)},singleLine=true,shape=RoundedCornerShape(18.dp))
        val filtered=apps.filter{it.label.contains(query,true)}
        LazyVerticalGrid(columns=GridCells.Fixed(grid),contentPadding=PaddingValues(vertical=16.dp),horizontalArrangement=Arrangement.spacedBy(4.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { items(filtered,key={it.packageName}) { AppIcon(activity,it,accent,size,labels,shape,monochrome) } }
    }
}

@Composable
private fun AppIcon(activity:Activity,app:PhoneApp,accent:Color,size:Float,labels:Boolean,shape:Int,monochrome:Boolean){
    val iconShape=when(shape){1->CircleShape;2->RoundedCornerShape(8.dp);3->RoundedCornerShape(18.dp);4->RoundedCornerShape(50.dp);else->RoundedCornerShape(20.dp)}
    Column(Modifier.width(72.dp).clickable(onClick = { try { activity.packageManager.getLaunchIntentForPackage(app.packageName)?.let { launch -> activity.startActivity(launch) } } catch (_: Exception) { } }).padding(4.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Box(Modifier.size((48*size).dp.coerceIn(36.dp,72.dp)).clip(iconShape).background(if(monochrome) accent.copy(.16f) else Color.White.copy(.06f)),contentAlignment=Alignment.Center){
            AndroidView(factory={ImageView(it).apply{scaleType=ImageView.ScaleType.FIT_CENTER;setImageDrawable(app.info.loadIcon(activity.packageManager))}},update={view->view.imageTintList=if(monochrome) ColorStateList.valueOf(accent.toArgb()) else null},modifier=Modifier.fillMaxSize().padding(2.dp))
        }
        if(labels)Text(app.label,color=Color.White,fontSize=10.sp,maxLines=1,textAlign=TextAlign.Center)
    }
}

@Composable
private fun ThemePage(theme:Int,setTheme:(Int)->Unit,size:Float,setSize:(Float)->Unit,grid:Int,setGrid:(Int)->Unit,shape:Int,setShape:(Int)->Unit,labels:Boolean,setLabels:(Boolean)->Unit,monochrome:Boolean,setMonochrome:(Boolean)->Unit,accent:Color){
    val ctx=androidx.compose.ui.platform.LocalContext.current
    val p=remember{ctx.getSharedPreferences("snowfall-extra",Activity.MODE_PRIVATE)}
    val groups=remember{listOf(
        "Home screen" to listOf("Page swipe","Page loop","Layout lock","Auto arrange","Dock blur","Dock opacity","Page dots","Wallpaper parallax","Widget snap","Folder previews","Folder animation","Folder tint","Folder size","Home padding","Safe zones","Page count","Home page selection","Tap haptics","Double tap shortcut","Icon labels shadow"),
        "App drawer" to listOf("Search field","Alphabet scroll","Recent apps","Suggestions","Drawer blur","Drawer opacity","Vertical scrolling","Page layout","App labels","Hide apps","Sort alphabetically","Sort by install date","Usage sorting","System apps visibility","Notification dots","Badge counts","Search hints","Auto keyboard","Quick scroll","Open animation"),
        "Icon options" to listOf("Themed icons","Monochrome","Drop shadows","Adaptive masks","Dynamic colors","Borders","Glow","Press effect","Bounce","Tint","Saturation","Contrast","Round corners","Pack compatibility","Rename apps","Glyph overrides","Individual scale","Small icons","Large icons","Reset"),
        "Widgets" to listOf("Clock","Date","Weather panel","Calendar","Battery","Music","Notes","Photo tile","Transparency","Roundness","Shadows","Padding","Resize","Snap","Stack","Background","Accent color","Animation","Preview","Refresh"),
        "Gestures and motion" to listOf("Pull-down action","Swipe-up drawer","Double-tap","Long-press edit","Haptic taps","Page animation","Spring effect","Fade","Reduce motion","Parallax","Scroll damping","Quick launch","Back hints","Home hints","Gesture vibration","Sensitivity","Edge swipe","Rotate screen","Tilt","Ambient motion"))}
    var states by remember{mutableStateOf((0 until 100).map{p.getBoolean("option"+it,true)}.toMutableList())}
    Column(Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(18.dp)){
        Text("Personalize your phone",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Launcher theme, icons, and layout",color=muted);Spacer(Modifier.height(18.dp))
        Text("THEME PACKS",color=accent,fontWeight=FontWeight.Bold)
        themeNames.forEachIndexed{i,n->Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF142237)).clickable{setTheme(i)}.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(55.dp,40.dp).clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(palettes[i])));Spacer(Modifier.width(12.dp));Text(n,color=Color.White,modifier=Modifier.weight(1f));if(theme==i)Icon(Icons.Rounded.CheckCircle,null,tint=palettes[i][2])};Spacer(Modifier.height(7.dp))}
        Spacer(Modifier.height(12.dp));Text("ICON SHAPE",color=accent,fontWeight=FontWeight.Bold)
        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("Default","Circle","Square","Rounded","Frost").forEachIndexed{i,n->FilterChip(selected=shape==i,onClick={setShape(i)},label={Text(n)})}}
        Text("Icon size: "+(size*100).toInt()+"%",color=Color.White);Slider(value=size,onValueChange=setSize,valueRange=.65f..1.35f)
        Text("App grid columns: "+grid,color=Color.White);Row{(3..6).forEach{FilterChip(selected=grid==it,onClick={setGrid(it)},label={Text(it.toString())})}}
        Row(verticalAlignment=Alignment.CenterVertically){Text("Show app names",color=Color.White,modifier=Modifier.weight(1f));Switch(labels,setLabels)}
        Spacer(Modifier.height(16.dp));Row(verticalAlignment=Alignment.CenterVertically){Text("Monochrome app icons",color=Color.White,modifier=Modifier.weight(1f));Switch(monochrome,setMonochrome)}
Text("Tint installed icons to match the selected winter theme.",color=muted,fontSize=12.sp)
Spacer(Modifier.height(18.dp));Text("100 STORED-ONLY PREFERENCE SWITCHES",color=accent,fontWeight=FontWeight.Bold);Text("These 100 advanced switches save preferences only. They do not currently activate the listed feature. The live controls are theme preset, icon shape, icon size, grid columns, app labels, monochrome tint, app launching, and live wallpaper.",color=muted,fontSize=12.sp)
        var index=0
        groups.forEach{(heading,list)->Text(heading.uppercase(),color=Color.White,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=16.dp,bottom=6.dp));list.forEach{name->val id=index;Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(name,color=Color.White,fontSize=13.sp,modifier=Modifier.weight(1f));Switch(states[id],{v->states=states.toMutableList().also{it[id]=v};p.edit().putBoolean("option"+id,v).apply()})};index++}}
    }
}

@Composable
private fun WallpaperPage(activity:Activity,accent:Color){
    Column(Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(20.dp)){
        Text("Snow studio",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Animated winter wallpaper",color=muted);Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(350.dp).clip(RoundedCornerShape(28.dp)).background(Brush.verticalGradient(listOf(Color(0xFF06101F),Color(0xFF173957),accent)))){val anim=rememberInfiniteTransition(label="snow");val f by anim.animateFloat(0f,1f,infiniteRepeatable(tween(7000,easing=LinearEasing)),label="flakes");Canvas(Modifier.fillMaxSize()){repeat(100){i->drawCircle(Color.White.copy(.45f),1.4f+i%3,androidx.compose.ui.geometry.Offset((i*79f)%size.width,(i*137f+f*size.height)%size.height))}};Column(Modifier.align(Alignment.BottomStart).padding(22.dp)){Text("WINTER",color=Color.White.copy(.7f),letterSpacing=4.sp);Text("Quietly frozen.",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold)}}
        Spacer(Modifier.height(16.dp));Button(onClick={try{activity.startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,ComponentName(activity,SnowWallpaperService::class.java)))}catch(_:Exception){try{activity.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))}catch(_:Exception){}}},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=accent,contentColor=bg)){Icon(Icons.Rounded.Wallpaper,null);Spacer(Modifier.width(8.dp));Text("APPLY LIVE WALLPAPER")}
        Spacer(Modifier.height(20.dp));Text("Android limitation",color=Color.White,fontWeight=FontWeight.Bold);Text("A third-party launcher can replace the home screen and app drawer. It cannot replace Android's lock screen, notification shade, Quick Settings, or system status icons.",color=muted)
    }
}
