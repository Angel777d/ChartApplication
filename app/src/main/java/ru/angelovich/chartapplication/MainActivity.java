package ru.angelovich.chartapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    List<ChartData> dataList;
    TouchController tc;
    ControllerChartDrawer controllerChartDrawer;
    ViewChartDrawer chartDrawer;
    int theme;

    public MainActivity() {
        super();
    }

    public void onThemeClick(MenuItem item) {
        theme = theme == R.style.AppTheme ? R.style.AppTheme_Dark : R.style.AppTheme;
        initView(theme);
    }

    void initView(int theme) {
        setTheme(theme);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initMainChart();
        initControlChart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadData();

        controllerChartDrawer = new ControllerChartDrawer();
        chartDrawer = new ViewChartDrawer();

        tc = new TouchController() {
            @Override
            void onBoundsChanged(float leftEdge, float rightEdge) {
                chartDrawer.setBounds(leftEdge, rightEdge);
                controllerChartDrawer.setBounds(leftEdge, rightEdge);
            }
        };

        ChartData data = dataList.get(1);
        updateData(data);

        theme = R.style.AppTheme;
        initView(theme);
    }

    void loadData() {
        String jsonStr = AssetsReader.readData(this, "chart_data.json");
        dataList = AssetsReader.process(jsonStr);
    }

    void initMainChart() {
        ChartView view = new ChartView(getApplicationContext(), chartDrawer);
        final FrameLayout canvasLayout = findViewById(R.id.mainChartView);
        canvasLayout.addView(view);

    }

    void initControlChart() {
        ChartView view = new ChartView(getApplicationContext(), controllerChartDrawer);
        final FrameLayout canvasLayout2 = findViewById(R.id.chartControllerView);
        canvasLayout2.addView(view);

        tc.setView(canvasLayout2);
    }

    void updateData(ChartData data) {
        chartDrawer.setData(data);
        controllerChartDrawer.setData(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

