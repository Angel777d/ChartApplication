package ru.angelovich.chartapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by zhaosong on 2018/6/16.
 */

public class ChartView extends SurfaceView implements SurfaceHolder.Callback {
    DrawThread drawThread;
    IChartDrawer drawer;

    public ChartView(Context context, IChartDrawer drawer) {
        super(context);

        this.drawer = drawer;
        drawer.setSize(getWidth(), getHeight());

        setFocusable(true);
        getHolder().addCallback(this);

        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawer.setSize(w, h);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        drawThread = new DrawThread(getHolder(), getResources()) {
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
    public void surfaceDestroyed(SurfaceHolder holder) {
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
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

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
    private final SurfaceHolder surfaceHolder;
    private TickGenerator timer;

    DrawThread(SurfaceHolder surfaceHolder, Resources resources) {
        this.surfaceHolder = surfaceHolder;
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
        Canvas canvas = surfaceHolder.lockCanvas(null);
        process(dt);
        synchronized (surfaceHolder) {
            draw(canvas);
        }
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    abstract void process(long dt);

    abstract void draw(Canvas canvas);
}
