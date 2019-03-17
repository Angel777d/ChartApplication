package ru.angelovich.chartapplication.drawer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.angelovich.chartapplication.data.ChartData;
import ru.angelovich.chartapplication.data.ChartLine;

class Data {
    //    ChartData data;
    ArrayList<ChartLine> visibleLines = new ArrayList<>();
    String[] times;
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
        times = new String[data.xAxis.length];
        SimpleDateFormat df = new SimpleDateFormat("MMM dd");

        for (int i = 0; i < data.xAxis.length; i++) {
            long value = data.xAxis[i];
            Date date = new Date(value);
            String dateStr = df.format(date);
            times[i] = dateStr;
        }


    }
}
