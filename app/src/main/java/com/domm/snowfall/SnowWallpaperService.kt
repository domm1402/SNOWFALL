package com.domm.snowfall

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.random.Random

class SnowWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = SnowEngine()

    inner class SnowEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var running = false
        private var width = 1
        private var height = 1
        private data class Flake(var x: Float, var y: Float, var radius: Float, var speed: Float)
        private val flakes = List(220) { Flake(Random.nextFloat(), Random.nextFloat(), 1f + Random.nextFloat() * 2.4f, .4f + Random.nextFloat() * 1.2f) }
        private val drawRunnable = object : Runnable { override fun run() { render() } }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            width = w.coerceAtLeast(1)
            height = h.coerceAtLeast(1)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            handler.removeCallbacks(drawRunnable)
            if (visible) handler.post(drawRunnable)
        }

        override fun onDestroy() {
            running = false
            handler.removeCallbacks(drawRunnable)
            super.onDestroy()
        }

        private fun render() {
            if (!running) return
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.rgb(7, 17, 31))
                    val prefs = this@SnowWallpaperService.getSharedPreferences("snowfall", MODE_PRIVATE)
                    val intensity = prefs.getFloat("snow", .68f)
                    val wind = prefs.getFloat("wind", .42f)
                    val flakeSize = prefs.getFloat("size", .5f)
                    val glow = prefs.getBoolean("glow", true)
                    val enabled = prefs.getBoolean("snowfall", true)
                    if (glow) {
                        paint.color = Color.rgb(19, 42, 62)
                        canvas.drawCircle(width * .5f, height * .35f, width * .45f, paint)
                    }
                    if (enabled) {
                        paint.color = Color.rgb(238, 247, 255)
                        flakes.take((flakes.size * intensity).toInt()).forEach { f ->
                            canvas.drawCircle(f.x * width, f.y * height, f.radius * (0.7f + flakeSize), paint)
                            f.y += f.speed * (0.0015f + wind * .0022f)
                            f.x += kotlin.math.sin((f.y * 8).toDouble()).toFloat() * wind * .0015f
                            if (f.y > 1.02f) { f.y = -.02f; f.x = Random.nextFloat() }
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                if (canvas != null) try { surfaceHolder.unlockCanvasAndPost(canvas) } catch (_: Exception) { }
            }
            if (running) handler.postDelayed(drawRunnable, 33L)
        }
    }
}
