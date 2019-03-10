package ru.angelovich.chartapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

class ChartDrawer implements IChartDrawer {
    private Paint paint;
    private ChartData data = null;

    private int width;
    private int height;

    private int offset;
    private int len;

    private int minY;
    private int maxY;

    private ArrayList<float[]> ptsList = new ArrayList<>();

    ChartDrawer(ChartData data) {
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setStrokeWidth(4);

        setData(data);
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    void setData(ChartData data) {
        this.data = data;
        setBounds(0, data.xAxis.length - 1);
    }

    void setBounds(int o, int l) {
        offset = o;
        len = l;

        updateExtrems();
        updateLists();
    }

    public void update(long dt) {
        float step = (float) width / len;

        int heightOffset = height - height / 10;
        int availableHeight = height - height / 5;
        float koeff = (float) availableHeight / (maxY - minY);

        for (int i = 0; i < len; i++) {
            int index = i * 4;
            for (int j = 0; j < data.chartsCount; j++) {
                float[] pts = ptsList.get(j);
                int[] line = data.lines.get(j).yAxis;

                pts[index] = (int) (i * step);
                pts[index + 2] = (int) ((i + 1) * step);

                //some magic
                pts[index + 1] = heightOffset - ((line[offset + i] - minY) * koeff);
                pts[index + 3] = heightOffset - ((line[offset + i + 1] - minY) * koeff);
            }
        }
    }

    void updateLists() {
        ptsList.clear();
        for (int i = 0; i < data.chartsCount; i++) {
            ptsList.add(new float[len * 4]);
        }
    }

    void updateExtrems() {
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;
        for (int i = 0; i < data.chartsCount; i++) {
            ChartLine line = data.lines.get(i);
            for (int j = 0; j < len; j++) {
                minY = Integer.min(minY, line.yAxis[offset + j]);
                maxY = Integer.max(maxY, line.yAxis[offset + j]);
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(Color.GRAY);

        for (int i = 0; i < data.chartsCount; i++) {
            float[] pts = ptsList.get(i);
            ChartLine line = data.lines.get(i);
            paint.setColor(line.color);

            canvas.drawLines(pts, paint);
        }
    }
}
