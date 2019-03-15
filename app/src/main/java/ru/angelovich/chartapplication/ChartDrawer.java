package ru.angelovich.chartapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;


class Props {

    int size = 0;
    private int min = 0;
    private int max = 0;
    private ArrayList<ChartLine> visibleLines = new ArrayList<>();
    float[] rows = new float[6];
    ArrayList<float[]> ptsList = new ArrayList<>();
    ArrayList<Paint> paints = new ArrayList<>();

    void updateData(ChartData data) {
        paints.clear();
        size = 0;
        visibleLines.clear();

        for (int i = 0; i < data.lines.size(); i++) {
            ChartLine line = data.lines.get(i);
            if (!line.visible)
                continue;
            visibleLines.add(line);
            ++size;
            Paint paint = new Paint();
            paint.setColor(line.color);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(6);
            paints.add(paint);
        }
    }

    boolean updateRange(int len, int offset) {
        ptsList.clear();
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int i = 0; i < size; i++) {
            ChartLine line = visibleLines.get(i);
            ptsList.add(new float[len * 4]);

            for (int j = 0; j < len; j++) {
                minY = Integer.min(minY, line.yAxis[offset + j]);
                maxY = Integer.max(maxY, line.yAxis[offset + j]);
            }
        }

        return setMinMax(minY, maxY);
    }

    boolean setMinMax(int minY, int maxY) {
        min = 0;
        int newMax = max;

        if (maxY * 1.05f > max) {
            newMax = (int) Math.ceil(maxY * 1.1f / 10) * 10;
        } else if (1f * max / (max - maxY) > 0.3) {
            newMax = (int) Math.ceil(maxY * 1.1f / 10) * 10;
        }

        if (max != newMax) {
            max = newMax;
            return true;
        }

        return false;
    }

    void updateSize(int width, int height, int len, int offset) {
        int baseY = height;
        float koeffY = (float) height / (max - min);
        float stepX = (float) width / len;

        for (int i = 0; i < len; i++) {
            int index = i * 4;
            for (int j = 0; j < size; j++) {
                ChartLine line = visibleLines.get(j);
                if (!line.visible)
                    continue;

                float[] pts = ptsList.get(j);

                pts[index] = (int) (i * stepX);
                pts[index + 2] = (int) ((i + 1) * stepX);

                //some magic
                pts[index + 1] = baseY - ((line.yAxis[offset + i] - min) * koeffY);
                pts[index + 3] = baseY - ((line.yAxis[offset + i + 1] - min) * koeffY);
            }
        }

        //raws
        int step = (max - min) / 6;
        step = step > 0 ? step : 1;

        for (int i = 0; i < 6; i++) {
            rows[i] = baseY - (step * i * koeffY);
        }
    }
}

abstract class BasicDrawer implements IDrawer, IChartBounds {

    abstract protected void startDraw(Canvas canvas);

    float leftEdge = 0;
    float rightEdge = 1;
    int width, height;

    private boolean invData, invBounds, invSize, invDraw = false;
    private int bgColor = Color.MAGENTA;

    Props props;
    ChartData data;

    BasicDrawer() {
        props = new Props();
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        invDraw = invSize = true;
    }

    @Override
    public boolean isInvalid() {
        return invDraw;
    }

    public void setBounds(float leftEdge, float rightEdge) {
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
        invDraw = invSize = invBounds = true;
    }

    public void setBgColor(int color) {
        bgColor = color;
        invDraw = true;
    }

    public void setData(ChartData d) {
        data = d;
        invDraw = invSize = invBounds = invData = true;
    }

    abstract public void update(long dt);

    void validate(int len, int offset) {
        if (invData) {
            props.updateData(data);
        }
        if (invBounds) {
            props.updateRange(len, offset);
        }
        if (invSize) {
            props.updateSize(width, height, len, offset);
        }
        invSize = invBounds = invData = false;
    }

    public final void draw(Canvas canvas) {
        invDraw = false;
        startDraw(canvas);
    }

    void drawBG(Canvas canvas) {
        canvas.drawColor(bgColor);
    }

    void drawCharts(Canvas canvas) {
        for (int i = 0; i < props.size; i++) {
            float[] pts = props.ptsList.get(i);
            Paint paint = props.paints.get(i);
            canvas.drawLines(pts, paint);
        }
    }
}

class ControllerDrawer extends BasicDrawer {
    Paint rectPaint;
    private static final int STROKE_SIZE = 20;
    Paint coverPaint;

    public ControllerDrawer() {
        rectPaint = new Paint();
        rectPaint.setColor(Color.GRAY);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setAlpha(100);
        rectPaint.setStrokeWidth(STROKE_SIZE);

        coverPaint = new Paint();
        coverPaint.setColor(Color.GRAY);
        coverPaint.setAlpha(50);
    }

    public void update(long dt) {
        int offset = 0;
        int len = data.size - 1;

        validate(len, offset);
    }

    @Override
    public void startDraw(Canvas canvas) {
        drawBG(canvas);
        drawCharts(canvas);

        Rect rect = new Rect(0, 0, 0, height);
        int lPos = (int) (width * leftEdge);
        int rPos = (int) (width * rightEdge);

        rect.right = lPos - STROKE_SIZE / 2;
        canvas.drawRect(rect, coverPaint);

        rect.left = rPos + STROKE_SIZE / 2;
        rect.right = width;
        canvas.drawRect(rect, coverPaint);

        rect.left = lPos;
        rect.right = rPos;
        canvas.drawRect(rect, rectPaint);
        //Rect rect = new Rect((int) (width * leftEdge), 0, (int) (width * rightEdge), height);
    }
}

class ViewDrawer extends BasicDrawer {

    private Paint gridPaint;

    public ViewDrawer() {
        gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setAlpha(80);
        gridPaint.setStrokeWidth(2);
    }

    public void update(long dt) {
        int size = data.size - 1;
        int offset = (int) Math.floor(size * leftEdge);
        int len = (int) Math.ceil(size * rightEdge) - offset;

        validate(len, offset);
    }

    @Override
    public void startDraw(Canvas canvas) {
        drawBG(canvas);
        drawRows(canvas);
        drawCharts(canvas);

    }

    void drawRows(Canvas canvas) {
        for (int i = 0; i < props.rows.length; i++) {
            canvas.drawLine(0, props.rows[i], width, props.rows[i], gridPaint);
        }
    }
}