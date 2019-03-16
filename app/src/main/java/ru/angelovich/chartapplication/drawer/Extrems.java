package ru.angelovich.chartapplication.drawer;

import ru.angelovich.chartapplication.data.ChartLine;

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
