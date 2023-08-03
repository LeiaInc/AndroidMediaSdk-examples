package com.leia.test_convergence;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.widget.ImageButton;
import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.leia.core.LogLevel;
import com.leia.sdk.views.InputViewsAsset;
import com.leia.sdk.views.InterlacedSurfaceView;
import com.leia.sdk.views.ScaleType;
import com.leia.test_stereo.R;
import com.leia.sdk.LeiaSDK;
import com.leiainc.leiamediasdk.LeiaMediaSDK;
import com.leiainc.leiamediasdk.interfaces.StereoVideoSurfaceRenderer;

public class StereoConvergenceVideoActivity extends Activity implements com.leia.sdk.LeiaSDK.Delegate{
    @BindView(R.id.interlacedView)
    InterlacedSurfaceView mInterlacedView;
    private SimpleExoPlayer mPlayer;
    private StereoVideoSurfaceRenderer mStereoVideoSurfaceRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_stereo_activity);

        //Init LeiaSDK and start tracking
        try {
            initTracking();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        //Bind view
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize() {
        if (mPlayer != null) {
            mPlayer.release();
        }
        // Init Exoplayer
        mPlayer = new SimpleExoPlayer.Builder(this).build();

        //Init 3d view
        InputViewsAsset newViewsAsset = new InputViewsAsset();
        newViewsAsset.CreateEmptySurfaceForVideo(
                1656,
                1036,
                surfaceTexture -> {
                    surfaceTexture.setDefaultBufferSize(1656, 1036);
                    configureGo4v(surfaceTexture);
                });
        mInterlacedView.setViewAsset(newViewsAsset);
        mInterlacedView.setScaleType(ScaleType.FIT_CENTER);

        //Turn on 3d backlight
        LeiaSDK.getInstance().enableBacklight(true);
    }

    private void configureGo4v(SurfaceTexture surfaceTexture) {
        if (mStereoVideoSurfaceRenderer != null) {
            mStereoVideoSurfaceRenderer.release();
            mStereoVideoSurfaceRenderer = null;
        }
        mStereoVideoSurfaceRenderer = LeiaMediaSDK.getInstance(this).createStereoVideoSurfaceRenderer(
                        this,
                        new Surface(surfaceTexture),
                        renderSurfaceTexture -> configureExoplayer(surfaceTexture, renderSurfaceTexture));
        mStereoVideoSurfaceRenderer.setAutoConvergence(true);
    }

    private void configureExoplayer( SurfaceTexture DepthViewSurface, SurfaceTexture surfaceTexture) {

        mPlayer.addVideoListener(
                new VideoListener() {
                    @Override
                    public void onVideoSizeChanged(
                            int width,
                            int height,
                            int unappliedRotationDegrees,
                            float pixelWidthHeightRatio) {
                        //This is half view video that needs to be stretched 2x to show properly
                        surfaceTexture.setDefaultBufferSize(width*2, height);
                        DepthViewSurface.setDefaultBufferSize((int) width*2, (int) height);
                    }
                });

        mPlayer.setVideoSurface(new Surface(surfaceTexture));

        String userAgent = Util.getUserAgent(this, "exoplayer2example");
        Uri uri = Uri.parse("asset:///avatar_2x1.mp4");
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent);
        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

        mPlayer.prepare(loopingSource);
    }

    @OnClick(R.id.playButton)
    void playOrPause() {
        boolean playing = !mPlayer.getPlayWhenReady();
        mPlayer.setPlayWhenReady(playing);
        ImageButton playButton = findViewById(R.id.playButton);
        playButton.setBackgroundResource(
                playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }

    private void setMode(boolean mode3D) {
        LeiaSDK instance = LeiaSDK.getInstance();
        if (instance != null) {
            instance.enableBacklight(mode3D);
            instance.startFaceTracking(mode3D);
            mInterlacedView.setSingleViewMode(!mode3D);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setMode(false);
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMode(true);
        if (mPlayer == null) {
            initialize();
        }
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setMode(hasFocus);
        if (mPlayer != null && !hasFocus) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        if (mStereoVideoSurfaceRenderer != null) {
            mStereoVideoSurfaceRenderer.release();
            mStereoVideoSurfaceRenderer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeiaSDK.shutdownSDK();
    }

    private void initTracking() throws Exception {
        com.leia.sdk.LeiaSDK.InitArgs initArgs = new com.leia.sdk.LeiaSDK.InitArgs();
        initArgs.platform.logLevel = LogLevel.Trace;
        initArgs.platform.context = this.getApplicationContext();
        initArgs.faceTrackingServerLogLevel = LogLevel.Trace;
        initArgs.enableFaceTracking = true;
        com.leia.sdk.LeiaSDK.createSDK(initArgs);
    }

    @Override
    public void didInitialize(@NonNull LeiaSDK leiaSDK) {
        assert (leiaSDK.isInitialized());
        com.leia.sdk.LeiaSDK leiaSDKInstance = com.leia.sdk.LeiaSDK.getInstance();
        if (leiaSDKInstance != null) leiaSDKInstance.enableBacklight(true);
    }

    @Override
    public void onFaceTrackingFatalError(@NonNull LeiaSDK leiaSDK) {

    }

    @Override
    public void onFaceTrackingStarted(@NonNull LeiaSDK leiaSDK) {

    }

    @Override
    public void onFaceTrackingStopped(@NonNull LeiaSDK leiaSDK) {

    }
}
