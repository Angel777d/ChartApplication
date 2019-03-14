package ru.angelovich.chartapplication;

import android.support.v4.math.MathUtils;
import android.view.MotionEvent;
import android.view.View;

abstract class TouchController implements View.OnTouchListener {
    private Thumb thumb;

    TouchController() {
        thumb = new Thumb() {
            @Override
            void onRangeChanged(float leftEdge, float rightEdge) {
                onBoundsChanged(leftEdge, rightEdge);
            }
        };
    }

    void setView(View view) {
        view.setOnTouchListener(this);
    }

    abstract void onBoundsChanged(float leftEdge, float rightEdge);

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                thumb.update(motionEvent.getX(), view.getWidth());
                break;
            case MotionEvent.ACTION_DOWN:
                thumb.touch(motionEvent.getX(), view.getWidth());
                break;
            case MotionEvent.ACTION_UP:
                thumb.release();
                break;
            default:
                return false;
        }
        return true;
    }

    abstract static class Thumb {

        float leftEdge = 0;
        float rightEdge = 1;
        State state = State.None;

        Thumb() {
        }

        abstract void onRangeChanged(float left, float right);

        void update(float x, int width) {
            float left;
            float right;
            switch (state) {
                case None:
                    break;
                case FullAction:
                    float size = rightEdge - leftEdge;
                    float center = x / width;
                    left = center - size / 2;
                    right = center + size / 2;
                    if (left < 0) {
                        right -= left;
                        left = 0;
                    }
                    if (right > 1) {
                        left -= right - 1;
                        right = 1;
                    }
                    leftEdge = left;
                    rightEdge = right;
                    onRangeChanged(leftEdge, rightEdge);
                    break;
                case LeftAction:
                    left = x / width;
                    leftEdge = MathUtils.clamp(left, 0, rightEdge - 0.1f);
                    onRangeChanged(leftEdge, rightEdge);
                    break;
                case RightAction:
                    right = x / width;
                    rightEdge = MathUtils.clamp(right, leftEdge + 0.1f, 1);
                    onRangeChanged(leftEdge, rightEdge);
                    break;
                default:
                    break;
            }
        }

        void touch(float x, int width) {

            if (state != State.None) {
                return;
            }

            float leftPos = width * leftEdge;
            float rightPos = width * rightEdge;

            if (x < leftPos) {
                state = State.LeftAction;
            } else if (x > rightPos) {
                state = State.RightAction;
            } else {
                float centerDiff = Math.abs((leftPos + rightPos) / 2 - x);
                //increase center weight
                centerDiff *= 0.3f;
                float leftDiff = Math.abs(leftPos - x);
                float rightDiff = Math.abs(rightPos - x);

                if (centerDiff < leftDiff && centerDiff < rightDiff) {
                    state = State.FullAction;
                } else if (leftDiff < rightDiff) {
                    state = State.LeftAction;
                } else {
                    state = State.RightAction;
                }
            }
        }

        void release() {
            state = State.None;
        }

        enum State {
            LeftAction,
            FullAction,
            RightAction,
            None,
        }
    }
}
