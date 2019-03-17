package ru.angelovich.chartapplication.drawer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

import ru.angelovich.chartapplication.IDrawer;
import ru.angelovich.chartapplication.data.ChartData;
import ru.angelovich.chartapplication.data.ChartLine;


class LineChartModel {
    private static final int STROKE_WIDTH = 6;
    private static final boolean ANTI_ALIAS = true;

    ArrayList<float[]> ptsList = new ArrayList<>();
    ArrayList<Paint> paints = new ArrayList<>();

    void updatePaint(Data data) {
        paints.clear();
        for (int i = 0; i < data.linesCount; i++) {
            ChartLine line = data.visibleLines.get(i);
            Paint paint = new Paint();
            paint.setColor(line.color);
            paint.setAntiAlias(ANTI_ALIAS);
            paint.setStrokeWidth(STROKE_WIDTH);
            paints.add(paint);
        }
    }

    void updateRange(Data data, Range range) {
        ptsList.clear();
        for (int i = 0; i < data.linesCount; i++) {
            ptsList.add(new float[range.len * 4]);
        }
    }

    void process(Data data, Stage stage, Range range, Extrems ext) {
        float koeffY = (float) stage.height / (ext.getMax() - ext.getMin());
        float stepX = (float) stage.width / range.len;

        for (int i = 0; i < range.len; i++) {
            int index = i * 4;
            for (int j = 0; j < data.linesCount; j++) {
                ChartLine line = data.visibleLines.get(j);
                if (!line.visible)
                    continue;

                float[] pts = ptsList.get(j);

                pts[index] = i * stepX;
                pts[index + 2] = (i + 1) * stepX;

                float pos = (line.yAxis[range.offset + i] - ext.getMin()) * koeffY;
                pts[index + 1] = stage.height - pos;

                pos = (line.yAxis[range.offset + i + 1] - ext.getMin()) * koeffY;
                pts[index + 3] = stage.height - pos;
            }
        }
    }
}

abstract class BasicDrawer implements IDrawer {

    abstract protected void startDraw(Canvas canvas);

    boolean invDraw = false;
    boolean invData, invStage, invBounds = false;

    Data data = new Data();
    Stage stage = new Stage();
    Bounds bounds = new Bounds();
    Range range = new Range();
    Extrems ext = new Extrems();
    LineChartModel model = new LineChartModel();
    private int bgColor = Color.MAGENTA;

    BasicDrawer() {
    }

    @Override
    public boolean isInvalid() {
        return invDraw;
    }

    public void setSize(int width, int height) {
        stage.width = width;
        stage.height = height;
        invStage = true;
    }


    public void setBounds(float leftEdge, float rightEdge) {
        bounds.leftEdge = leftEdge;
        bounds.rightEdge = rightEdge;
        invBounds = true;
    }

    public void invalidateDraw() {
        invDraw = true;
    }

    public void setBgColor(int color) {
        invDraw = bgColor != color;
        bgColor = color;
    }

    public void setData(ChartData chartData) {
        data.setData(chartData);
        invData = true;
    }

    public final void update(long dt) {
        invBounds = invBounds || ext.animate(dt);
        validate();
        invData = invStage = invBounds = false;
    }

    protected void validate() {
        if (invData || invBounds)
            range.process(data, bounds);

        if (invData || invBounds)
            ext.process(data, range);

        if (invData)
            model.updatePaint(data);

        if (invData || invBounds)
            model.updateRange(data, range);

        if (invData || invStage || invBounds)
            model.process(data, stage, range, ext);

        invDraw = invDraw || invData || invStage || invBounds;
    }

    public final void draw(Canvas canvas) {
        invDraw = false;
        startDraw(canvas);
    }

    void drawBG(Canvas canvas) {
        canvas.drawColor(bgColor);
    }

    void drawCharts(Canvas canvas) {
        for (int i = 0; i < data.linesCount; i++) {
            float[] pts = model.ptsList.get(i);
            Paint paint = model.paints.get(i);
            canvas.drawLines(pts, paint);
        }
    }
}

