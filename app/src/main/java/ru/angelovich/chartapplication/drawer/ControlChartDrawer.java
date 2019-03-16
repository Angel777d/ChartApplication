package ru.angelovich.chartapplication.drawer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class ControlChartDrawer extends BasicDrawer {
    private static final int STROKE_SIZE = 16;

    private Paint rectPaint;
    private Paint coverPaint;

    private float leftEdge, rightEdge;

    public ControlChartDrawer() {
        initColors();
    }

    private void initColors() {
        rectPaint = new Paint();
        rectPaint.setColor(Color.GRAY);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setAlpha(100);
        rectPaint.setStrokeWidth(STROKE_SIZE);

        coverPaint = new Paint();
        coverPaint.setColor(Color.GRAY);
        coverPaint.setAlpha(50);
    }

    //do not invalidate charts here.
    public void setBounds(float leftEdge, float rightEdge) {
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
        invalidateDraw();
    }

    @Override
    public void startDraw(Canvas canvas) {
        drawBG(canvas);
        drawCharts(canvas);

        Rect rect = new Rect(0, 0, 0, stage.height);
        int lPos = (int) (stage.width * leftEdge);
        int rPos = (int) (stage.width * rightEdge);

        rect.right = lPos - STROKE_SIZE / 2;
        canvas.drawRect(rect, coverPaint);

        rect.left = rPos + STROKE_SIZE / 2;
        rect.right = stage.width;
        canvas.drawRect(rect, coverPaint);

        rect.left = lPos;
        rect.right = rPos;
        canvas.drawRect(rect, rectPaint);
    }
}
