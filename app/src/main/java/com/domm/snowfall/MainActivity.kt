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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private val ink=Color(0xFF081321); private val panel=Color(0xFF122237); private val ice=Color(0xFF9BD8FF); private val mist=Color(0xFFDAE9F6)
class MainActivity: ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){ super.onCreate(savedInstanceState); setContent { SnowApp(this) } } }

@Composable fun SnowApp(activity: Activity){ var intensity by remember { mutableFloatStateOf(.65f) }; var speed by remember { mutableFloatStateOf(.45f) }; var glow by remember { mutableStateOf(true) }; var active by remember { mutableStateOf(false) }; var page by remember { mutableStateOf(0) }
    MaterialTheme(colorScheme=darkColorScheme(primary=ice,background=ink,surface=panel)){ Surface(color=ink, modifier=Modifier.fillMaxSize()){ Column(Modifier.fillMaxSize().padding(horizontal=22.dp)) {
        Spacer(Modifier.height(24.dp)); Row(verticalAlignment=Alignment.CenterVertically){ Icon(Icons.Rounded.AcUnit,null,tint=ice,modifier=Modifier.size(27.dp)); Spacer(Modifier.width(10.dp)); Text("SNOWFALL",style=MaterialTheme.typography.labelLarge,letterSpacing=3.sp,color=mist,fontWeight=FontWeight.Bold); Spacer(Modifier.weight(1f)); Icon(Icons.Rounded.Settings,null,tint=mist) }
        Spacer(Modifier.height(30.dp)); Text(if(page==0)"Your winter" else "Customize",style=MaterialTheme.typography.headlineMedium,color=Color.White,fontWeight=FontWeight.SemiBold); Text("A quieter, colder world.",color=mist.copy(alpha=.7f),style=MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp)); Box(Modifier.fillMaxWidth().height(290.dp).clip(RoundedCornerShape(28.dp))) { SnowPreview(intensity,speed); Column(Modifier.align(Alignment.BottomStart).padding(20.dp)){ Text("WINTER  /  01",color=Color.White.copy(alpha=.7f),style=MaterialTheme.typography.labelSmall,letterSpacing=2.sp); Text("Let it snow.",color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Medium) } }
        Spacer(Modifier.height(20.dp)); Row(verticalAlignment=Alignment.CenterVertically){ Column(Modifier.weight(1f)){ Text("Winter theme",color=Color.White,fontWeight=FontWeight.SemiBold); Text(if(active)"Live wallpaper enabled" else "Ready to apply",color=mist.copy(alpha=.65f),style=MaterialTheme.typography.bodySmall) }; Switch(checked=active,onCheckedChange={ if(it){ try { activity.startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,ComponentName(activity,SnowWallpaperService::class.java))) }catch(_:Exception){} } }) }
        Spacer(Modifier.height(14.dp)); SettingSlider("Snowfall density",intensity,{intensity=it},"Light","Heavy"); Spacer(Modifier.height(10.dp)); SettingSlider("Snow drift",speed,{speed=it},"Slow","Fast")
        Spacer(Modifier.height(15.dp)); Row(verticalAlignment=Alignment.CenterVertically){ Column(Modifier.weight(1f)){Text("Soft glow",color=Color.White,fontWeight=FontWeight.Medium);Text("Subtle atmospheric light",color=mist.copy(alpha=.6f),style=MaterialTheme.typography.bodySmall)};Switch(glow,{glow=it}) }
        Spacer(Modifier.height(16.dp)); Button(onClick={ try { activity.startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,ComponentName(activity,SnowWallpaperService::class.java))) }catch(_:Exception){} },modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=ice,contentColor=ink)){ Text("APPLY LIVE WALLPAPER",fontWeight=FontWeight.Bold,letterSpacing=1.sp) }
        Spacer(Modifier.height(14.dp)); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){ Text("WALLPAPER  •  WIDGETS  •  ICONS",color=mist.copy(alpha=.45f),style=MaterialTheme.typography.labelSmall,letterSpacing=1.sp) }
    } } }
}
@Composable private fun SettingSlider(title:String,value:Float,onChange:(Float)->Unit,left:String,right:String){ Column { Row{Text(title,color=mist,fontWeight=FontWeight.Medium);Spacer(Modifier.weight(1f));Text("${(value*100).toInt()}%",color=ice)}; Slider(value=value,onValueChange=onChange,colors=SliderDefaults.colors(thumbColor=ice,activeTrackColor=ice,inactiveTrackColor=panel));Row{Text(left,color=mist.copy(alpha=.5f),style=MaterialTheme.typography.labelSmall);Spacer(Modifier.weight(1f));Text(right,color=mist.copy(alpha=.5f),style=MaterialTheme.typography.labelSmall)} } }
@Composable private fun SnowPreview(density:Float,speed:Float){ val infinite= rememberInfiniteTransition(label="snow"); val drift by infinite.animateFloat(0f,1f,infiniteRepeatable(tween(9000, easing=LinearEasing)),label="drift"); val flakes= remember { List(140){ Triple(Random.nextFloat(),Random.nextFloat(),1f+Random.nextFloat()*3f) } }; Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF253F5C),Color(0xFF0C1A2C),Color(0xFF060E19))))) { Canvas(Modifier.fillMaxSize()){ val count=(flakes.size*density).toInt(); flakes.take(count).forEach { (x,y,r)-> val yy=(y+drift*speed*.35f)%1f; drawCircle(Color.White.copy(alpha=.35f+.65f*(r/4)),radius=r*1.5f,center=androidx.compose.ui.geometry.Offset(x*size.width,yy*size.height)) } }; if(density>.6f) Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(ice.copy(alpha=if(glowPreview()) .07f else 0f),Color.Transparent)))) } }
private fun glowPreview()=true
