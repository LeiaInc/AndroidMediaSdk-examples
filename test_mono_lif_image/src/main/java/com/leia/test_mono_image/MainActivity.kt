package com.leia.test_mono_image

import android.app.Activity
import android.os.Bundle
import android.annotation.SuppressLint
import com.leia.core.LogLevel
import com.leia.sdk.LeiaSDK
import com.leia.sdk.LeiaSDK.InitArgs
import com.leia.sdk.views.InputViewsAsset
import com.leia.sdk.views.InterlacedSurfaceView
import com.leia.sdk.views.ScaleType
import com.leiainc.androidsdk.photoformat.MultiviewImageDecoder
import com.leiainc.leiamediasdk.LeiaMediaSDK

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        initTracking()
    }
    private fun initTracking() {
        val initArgs = InitArgs()
        initArgs.platform.activity = this
        initArgs.platform.context = this
        initArgs.platform.logLevel = LogLevel.Info
        try {
            LeiaSDK.createSDK(initArgs)
            LeiaSDK.getInstance()?.startFaceTracking(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        setMode(false)
    }

    @SuppressLint("WrongThread")
    override fun onResume() {
        super.onResume()
        setMode(true)
        val backgroundThread = Thread {
            // Load LIF file (jpeg that has disparity encoded)
            val decoder: MultiviewImageDecoder = MultiviewImageDecoder.getDefault()
            var multiviewImage = decoder.decode(this.assets.open("car.jpg").readBytes())
            // Render SBS stereo form mono image
            val synthizer = LeiaMediaSDK.getInstance(this).createMultiviewSynthesizer(this, false, false, false, true)
            val stereoImage = synthizer.toTiledBitmap(synthizer.synthesizeViews(multiviewImage, 2));
            val interlacedSurfaceView = findViewById<InterlacedSurfaceView>(R.id.interlacedSurfaceView)
            val newViewsAsset =
                InputViewsAsset.createSurfaceFromLoadedBitmap(stereoImage, true)
            interlacedSurfaceView.setScaleType(ScaleType.FIT_CENTER)
            interlacedSurfaceView.setViewAsset(newViewsAsset)
        }
        backgroundThread.start()
        backgroundThread.join()
    }

    override fun onDestroy() {
        super.onDestroy()
        LeiaSDK.shutdownSDK()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        setMode(hasFocus)
    }

    private fun setMode(mode3D: Boolean) {
        LeiaSDK.getInstance()?.let {
            it.enableBacklight(mode3D)
            it.startFaceTracking(mode3D)
        }
    }
}