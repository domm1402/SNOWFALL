package com.domm.snowfall

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Rect
import kotlin.random.Random

class SnowWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = SnowEngine()
    inner class SnowEngine : Engine() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        private var running = false
        private var width = 1
        private var height = 1
        private data class Flake(var x: Float, var y: Float, val r: Float, val speed: Float)
        private val flakes = mutableListOf<Flake>()
        init { repeat(110) { flakes += Flake(Random.nextFloat(), Random.nextFloat(), 1.2f + Random.nextFloat()*3f, .25f + Random.nextFloat()*.8f) } }
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) { super.onSurfaceChanged(holder,format,w,h); width=w; height=h }
        override fun onVisibilityChanged(visible: Boolean) { running = visible; if(visible) drawSnow() }
        override fun onDestroy() { running=false; super.onDestroy() }
        private fun drawSnow() {
            if(!running) return
            var canvas: Canvas? = null
            try { canvas = surfaceHolder.lockCanvas(); if(canvas != null) {
                canvas.drawColor(Color.rgb(9,18,34))
                paint.color=Color.rgb(230,244,255)
                flakes.forEach { f -> canvas.drawCircle(f.x*width, f.y*height, f.r, paint); f.y += f.speed/height*5f; f.x += kotlin.math.sin((f.y*12).toDouble()).toFloat()*.0009f; if(f.y>1.02f) { f.y=-.02f; f.x=Random.nextFloat() } }
            } } finally { if(canvas!=null) surfaceHolder.unlockCanvasAndPost(canvas) }
            if(running) surfaceHolder.setFixedSize(width,height)
            if(running) android.os.Handler(mainLooper).postDelayed({ drawSnow() },32)
        }
    }
}
