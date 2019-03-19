package ru.angelovich.chartapplication.drawer;

class Bounds {
    float leftEdge = 0;
    float rightEdge = 1;

    float delta() {
        return rightEdge - leftEdge;
    }
}
