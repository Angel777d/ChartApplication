package ru.angelovich.chartapplication;

import android.graphics.Canvas;

public interface IDrawer {
    void update(long dt);
    void draw(Canvas canvas);
    void setSize(int w, int h);

    boolean isInvalid();
}
