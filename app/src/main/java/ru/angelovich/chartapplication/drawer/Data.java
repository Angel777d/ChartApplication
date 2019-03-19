package ru.angelovich.chartapplication.drawer;

import java.util.ArrayList;

import ru.angelovich.chartapplication.data.ChartData;
import ru.angelovich.chartapplication.data.ChartLine;

class Data {
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
