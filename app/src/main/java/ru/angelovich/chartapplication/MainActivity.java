package ru.angelovich.chartapplication;

import android.os.Bundle;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

import ru.angelovich.mediacontroller.chartapplication.R;

//import android.util.Log;


abstract class Thumb {

    float leftEdge = 0;
    float rightEdge = 1;
    State state = State.None;

    public Thumb() {
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
        float centerDiff = Math.abs((leftPos + rightPos) / 2 - x);
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

abstract class TouchController implements View.OnTouchListener {
    Thumb thumb;
    View view;

    public TouchController(View view) {
        this.view = view;
        view.setOnTouchListener(this);
        thumb = new Thumb() {
            @Override
            void onRangeChanged(float leftEdge, float rightEdge) {
                onBoundsChanged(leftEdge, rightEdge);
            }
        };
    }

    abstract void onBoundsChanged(float leftEdge, float rightEdge);

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (view == this.view) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    thumb.update(motionEvent.getX(), view.getWidth());
//                    Log.d("ACTION_MOVE", motionEvent.toString());
                    break;
                case MotionEvent.ACTION_DOWN:
                    thumb.touch(motionEvent.getX(), view.getWidth());
//                    Log.d("ACTION_DOWN", motionEvent.toString());
                    break;
                case MotionEvent.ACTION_UP:
                    thumb.release();
//                    Log.d("ACTION_UP", motionEvent.toString());
                    break;
                default:
                    return false;
            }
            return true;
        }

        return false;
    }
}

public class MainActivity extends AppCompatActivity {
    ChartView chartView = null;
    ChartView chartView2 = null;

    TouchController tc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String jsonStr = AssetsReader.readData(this, "chart_data.json");
        List<ChartData> list = AssetsReader.process(jsonStr);
        final ChartData data = list.get(0);

        final ViewChartDrawer drawer = new ViewChartDrawer();
        drawer.setData(data);

        chartView = new ChartView(getApplicationContext(), drawer);
        final FrameLayout canvasLayout = findViewById(R.id.mainChartView);
        canvasLayout.addView(chartView);


        final ControllerChartDrawer drawer2 = new ControllerChartDrawer();
        drawer2.setData(data);

        chartView2 = new ChartView(getApplicationContext(), drawer2);
        final FrameLayout canvasLayout2 = findViewById(R.id.chartControllerView);
        canvasLayout2.addView(chartView2);

        tc = new TouchController(canvasLayout2) {
            @Override
            void onBoundsChanged(float leftEdge, float rightEdge) {
                Log.d("Thumb", String.format("left: %s, right: %s", leftEdge, rightEdge));
                drawer.setBounds(leftEdge, rightEdge);
                drawer2.setBounds(leftEdge, rightEdge);
            }
        };
    }


}

