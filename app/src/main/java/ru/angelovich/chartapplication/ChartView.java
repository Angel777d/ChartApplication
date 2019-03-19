package ru.angelovich.chartapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class ChartView extends SurfaceView implements SurfaceHolder.Callback {
    DrawThread drawThread;
    IDrawer drawer;

    public ChartView(Context context, IDrawer drawer) {
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

            @Override
            boolean isInvalid() {
                return drawer.isInvalid();
            }
        };


        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawThread.setRunning(false);
        drawThread.close();
        drawThread = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}

abstract class DrawThread extends Thread {

    private final SurfaceHolder surfaceHolder;

    private boolean runFlag = false;

    DrawThread(SurfaceHolder surfaceHolder, Resources resources) {
        this.surfaceHolder = surfaceHolder;
        timer = new TickGenerator();
    }

    private TickGenerator timer;

    private boolean closeRetry;

    @Override
    public void run() {
        while (runFlag) {
            long dt = timer.get_dt();
            process(dt);

            if (isInvalid()) {
                Canvas canvas = surfaceHolder.lockCanvas(null);
                if (canvas == null)
                    return;

                synchronized (surfaceHolder) {
                    draw(canvas);
                }
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            try {
                sleep(TickGenerator.FRAME_TIME / 3);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }

        while (closeRetry) {
            try {
                join();
                closeRetry = false;
            } catch (InterruptedException e) {
                // try again
            }
        }
    }

    void setRunning(boolean run) {
        runFlag = run;
    }

    void close() {
        closeRetry = true;
    }

    abstract boolean isInvalid();

    abstract void process(long dt);

    abstract void draw(Canvas canvas);

    class TickGenerator {
        private static final long TARGET_FRAME_RATE = 60;
        private static final long FRAME_TIME = 1000 / TARGET_FRAME_RATE;
        private long prevTime;

        TickGenerator() {
            prevTime = System.currentTimeMillis();
        }

        long get_dt() {
            long now = System.currentTimeMillis();
            long result = now - prevTime;
            prevTime = now;
            return result;
        }
    }
}
