package ru.angelovich.chartapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.List;

import ru.angelovich.chartapplication.data.AssetsReader;
import ru.angelovich.chartapplication.data.ChartData;
import ru.angelovich.chartapplication.data.ChartLine;


public class MainActivity extends AppCompatActivity {

    List<ChartData> dataList;
    TouchController tc;
    ControllerDrawer controllerChartDrawer;
    ViewDrawer chartDrawer;
    boolean isDark;

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadData();

        controllerChartDrawer = new ControllerDrawer();
        chartDrawer = new ViewDrawer();

        tc = new TouchController() {
            @Override
            void onBoundsChanged(float leftEdge, float rightEdge) {
                chartDrawer.setBounds(leftEdge, rightEdge);
                controllerChartDrawer.setBounds(leftEdge, rightEdge);
            }
        };

        tc.setBounds(.3f, .7f);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        isDark = sp.getBoolean("isDark", false);

        initView();
        updateData();

        ChartData data = dataList.get(1);
        chartDrawer.setData(data);
        controllerChartDrawer.setData(data);
    }

    public void onThemeClick(MenuItem item) {
        isDark = !isDark;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("isDark", isDark);
        edit.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
//
//        initView();
//        updatePaint();
    }

    void loadData() {
        String jsonStr = AssetsReader.readData(this, "chart_data.json");
        dataList = AssetsReader.process(jsonStr);
    }

    void initView() {
        int theme = isDark ? R.style.AppTheme_Dark : R.style.AppTheme;
        int bgColor = ContextCompat.getColor(getApplicationContext(), isDark ? R.color.backgroundDark : R.color.backgroundLight);
        chartDrawer.setBgColor(bgColor);
        controllerChartDrawer.setBgColor(bgColor);

        setTheme(theme);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initMainChart();
        initControlChart();
    }

    void initMainChart() {
        final FrameLayout canvasLayout = findViewById(R.id.mainChartView);
        ChartView view = new ChartView(canvasLayout.getContext(), chartDrawer);
        canvasLayout.addView(view);

    }

    void initControlChart() {
        final FrameLayout canvasLayout2 = findViewById(R.id.chartControllerView);
        ChartView view = new ChartView(canvasLayout2.getContext(), controllerChartDrawer);
        canvasLayout2.addView(view);

        tc.setView(canvasLayout2);
    }

    void updateData() {
        ChartData data = dataList.get(1);
        LinearLayout container = findViewById(R.id.linesControls);
        container.removeAllViews();
        for (int i = 0; i < data.lines.size(); i++) {
            final ChartLine line = data.lines.get(i);
            AppCompatCheckBox box = createCheckBox(i, line, container.getContext());
            container.addView(box);
        }
    }

    AppCompatCheckBox createCheckBox(final int index, ChartLine line, Context context) {
        AppCompatCheckBox box = new AppCompatCheckBox(context);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLineVisible(isChecked, index);
            }
        });
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[]{
                line.color,
                Color.GRAY,
                Color.GRAY,
                line.color
        };

        ColorStateList myList = new ColorStateList(states, colors);
        box.setButtonTintList(myList);
        box.setTextColor(line.color);
        box.setText(line.name);
        box.setChecked(line.visible);
        return box;
    }

    void setLineVisible(boolean visible, int index) {
        ChartData data = dataList.get(1);
        ChartLine line = data.lines.get(index);
        line.visible = visible;
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

