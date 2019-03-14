package ru.angelovich.chartapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

/**
 * Created by zhaosong on 2018/6/16.
 */

public class ChartView extends TextureView implements TextureView.SurfaceTextureListener {
    DrawThread drawThread;
    IChartDrawer drawer;
    Surface mSurface;

    public ChartView(Context context) {
        this(context, null, 0);
    }

    public ChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChartView(Context context, IChartDrawer drawer) {
        this(context, null, 0);
        this.drawer = drawer;

        setFocusable(true);
        setOpaque(false);

        setSurfaceTextureListener(this);

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        drawer.setSize(width, height);
        drawThread = new DrawThread(mSurface, getResources()) {
            @Override
            void process(long dt) {
                drawer.update(dt);
            }

            @Override
            void draw(Canvas canvas) {
                drawer.draw(canvas);
            }
        };
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int w, int h) {
        drawer.setSize(w, h);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again
            }
        }
        mSurface.release();
        mSurface = null;
        return true;
    }
}

abstract class DrawThread extends Thread {

    class TickGenerator {
        private static final long TARGET_FRAME_RATE = 30;
        private static final long FRAME_TIME = 1000 / TARGET_FRAME_RATE;
        private long prevTime;

        TickGenerator() {
            prevTime = System.currentTimeMillis();
        }

        long get_dt() {
            long now = System.currentTimeMillis();
            long elapsedTime = now - prevTime;
            long result = 0;

            if (elapsedTime >= FRAME_TIME) {
                result = now - prevTime;
                prevTime = now;
            }
            return result;
        }
    }

    private boolean runFlag = false;
    private final Surface surface;
    private TickGenerator timer;

    DrawThread(Surface surface, Resources resources) {
        this.surface = surface;
        timer = new TickGenerator();
    }

    void setRunning(boolean run) {
        runFlag = run;
    }

    @Override
    public void run() {
        while (runFlag) {
            long dt = timer.get_dt();

            if (dt > 0) {
                onTick(dt);
            }
        }
    }

    private void onTick(long dt) {
        process(dt);

        Canvas canvas = surface.lockCanvas(null);
        if (canvas == null)
            return;

        synchronized (surface) {
            draw(canvas);
        }
        surface.unlockCanvasAndPost(canvas);
    }

    abstract void process(long dt);

    abstract void draw(Canvas canvas);
}
