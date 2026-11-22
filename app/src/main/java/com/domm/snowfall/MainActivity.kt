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
            Column(Modifier.fillMaxWidth().background(Color(0xFF091321))) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(.07f)))
                Row(Modifier.fillMaxWidth().height(62.dp).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){
                    listOf("Home","Apps","Themes","Snow").forEachIndexed{i,n->
                        val icons=listOf(Icons.Rounded.Home,Icons.Rounded.Apps,Icons.Rounded.Palette,Icons.Rounded.AcUnit)
                        Column(Modifier.weight(1f).fillMaxHeight().clickable{tab=i}.padding(top=7.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween){
                            Icon(icons[i],null,tint=if(tab==i)accent else muted,modifier=Modifier.size(20.dp))
                            Text(n,color=if(tab==i)accent else muted,fontSize=10.sp,letterSpacing=.3.sp)
                            Box(Modifier.width(18.dp).height(2.dp).background(if(tab==i)accent else Color.Transparent))
                        }
                    }
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
        Canvas(Modifier.fillMaxSize()){ repeat(70){i-> drawCircle(Color.White.copy(.12f+(i%5)*.04f),1f+(i%2),androidx.compose.ui.geometry.Offset((i*91f)%this.size.width,(i*157f)%this.size.height)) } }
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
            Box(Modifier.fillMaxWidth().height(190.dp).background(Brush.linearGradient(listOf(palettes[theme][1],palettes[theme][0],Color(0xFF07101A))))){
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
            Row(Modifier.fillMaxWidth().height(62.dp).background(Color.White.copy(.045f)).clickable{goApps()}.padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){
                apps.take(4).forEach{AppIcon(activity,it,accent,.78f,false,shape,monochrome)}
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
    Column(Modifier.widthIn(min=52.dp,max=72.dp).clickable(onClick = { try { activity.packageManager.getLaunchIntentForPackage(app.packageName)?.let { launch -> activity.startActivity(launch) } } catch (_: Exception) { } }).padding(4.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Box(Modifier.size((48*size).dp.coerceIn(36.dp,72.dp)).clip(iconShape).background(if(monochrome) accent.copy(.16f) else Color.White.copy(.06f)),contentAlignment=Alignment.Center){
            AndroidView(factory={ImageView(it).apply{scaleType=ImageView.ScaleType.FIT_CENTER;setImageDrawable(app.info.loadIcon(activity.packageManager))}},update={view->view.imageTintList=if(monochrome) ColorStateList.valueOf(accent.toArgb()) else null},modifier=Modifier.fillMaxSize().padding(2.dp))
        }
        if(labels)Text(app.label,color=Color.White,fontSize=10.sp,maxLines=1,textAlign=TextAlign.Center)
    }
}

@Composable
private fun ThemePage(theme:Int,setTheme:(Int)->Unit,size:Float,setSize:(Float)->Unit,grid:Int,setGrid:(Int)->Unit,shape:Int,setShape:(Int)->Unit,labels:Boolean,setLabels:(Boolean)->Unit,monochrome:Boolean,setMonochrome:(Boolean)->Unit,accent:Color){
    Column(Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(horizontal=22.dp,vertical=24.dp)){
        Text("PERSONALIZATION",color=muted,fontSize=10.sp,letterSpacing=2.6.sp)
        Spacer(Modifier.height(7.dp))
        Text("Make it yours.",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Light,letterSpacing=(-.7).sp)
        Text("A quieter home for your apps.",color=muted,fontSize=13.sp)
        Spacer(Modifier.height(28.dp))
        Text("SEASONAL PALETTES",color=accent,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
        Spacer(Modifier.height(12.dp))
        themeNames.forEachIndexed{i,n->
            Row(Modifier.fillMaxWidth().clickable{setTheme(i)}.padding(vertical=9.dp),verticalAlignment=Alignment.CenterVertically){
                Box(Modifier.width(68.dp).height(40.dp).background(Brush.horizontalGradient(palettes[i])))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)){
                    Text(n,color=Color.White,fontSize=15.sp,fontWeight=if(theme==i)FontWeight.Medium else FontWeight.Normal)
                    Text(if(theme==i)"CURRENT PALETTE" else "WINTER COLORWAY",color=muted,fontSize=9.sp,letterSpacing=1.2.sp)
                }
                if(theme==i)Icon(Icons.Rounded.Check,null,tint=palettes[i][2],modifier=Modifier.size(18.dp))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(.055f)))
        }
        Spacer(Modifier.height(26.dp))
        Text("ICON FORM",color=accent,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("Default","Circle","Square","Rounded","Frost").forEachIndexed{i,n->FilterChip(selected=shape==i,onClick={setShape(i)},label={Text(n)},shape=RoundedCornerShape(5.dp))}}
        Spacer(Modifier.height(20.dp))
        Text("ICON SCALE   ${(size*100).toInt()}%",color=Color.White,fontSize=12.sp)
        Slider(value=size,onValueChange=setSize,valueRange=.65f..1.35f)
        Spacer(Modifier.height(12.dp))
        Text("DRAWER GRID   $grid COLUMNS",color=Color.White,fontSize=12.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){(3..6).forEach{FilterChip(selected=grid==it,onClick={setGrid(it)},label={Text(it.toString())},shape=RoundedCornerShape(5.dp))}}
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(.08f)))
        Row(Modifier.fillMaxWidth().padding(vertical=14.dp),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)){
                Text("App names",color=Color.White,fontSize=14.sp)
                Text("Show labels under icons",color=muted,fontSize=11.sp)
            }
            Switch(labels,setLabels)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(.08f)))
        Row(Modifier.fillMaxWidth().padding(vertical=14.dp),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)){
                Text("Winter icon tint",color=Color.White,fontSize=14.sp)
                Text("Apply the selected accent to icons",color=muted,fontSize=11.sp)
            }
            Switch(monochrome,setMonochrome)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(.08f)))
        Spacer(Modifier.height(24.dp))
        Text("LIVE FEATURES",color=accent,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
        Text("These controls update the launcher immediately. Wallpaper motion and snow settings live in Snow Studio.",color=muted,fontSize=12.sp,lineHeight=18.sp)
    }
}

