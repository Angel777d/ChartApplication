package ru.angelovich.chartapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

import ru.angelovich.mediacontroller.chartapplication.R;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    ChartView chartView = null;
    ChartView chartView2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String jsonStr = AssetsReader.readData(this, "chart_data.json");
        List<ChartData> list = AssetsReader.process(jsonStr);
        ChartData data = list.get(0);

        ChartDrawer drawer = new ChartDrawer(data);
        chartView = new ChartView(getApplicationContext(), drawer);
        FrameLayout canvasLayout = findViewById(R.id.mainChartView);
        canvasLayout.addView(chartView);

        ChartDrawer drawer2 = new ChartDrawer(data);
        chartView2 = new ChartView(getApplicationContext(), drawer2);
        FrameLayout canvasLayout2 = findViewById(R.id.chartControllerView);
        canvasLayout2.addView(chartView2);

        chartView2.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (view instanceof ChartView) {

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Log.d("ACTION_MOVE", motionEvent.toString());
                    break;
                case MotionEvent.ACTION_DOWN:
                    Log.d("ACTION_DOWN", motionEvent.toString());
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("ACTION_UP", motionEvent.toString());
                    break;
                default:
                    return false;
            }
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            return true;
        }

        return false;
    }

}

