package ru.angelovich.chartapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;


class Props {
    private int minY;
    private int maxY;

    ArrayList<float[]> ptsList = new ArrayList<>();
    ArrayList<Paint> paints = new ArrayList<>();

    void updatePaints(ChartData data) {
        paints.clear();
        for (int i = 0; i < data.chartsCount; i++) {
            ChartLine line = data.lines.get(i);
            Paint paint = new Paint();
            paint.setColor(line.color);
            paint.setAntiAlias(false);
            paint.setStrokeWidth(4);
            paints.add(paint);
        }
    }

    void updateLists(ChartData data, int len, int offset) {
        ptsList.clear();
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;

        for (int i = 0; i < data.chartsCount; i++) {
            ptsList.add(new float[len * 4]);
            ChartLine line = data.lines.get(i);
            for (int j = 0; j < len; j++) {
                minY = Integer.min(minY, line.yAxis[offset + j]);
                maxY = Integer.max(maxY, line.yAxis[offset + j]);
            }
        }
    }

    void updateData(ChartData data, int width, int height, int padding, int len, int offset) {
        int baseY = height - padding;
        float koeffY = (float) (height - padding * 2) / (maxY - minY);
        float stepX = (float) width / len;

        for (int i = 0; i < len; i++) {
            int index = i * 4;
            for (int j = 0; j < data.chartsCount; j++) {
                float[] pts = ptsList.get(j);
                int[] line = data.lines.get(j).yAxis;

                pts[index] = (int) (i * stepX);
                pts[index + 2] = (int) ((i + 1) * stepX);

                //some magic
                pts[index + 1] = baseY - ((line[offset + i] - minY) * koeffY);
                pts[index + 3] = baseY - ((line[offset + i + 1] - minY) * koeffY);
            }
        }
    }
}

class BasicChartDrawer implements IChartDrawer, IChartBounds {
    int width, height, padding, len, offset;
    boolean invData, invBounds, invSize, invDraw = false;
    private Props props;
    private ChartData data;

    BasicChartDrawer(int p) {
        padding = p;
        props = new Props();
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        invDraw = invSize = true;
    }

    public void setBounds(int l, int o) {
        len = l;
        offset = o;
        invDraw = invSize = invBounds = true;
    }

    public void setData(ChartData d) {
        data = d;
        invDraw = invSize = invBounds = invData = true;
    }

    public void update(long dt) {
        if (invData) {
            props.updatePaints(data);
        }
        if (invBounds) {
            props.updateLists(data, len, offset);
        }
        if (invSize) {
            props.updateData(data, width, height, padding, len, offset);
        }
        invSize = invBounds = invData = false;
    }

    public void draw(Canvas canvas) {
//        TODO: find out what is wrong here
//        if (!invDraw)
//            return;
//        invDraw = false;
        canvas.drawColor(Color.GRAY);
        for (int i = 0; i < data.chartsCount; i++) {
            float[] pts = props.ptsList.get(i);
            Paint paint = props.paints.get(i);
            canvas.drawLines(pts, paint);
        }
    }
}

class ControllerChartDrawer extends BasicChartDrawer {
    public ControllerChartDrawer(int p) {
        super(p);
    }
}

class ViewChartDrawer extends BasicChartDrawer {
    public ViewChartDrawer(int p) {
        super(p);
    }
}