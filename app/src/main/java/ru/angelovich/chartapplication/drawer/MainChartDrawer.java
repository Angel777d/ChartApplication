package ru.angelovich.chartapplication.drawer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class MainChartDrawer extends BasicDrawer {

    private Paint gridPaint;
    private Paint fontPaint;
    private YGridModel gridModelY;
    private XGridModel gridModelX;

    public MainChartDrawer() {
        gridModelY = new YGridModel();
        gridModelX = new XGridModel();

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

        if (invData || invStage || invBounds) {
            gridModelY.process(data, stage, bounds, ext);
            gridModelX.process(data, range, stage);
        }
    }

    private void drawRows(Canvas canvas) {
        for (int i = 0; i < gridModelY.rows.length; i++) {
            canvas.drawText(Integer.toString(gridModelY.values[i]), 0, gridModelY.rows[i] - 10, fontPaint);
            canvas.drawLine(0, gridModelY.rows[i], stage.width, gridModelY.rows[i], gridPaint);
        }

        for (int i = 0; i < gridModelX.size; i++) {
            canvas.drawText(gridModelX.values[i], gridModelX.positions[i], stage.height - 10, fontPaint);
        }
    }

    static class YGridModel {
        private static final int ROWS_COUNT = 6;
        int[] rows = new int[ROWS_COUNT];
        int[] values = new int[ROWS_COUNT];

        void process(Data data, Stage stage, Bounds bounds, Extrems ext) {
            float delta = ext.getMax() - ext.getMin();
            float koeffY = (float) stage.height / delta;

            float step = delta / ROWS_COUNT;
            for (int i = 0; i < 6; i++) {
                rows[i] = stage.height - Math.round(step * i * koeffY);
                values[i] = Math.round(step * i);
            }
        }
    }

    static class XGridModel {
        private static final int MAX = 6;
        int size;
        int[] indexes = new int[MAX];
        float[] positions = new float[MAX];
        String[] values = new String[MAX];

        void process(Data data, Range range, Stage stage) {
            size = range.len > MAX ? MAX : range.len;

            float step = (float) range.len / size;

            for (int i = 0; i < size; i++) {
                int index = range.offset + Math.round(step * i);
                values[i] = data.times[index];
                positions[i] = (float) stage.width / size * i;
                indexes[i] = index;
            }
        }
    }


}