@Composable
private fun WallpaperPage(activity:Activity,accent:Color){
    val prefs=remember(activity){activity.getSharedPreferences("snowfall",Activity.MODE_PRIVATE)}
    var snowEnabled by remember{mutableStateOf(prefs.getBoolean("snowfall",true))}
    var glow by remember{mutableStateOf(prefs.getBoolean("glow",true))}
    var density by remember{mutableFloatStateOf(prefs.getFloat("snow",.68f))}
    var wind by remember{mutableFloatStateOf(prefs.getFloat("wind",.42f))}
    var flakeSize by remember{mutableFloatStateOf(prefs.getFloat("size",.5f))}
    Column(Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(20.dp)){
        Text("Snow studio",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold)
        Text("Live wallpaper and snowfall controls",color=muted)
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(350.dp).clip(RoundedCornerShape(28.dp)).background(Brush.verticalGradient(listOf(Color(0xFF06101F),Color(0xFF173957),accent)))){
            val anim=rememberInfiniteTransition(label="snow")
            val f by anim.animateFloat(0f,1f,infiniteRepeatable(tween(7000,easing=LinearEasing)),label="flakes")
            Canvas(Modifier.fillMaxSize()){
                if(glow)drawCircle(accent.copy(.09f),this.size.width*.42f,androidx.compose.ui.geometry.Offset(this.size.width*.72f,this.size.height*.28f))
                if(snowEnabled)repeat((100*density).toInt()){i->
                    val x=((i*79f+wind*f*80f)%this.size.width)
                    val y=((i*137f+f*this.size.height)%this.size.height)
                    drawCircle(Color.White.copy(.35f+(i%4)*.08f), (1.4f+i%3)*(0.65f+flakeSize),androidx.compose.ui.geometry.Offset(x,y))
                }
            }
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)){
                Text("WINTER",color=Color.White.copy(.7f),letterSpacing=4.sp)
                Text("Quietly frozen.",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold)
                Text("Preview responds to the controls below",color=Color.White.copy(.65f),fontSize=11.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick={try{activity.startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,ComponentName(activity,SnowWallpaperService::class.java)))}catch(_:Exception){try{activity.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))}catch(_:Exception){}}},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=accent,contentColor=bg)){
            Icon(Icons.Rounded.Wallpaper,null);Spacer(Modifier.width(8.dp));Text("APPLY LIVE WALLPAPER")
        }
        Spacer(Modifier.height(16.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF142237)),shape=RoundedCornerShape(22.dp)){
            Column(Modifier.fillMaxWidth().padding(16.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){Text("Snowfall enabled",color=Color.White,modifier=Modifier.weight(1f));Switch(snowEnabled,{snowEnabled=it;prefs.edit().putBoolean("snowfall",it).apply()})}
                Row(verticalAlignment=Alignment.CenterVertically){Text("Soft background glow",color=Color.White,modifier=Modifier.weight(1f));Switch(glow,{glow=it;prefs.edit().putBoolean("glow",it).apply()})}
                Spacer(Modifier.height(12.dp))
                Text("Snow density ${(density*100).toInt()}%",color=Color.White)
                Slider(value=density,onValueChange={density=it;prefs.edit().putFloat("snow",it).apply()},valueRange=.05f..1f)
                Text("Wind ${(wind*100).toInt()}%",color=Color.White)
                Slider(value=wind,onValueChange={wind=it;prefs.edit().putFloat("wind",it).apply()},valueRange=0f..1f)
                Text("Flake size ${(flakeSize*100).toInt()}%",color=Color.White)
                Slider(value=flakeSize,onValueChange={flakeSize=it;prefs.edit().putFloat("size",it).apply()},valueRange=0f..1f)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("System limitation",color=Color.White,fontWeight=FontWeight.Bold)
        Text("The live wallpaper is installed through Android's wallpaper picker. Snow, wind, flake size and glow are read by the wallpaper service and update while it is running. A launcher cannot replace Android's lock screen, notification shade, Quick Settings, or system status icons.",color=muted)
    }
}
