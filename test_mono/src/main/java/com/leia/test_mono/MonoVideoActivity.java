package com.leia.test_mono;

import static java.lang.String.format;
import static java.util.Locale.US;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

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
import com.leia.sdk.LeiaSDK;
import com.leia.sdk.views.InputViewsAsset;
import com.leia.sdk.views.InterlacedSurfaceView;
import com.leia.sdk.views.ScaleType;
import com.leiainc.androidsdk.video.RenderConfig;
import com.leiainc.androidsdk.video.mono.MonoVideoSurfaceRenderer;

public class MonoVideoActivity extends Activity implements com.leia.sdk.LeiaSDK.Delegate{

    @BindView(R.id.interlacedView)
    InterlacedSurfaceView mInterlacedView;

    private SimpleExoPlayer mPlayer;
    private MonoVideoSurfaceRenderer mMonoVideoSurfaceRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_mono_activity);
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
        // Setup 3d view to render video
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
        // Setup 3d effect slider
        TextView gainTextView = findViewById(R.id.gain_textview);
        SeekBar gainSeekBar = findViewById(R.id.gain_seekbar);
        gainSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        if (mMonoVideoSurfaceRenderer != null) {
                            mMonoVideoSurfaceRenderer.setGainMultiplier(progress / 100.0f); //0..1
                            mMonoVideoSurfaceRenderer.requestRender();
                        }
                        String gainString = format(US, "Gain = %d%%", (int) (progress));
                        gainTextView.setText(gainString);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
        gainSeekBar.setProgress(50);
    }

    private void configureGo4v(SurfaceTexture surfaceTexture) {
        if (mMonoVideoSurfaceRenderer == null) {
            mMonoVideoSurfaceRenderer =
                    new MonoVideoSurfaceRenderer(
                            this,
                            new Surface(surfaceTexture),
                            null,
                            monoSurfaceTexture -> {
                                configureExoplayer(surfaceTexture, monoSurfaceTexture);
                            },
                            new RenderConfig(2, 1, 2f, 5, 2400, 1600));
        }
    }

    private void configureExoplayer(
            SurfaceTexture DepthViewSurface, SurfaceTexture surfaceTexture) {
        mPlayer.addVideoListener(
                new VideoListener() {
                    @Override
                    public void onVideoSizeChanged(
                            int width,
                            int height,
                            int unappliedRotationDegrees,
                            float pixelWidthHeightRatio) {
                        // We transform mono to stereo
                        int stereo_width = width * 2;
                        surfaceTexture.setDefaultBufferSize(stereo_width, height);
                        DepthViewSurface.setDefaultBufferSize((int) stereo_width, (int) height);
                    }
                });

        mPlayer.setVideoSurface(new Surface(surfaceTexture));

        String userAgent = Util.getUserAgent(this, "exoplayer2example");

        Uri uri = Uri.parse("asset:///animals.mp4");
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent);
        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
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
        LeiaSDK.getInstance().enableBacklight(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMonoVideoSurfaceRenderer != null) {
            mMonoVideoSurfaceRenderer.release();
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
