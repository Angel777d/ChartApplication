package ru.angelovich.chartapplication.drawer;

class Range {

    float realLeft, realRight;

    int left, right;
    int len, offset;

    void process(Data data, Bounds bounds) {
        realLeft = data.fullSize * bounds.leftEdge;
        realRight = data.fullSize * bounds.rightEdge;

        left = offset = (int) Math.floor(realLeft);
        right = (int) Math.ceil(data.fullSize * bounds.rightEdge);
        len = right - left - 1;
    }
}
