package ru.angelovich.chartapplication.drawer;

import ru.angelovich.chartapplication.data.ChartLine;


class AnimatedValue {
    private static final int DURATION = 100;

    float current, target;
    private float step, remain;

    void set(float value) {
        if (value != target) {
            target = value;
            remain = remain > 0 ? remain : DURATION;
            step = (target - current) / remain;
        }
    }

    float get() {
        return current;
    }

    boolean animate(float dt) {
        if (remain > 0) {
            remain -= dt;
            current += step * dt;

            if (remain <= 0) {
                current = target;
            }
            return true;
        }
        return false;
    }
}


class Extrems {
    //    AnimatedValue min = new AnimatedValue();
    AnimatedValue max = new AnimatedValue();

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

        float newMax = max.get();
        if (maxY * 1.05f > max.target) {
            newMax = (int) Math.ceil(maxY * 1.1f / 10) * 10;
        } else if (1f * max.target / (max.target - maxY) > 0.3) {
            newMax = (int) Math.ceil(maxY * 1.1f / 10) * 10;
        }

        max.set(newMax);
//        min.set(0);
    }

    int getMin() {
//        return Math.round(min.current);
        return 0;
    }

    int getMax() {
        return Math.round(max.current);
    }

    boolean animate(float dt) {
//        return min.animate(dt) || max.animate(dt);
        return max.animate(dt);
    }


}
