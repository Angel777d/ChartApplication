package ru.angelovich.chartapplication.drawer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.angelovich.chartapplication.data.ChartData;

public class MainChartDrawer extends BasicDrawer {

    private Paint gridPaint;
    private YGridModel gridModelY;
    private XGridModel gridModelX;

    private static final int X_OFFSET = 50;


    public MainChartDrawer() {
        gridModelY = new YGridModel();
        gridModelX = new XGridModel();

        gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setColor(Color.GRAY);
        gridPaint.setAlpha(127);
        gridPaint.setStrokeWidth(2);
        gridPaint.setTextSize(32);
    }

    @Override
    public void startDraw(Canvas canvas) {
        drawBG(canvas);
        drawRows(canvas);
        drawCharts(canvas);
    }

    @Override
    public void setData(ChartData chartData) {
        super.setData(chartData);
        gridModelX.updateTimes(chartData);
    }

    @Override
    protected void validate() {
        super.validate();
        if (invData || invStage || invBounds) {
            gridModelY.process(data, stage, bounds, ext);
            gridModelX.process(data, stage, bounds, range);
        }
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height - X_OFFSET);
    }


    private void drawRows(Canvas canvas) {
        for (int i = 0; i < gridModelY.rows.length; i++) {
            canvas.drawText(gridModelY.values[i], 0, gridModelY.rows[i] - 10, gridPaint);
            canvas.drawLine(0, gridModelY.rows[i], stage.width, gridModelY.rows[i], gridPaint);
        }

        for (int i = 0; i < gridModelX.size; i++) {
            XGridModel.XGridItem item = gridModelX.times[gridModelX.indexes[i]];
            canvas.drawText(item.text, item.position, stage.height + X_OFFSET - 10, gridPaint);
        }
    }

    static class YGridModel {
        private static final int ROWS_COUNT = 6;
        int[] rows = new int[ROWS_COUNT];
        String[] values = new String[ROWS_COUNT];

        static String coolNumberFormat(long count) {
            if (count < 50) return "" + count;
            if (count < 1000) return "" + count / 10 * 10;
            int exp = (int) (Math.log(count) / Math.log(1000));
            DecimalFormat format = new DecimalFormat("0.#");
            String value = format.format(count / Math.pow(1000, exp));
            return String.format("%s%c", value, "kMBTPE".charAt(exp - 1));
        }

        void process(Data data, Stage stage, Bounds bounds, Extrems ext) {
//            float delta = ext.max.target - ext.min.target;
            float delta = ext.max.target - 0;
            float koeffY = (float) stage.height / delta;

            float step = delta / ROWS_COUNT;
            for (int i = 0; i < 6; i++) {
                rows[i] = stage.height - Math.round(step * i * koeffY);
                values[i] = coolNumberFormat(Math.round(step * i));
            }
        }
    }

    static class XGridModel {
        private static final int MAX = 7;
        int size;
        int[] indexes = new int[MAX];

        private XGridItem[] times;

        void process(Data data, Stage stage, Bounds bounds, Range range) {

            size = range.len;
            int pow = 1;
            while (size > MAX - 1) {
                pow *= 2;
                size = range.len / pow;
            }

            size = Math.max(size, MAX);
            size = Math.min(size, data.fullSize);

            int first = range.left - range.left % pow;

            float virtualWidth = stage.width / bounds.delta();
            float stepSize = virtualWidth / data.fullSize;
            float startPos = first * stepSize - bounds.leftEdge * virtualWidth;

            for (int i = 0; i < size; i++) {
                int index = Math.min(first + i * pow, data.fullSize - 1);
                indexes[i] = index;
                times[index].position = startPos + stepSize * i * pow;
            }
        }

        void updateTimes(ChartData data) {
            times = new XGridItem[data.xAxis.length];
            SimpleDateFormat df = new SimpleDateFormat("MMM dd");
            for (int i = 0; i < data.xAxis.length; i++) {
                XGridItem item = new XGridItem();
                item.text = df.format(new Date(data.xAxis[i]));
                item.position = 0;
                item.alpha = 1;
                times[i] = item;

            }
        }

        static class XGridItem {
            float position;
            float alpha;
            String text;
        }
    }


}
