package ru.angelovich.chartapplication;

import android.graphics.Canvas;

interface IChartDrawer {
    void update(long dt);
    void draw(Canvas canvas);
    void setSize(int w, int h);
}
