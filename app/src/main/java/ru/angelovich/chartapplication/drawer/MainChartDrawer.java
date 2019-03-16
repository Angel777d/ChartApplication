package ru.angelovich.chartapplication.drawer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class MainChartDrawer extends BasicDrawer {

    private Paint gridPaint;
    private Paint fontPaint;
    private GridModel gridModel;

    public MainChartDrawer() {
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

        if (invDraw) {
            gridModel.process(data, stage, bounds, ext);
        }
    }

    private void drawRows(Canvas canvas) {
        for (int i = 0; i < gridModel.rows.length; i++) {
            canvas.drawText(Integer.toString(gridModel.values[i]), 0, gridModel.rows[i] - 10, fontPaint);
            canvas.drawLine(0, gridModel.rows[i], stage.width, gridModel.rows[i], gridPaint);
        }
    }

    static class GridModel {
        private static final int ROWS_COUNT = 6;
        int[] rows = new int[ROWS_COUNT];
        int[] values = new int[ROWS_COUNT];

        void process(Data data, Stage stage, Bounds bounds, Extrems ext) {
            float koeffY = (float) stage.height / ext.delta;

            float step = (float) ext.delta / ROWS_COUNT;
            for (int i = 0; i < 6; i++) {
                rows[i] = stage.height - Math.round(step * i * koeffY);
                values[i] = Math.round(step * i);
            }
        }

    }
}
