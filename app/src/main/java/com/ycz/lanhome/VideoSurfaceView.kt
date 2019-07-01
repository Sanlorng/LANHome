package com.ycz.lanhome

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.PipedInputStream
import java.nio.ByteBuffer

class VideoSurfaceView : SurfaceView, SurfaceHolder.Callback {
    companion object {
        const val TAG = "VideoSurfaceView"
        const val PICTURE_WIDTH = 640
        const val PICTURE_HEIGHT = 480
        const val PICTURE_SIZE = PICTURE_HEIGHT * PICTURE_WIDTH * 4

    }

    private var job: Job? = null
    private val scrRect: Rect
    private val dstRect: Rect
    private var flag = false
    private var canvas: Canvas? = null
    private val pixels = ByteArray(PICTURE_SIZE)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        scrRect = Rect(0, 0, PICTURE_WIDTH, PICTURE_HEIGHT)
        dstRect = Rect(800, 5, 800 + PICTURE_WIDTH, 50 + PICTURE_HEIGHT)
        holder.addCallback(this)
//        canvas = holder.lockCanvas()
    }

    fun read(inputStream: PipedInputStream) {
        job?.cancel()
        job = GlobalScope.launch {
            while (true) {
                canvas = holder.lockCanvas()
                canvas?.drawColor(Color.BLACK)
                val len = inputStream.read(pixels, 0, PICTURE_SIZE)
                if (len == -1)
                    break
                val buf = ByteBuffer.wrap(pixels)
                val bitmap =
                    Bitmap.createBitmap(PICTURE_WIDTH, PICTURE_HEIGHT, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(buf)

                canvas?.drawBitmap(bitmap, scrRect, dstRect, null)
                holder.unlockCanvasAndPost(canvas)

            }
        }

    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceCreated(p0: SurfaceHolder?) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        job?.cancel()
    }
}