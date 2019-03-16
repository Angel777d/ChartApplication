package ru.angelovich.chartapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;

import ru.angelovich.chartapplication.data.ChartData;
import ru.angelovich.chartapplication.data.ChartLine;


class Data {
    //    ChartData data;
    ArrayList<ChartLine> visibleLines = new ArrayList<>();

    int fullSize;
    int linesCount;

    void setData(ChartData data) {
        fullSize = data.xAxis.length;
        visibleLines.clear();
        for (int i = 0; i < data.lines.size(); i++) {
            ChartLine line = data.lines.get(i);
            if (line.visible)
                visibleLines.add(line);
        }
        linesCount = visibleLines.size();
    }
}

class Bounds {
    float leftEdge = 0;
    float rightEdge = 1;
}

class Stage {
    int width = 0;
    int height = 0;
}

class Range {
    int len, offset;

    void process(Data data, Bounds bounds) {
        offset = (int) Math.floor(data.fullSize * bounds.leftEdge);
        len = (int) Math.ceil(data.fullSize * bounds.rightEdge) - offset - 1;
    }
}


class Extrems {
    int min, max, delta;

    void process(Data data, Range range) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int i = 0; i < data.linesCount; i++) {
            ChartLine line = data.visibleLines.get(i);
            for (int j = 0; j < range.len; j++) {
                minY = Integer.min(minY, line.yAxis[range.offset + j]);
                maxY = Integer.max(maxY, line.yAxis[range.offset + j]);
            }
        }

        int newMax = max;
        if (maxY * 1.05f > max) {
            newMax = (int) Math.ceil(maxY * 1.1f / 10) * 10;
        } else if (1f * max / (max - maxY) > 0.3) {
            newMax = (int) Math.ceil(maxY * 1.1f / 10) * 10;
        }

        min = 0;
        max = newMax;

        delta = max - min;
    }
}


class GridModel {
    private static final int ROWS_COUNT = 6;
    int[] rows = new int[ROWS_COUNT];
    int[] values = new int[ROWS_COUNT];

    void process(Stage stage, Extrems ext) {
        float koeffY = (float) stage.height / ext.delta;

        float step = (float) ext.delta / ROWS_COUNT;
        for (int i = 0; i < 6; i++) {
            rows[i] = stage.height - Math.round(step * i * koeffY);
            values[i] = Math.round(step * i);
        }
    }
}

class LineChartModel {

    ArrayList<float[]> ptsList = new ArrayList<>();
    ArrayList<Paint> paints = new ArrayList<>();
    private int strokeWidth = 6;
    private boolean setAntiAlias = true;

    void updatePaint(Data data) {
        paints.clear();
        for (int i = 0; i < data.linesCount; i++) {
            ChartLine line = data.visibleLines.get(i);
            Paint paint = new Paint();
            paint.setColor(line.color);
            paint.setAntiAlias(setAntiAlias);
            paint.setStrokeWidth(strokeWidth);
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
        float koeffY = (float) stage.height / ext.delta;
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

                float pos = (line.yAxis[range.offset + i] - ext.min) * koeffY;
                pts[index + 1] = stage.height - pos;

                pos = (line.yAxis[range.offset + i + 1] - ext.min) * koeffY;
                pts[index + 3] = stage.height - pos;
            }
        }
    }
}

abstract class BasicDrawer implements IDrawer, IChartBounds {

    abstract protected void startDraw(Canvas canvas);

    protected boolean invData, invStage, invBounds, invDraw = false;
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

class ControllerDrawer extends BasicDrawer {
    private static final int STROKE_SIZE = 16;

    private Paint rectPaint;
    private Paint coverPaint;

    private float leftEdge, rightEdge;

    ControllerDrawer() {
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

class ViewDrawer extends BasicDrawer {

    private Paint gridPaint;
    private Paint fontPaint;
    private GridModel gridModel;

    public ViewDrawer() {
        gridModel = new GridModel();

        gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setAlpha(80);
        gridPaint.setStrokeWidth(2);

        fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaint.setTextSize(32);
        fontPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void startDraw(Canvas canvas) {
        drawBG(canvas);
        drawRows(canvas);
        drawCharts(canvas);
    }

    @Override
    protected void validate() {
        super.validate();

        if (invData || invBounds) {
            gridModel.process(stage, ext);
        }
    }

    private void drawRows(Canvas canvas) {
        for (int i = 0; i < gridModel.rows.length; i++) {
            canvas.drawText(Integer.toString(gridModel.values[i]), 0, gridModel.rows[i] - 10, fontPaint);
            canvas.drawLine(0, gridModel.rows[i], stage.width, gridModel.rows[i], gridPaint);
        }
    }
}