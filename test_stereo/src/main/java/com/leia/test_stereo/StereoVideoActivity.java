package com.leia.test_stereo;

import static com.leiainc.androidsdk.video.stereo.TextureShape.LANDSCAPE;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
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
import com.leiainc.androidsdk.video.RenderConfig;
import com.leiainc.androidsdk.video.stereo.StereoVideoSurfaceRenderer;
import com.leia.sdk.LeiaSDK;

public class StereoVideoActivity extends Activity implements com.leia.sdk.LeiaSDK.Delegate{
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

        // Init Exoplayer
        mPlayer = new SimpleExoPlayer.Builder(this).build();

        //Init 3d view
        InputViewsAsset newViewsAsset = new InputViewsAsset();
        RenderConfig cfg = RenderConfig.getDefaultRenderConfig();
        newViewsAsset.CreateEmptySurfaceForVideo(
                cfg.screenWidth,
                cfg.screenHeight,
                surfaceTexture -> {
                    surfaceTexture.setDefaultBufferSize(cfg.screenWidth, cfg.screenHeight);
                    configureGo4v(surfaceTexture);
                });
        mInterlacedView.setViewAsset(newViewsAsset);
        mInterlacedView.setScaleType(ScaleType.FIT_CENTER);
        //Turn on 3d backlight
        LeiaSDK.getInstance().enableBacklight(true);

    }

    private void configureGo4v(SurfaceTexture surfaceTexture) {
        if (mStereoVideoSurfaceRenderer == null) {
            mStereoVideoSurfaceRenderer =
                    new StereoVideoSurfaceRenderer(
                            this,
                            new Surface(surfaceTexture),
                            LANDSCAPE,
                            null,
                            renderSurfaceTexture -> {
                                configureExoplayer(surfaceTexture, renderSurfaceTexture);
                            },
                            true);
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayer.setPlayWhenReady(true);
        LeiaSDK.getInstance().onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mStereoVideoSurfaceRenderer != null) {
            mStereoVideoSurfaceRenderer.release();
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
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
