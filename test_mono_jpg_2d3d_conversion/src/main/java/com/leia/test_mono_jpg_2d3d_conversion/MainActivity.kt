package com.leia.test_mono_jpg_2d3d_conversion

import android.app.Activity
import android.os.Bundle
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.leia.core.LogLevel
import com.leia.sdk.LeiaSDK
import com.leia.sdk.LeiaSDK.InitArgs
import com.leia.sdk.views.InputViewsAsset
import com.leia.sdk.views.InterlacedSurfaceView
import com.leia.sdk.views.ScaleType
import com.leiainc.androidsdk.photoformat.MultiviewImage
import com.leiainc.androidsdk.photoformat.ViewPoint
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
            // Load regular jpeg file as Bitmap
            var img:Bitmap = BitmapFactory.decodeStream(this.assets.open("cat1.jpg"))
            // Create single view Multiview image
            var multiviewImage = MultiviewImage()
            multiviewImage.viewPoints.add(0, ViewPoint(img, null, 0f,0f))
            val synthizer = LeiaMediaSDK.getInstance(this).createMultiviewSynthesizer(this, false, false, false, true)
            // Populate disparity map for 2d3d conversion
            multiviewImage = synthizer.populateDisparityMaps(multiviewImage);
            // Render SBS stereo form mono image
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