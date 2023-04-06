package com.leia.test_2x1_image

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import com.leia.core.LogLevel
import com.leia.sdk.LeiaSDK
import com.leia.sdk.LeiaSDK.InitArgs
import com.leia.sdk.views.InputViewsAsset
import com.leia.sdk.views.InterlacedSurfaceView

class Image2x1Activity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTracking()

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.car_2x1)
        val asset = InputViewsAsset()
        val interlacedSurfaceView = findViewById<InterlacedSurfaceView>(R.id.interlacedSurfaceView)
        asset.CreateSurfaceFromLoadedBitmap(bitmap)
        interlacedSurfaceView.setViewAsset(asset)
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

    override fun onResume() {
        super.onResume()
        setMode(true)
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