package ru.angelovich.chartapplication.data;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class AssetsReader {
    private static final String TYPE_X = "x";
    private static final String TYPE_LINE = "line";

    public static String readData(Context context, String fileName) {
        String result = "";
        try (InputStream is = context.getAssets().open(fileName)) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            result = writer.toString();
        } catch (UnsupportedEncodingException ex) {
            //
        } catch (IOException ex) {
            //
        }

        return result;
    }

    public static ArrayList<ChartData> process(String jsonValue) {
        ArrayList<ChartData> result = new ArrayList<>();
        try {
            JSONArray dataArray = new JSONArray(jsonValue);
            int len = dataArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject chart = dataArray.getJSONObject(i);
                result.add(processChart(chart));
            }
            return result;
        } catch (Exception ex) {
            Log.d("JSON P", ex.getMessage());
        }
        return result;
    }

    private static ChartData processChart(JSONObject chart) {
        ChartData result = new ChartData();
        try {
            JSONObject types = chart.getJSONObject("types");
            JSONObject names = chart.getJSONObject("names");
            JSONObject colors = chart.getJSONObject("colors");
            JSONArray columns = chart.getJSONArray("columns");

            result.lines = new ArrayList<>();

            for (int i = 0; i < columns.length(); i++) {
                JSONArray list = columns.getJSONArray(i);
                String listTypeId = list.getString(0);
                String listType = types.getString(listTypeId);
                if (TYPE_X.equals(listType)) {
                    long[] target = new long[list.length() - 1];
                    result.xAxis = target;
                    for (int j = 1; j < list.length(); j++) {
                        target[j - 1] = list.getLong(j);
                    }

                } else if (TYPE_LINE.equals(listType)) {
                    int[] target = new int[list.length() - 1];
                    ChartLine line = new ChartLine();
                    line.name = names.getString(listTypeId);
                    String colorStr = colors.getString(listTypeId);
                    line.color = Color.parseColor(colorStr);
                    line.yAxis = target;
                    result.lines.add(line);
                    for (int j = 1; j < list.length(); j++) {
                        target[j - 1] = list.getInt(j);
                    }
                }

            }
        } catch (JSONException ex) {
            //
        }
        result.size = result.xAxis.length;
        return result;
    }
}
