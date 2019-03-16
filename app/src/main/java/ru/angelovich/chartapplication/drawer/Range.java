package ru.angelovich.chartapplication.drawer;

class Range {
    int len, offset;

    void process(Data data, Bounds bounds) {
        offset = (int) Math.floor(data.fullSize * bounds.leftEdge);
        len = (int) Math.ceil(data.fullSize * bounds.rightEdge) - offset - 1;
    }
}
