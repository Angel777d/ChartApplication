package ru.angelovich.chartapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;

import java.util.ArrayList;



class Props {

    int min = 0;
    int max = 0;

    float[] rows = new float[6];
    ArrayList<float[]> ptsList = new ArrayList<>();
    ArrayList<Paint> paints = new ArrayList<>();


    void updatePaints(ChartData data) {
        paints.clear();
        for (int i = 0; i < data.chartsCount; i++) {
            ChartLine line = data.lines.get(i);
            Paint paint = new Paint();
            paint.setColor(line.color);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(6);
            paints.add(paint);
        }
    }

    boolean updateLists(ChartData data, int len, int offset) {
        ptsList.clear();
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int i = 0; i < data.chartsCount; i++) {
            ptsList.add(new float[len * 4]);
            ChartLine line = data.lines.get(i);
            for (int j = 0; j < len; j++) {
                minY = Integer.min(minY, line.yAxis[offset + j]);
                maxY = Integer.max(maxY, line.yAxis[offset + j]);
            }
        }

        return updateRange(minY, maxY);
    }

    boolean updateRange(int minY, int maxY) {
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

    void updateData(ChartData data, int width, int height, int len, int offset) {
        int baseY = height;
        float koeffY = (float) height / (max - min);
        float stepX = (float) width / len;

        for (int i = 0; i < len; i++) {
            int index = i * 4;
            for (int j = 0; j < data.chartsCount; j++) {
                float[] pts = ptsList.get(j);
                int[] line = data.lines.get(j).yAxis;

                pts[index] = (int) (i * stepX);
                pts[index + 2] = (int) ((i + 1) * stepX);

                //some magic
                pts[index + 1] = baseY - ((line[offset + i] - min) * koeffY);
                pts[index + 3] = baseY - ((line[offset + i + 1] - min) * koeffY);
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

abstract class BasicChartDrawer implements IChartDrawer, IChartBounds {

    abstract protected void startDraw(Canvas canvas);

    int buffer = 0;
    boolean invData, invBounds, invSize, invDraw = false;
    float leftEdge = 0;
    float rightEdge = 1;
    int width, height;

    Props props;
    ChartData data;

    BasicChartDrawer() {
        props = new Props();
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        invDraw = invSize = true;
    }

    public void setBounds(float leftEdge, float rightEdge) {
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
        invDraw = invSize = invBounds = true;
    }

    public void setData(ChartData d) {
        data = d;
        invDraw = invSize = invBounds = invData = true;
    }

    public void update(long dt) {

        int size = data.size - 1;
        int offset = Math.round(size * leftEdge);
        int len = Math.round(size * rightEdge) - offset;

        if (invData) {
            props.updatePaints(data);
        }
        if (invBounds) {
            props.updateLists(data, len, offset);
        }
        if (invSize) {
            props.updateData(data, width, height, len, offset);
        }
        invSize = invBounds = invData = false;
    }

    public final void draw(Canvas canvas) {
//        TODO: find out what is wrong here
        if (invDraw)
            buffer = 0;
        invDraw = false;

        if (buffer > 10) {
            return;
        }
        ++buffer;

        startDraw(canvas);
    }


    void drawBG(Canvas canvas) {
        canvas.drawColor(Color.argb(0, 255, 255, 255));
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

    }

    void drawCharts(Canvas canvas) {
        for (int i = 0; i < data.chartsCount; i++) {
            float[] pts = props.ptsList.get(i);
            Paint paint = props.paints.get(i);
            canvas.drawLines(pts, paint);
        }
    }
}

class ControllerChartDrawer extends BasicChartDrawer {
    Paint rectPaint;

    public ControllerChartDrawer() {
        rectPaint = new Paint();
        rectPaint.setColor(Color.RED);
        rectPaint.setAlpha(127);
    }

    public void update(long dt) {
        int offset = 0;
        int len = data.size - 1;

        if (invData) {
            props.updatePaints(data);
        }
        if (invBounds) {
            props.updateLists(data, len, offset);
        }
        if (invSize) {
            props.updateData(data, width, height, len, offset);
        }
        invSize = invBounds = invData = false;
    }

    @Override
    public void startDraw(Canvas canvas) {
        drawBG(canvas);
        drawCharts(canvas);

        Rect rect = new Rect((int) (width * leftEdge), 0, (int) (width * rightEdge), height);
        canvas.drawRect(rect, rectPaint);
    }
}

class ViewChartDrawer extends BasicChartDrawer {

    Paint gridPaint;

    public ViewChartDrawer() {
        gridPaint = new Paint();
        gridPaint.setColor(Color.WHITE);
        gridPaint.setAlpha(80);
        gridPaint.setStrokeWidth(2);
    }

    public void update(long dt) {
        int size = data.size - 1;
        int offset = (int) Math.floor(size * leftEdge);
        int len = (int) Math.ceil(size * rightEdge) - offset;

        if (invData) {
            props.updatePaints(data);
        }
        if (invBounds) {
            props.updateLists(data, len, offset);
        }
        if (invSize) {
            props.updateData(data, width, height, len, offset);
        }
        invSize = invBounds = invData = false;
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