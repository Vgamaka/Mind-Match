package com.example.mindmatch

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import java.io.IOException
import kotlin.math.ln

class BackgroundVideoView : SurfaceView, SurfaceHolder.Callback {

    private var mp: MediaPlayer? = null
    private var isStarted = false

    //init():
    //set up the essential components for video playback.
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        mp = MediaPlayer()
        holder.addCallback(this)
    }

    // called when the surface (the area where the video will be displayed) is first created
    override fun surfaceCreated(holder: SurfaceHolder) {
        val assetFileDescriptor: AssetFileDescriptor = resources.openRawResourceFd(R.raw.bg_stars)
        try {
            if (!isStarted) {
                isStarted = true
                mp?.apply {
                    setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.length)
                }
            }

            val layoutParams: ViewGroup.LayoutParams = layoutParams.apply {
            }
            setLayoutParams(layoutParams)
            mp?.apply {
                prepare()
                setDisplay(holder)
                isLooping = true
                start()
            }
            setVolume(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //adjusts the volume based on a logarithmic scale to provide a more natural adjustment of audio levels
    private fun setVolume(amount: Int) {
        val max = 100
        val numerator = if (max - amount > 0) ln((max - amount).toDouble()) else 0.0
        val volume = (1 - (numerator / ln(max.toDouble()))).toFloat()
        mp?.setVolume(volume, volume)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // handle surface changes if needed
    }

    //stops the video playback
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mp?.stop() // Stop the video while the surface is destroyed
    }
}

