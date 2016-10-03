package ru.velbloki.lukimeteo;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MainActivity activity;
    AQuery aq;

    public static float dY, centerH;

    public static final String API_URL = "http://lukimeteo.ru/report/json.php";
    Timer myTimer = new Timer();

    ImageView   wind_direction,
                forecast_icon,
                sun_moon_rise,
                logo_t,
                sun_moon_up,
                sun_moon_down;

    TextView    temp_fact,
                forecast_text,
                rise_length,
                wind_direction_text,
                when_up,
                when_down,
                humidity,
                pressure,
                wind;

    DateParser  dp;

    // ассоц. массив с углами поворота для направления ветра
    Map<String, Float> wdd  = new HashMap<String, Float>();

    // ассоц. массив с текстом направления ветра
    Map<String, Integer> wds  = new HashMap<String, Integer>();

    // погода
    Map<String, Integer> forecast  = new HashMap<String, Integer>();

    protected void initMyData(){
        // ассоц. углы
        wdd.put("N", 0f);       wdd.put("NNE", 22.5f);    wdd.put("NE", 45f);      wdd.put("ENE", 67.5f);
        wdd.put("E", 90f);      wdd.put("ESE", 112.5f);   wdd.put("SE", 135f);     wdd.put("SSE", 257.5f);
        wdd.put("S", 180f);     wdd.put("SSW", 202.5f);   wdd.put("SW", 225f);     wdd.put("WSW", 147.5f);
        wdd.put("W", 270f);     wdd.put("WNW", 292.5f);   wdd.put("NW", 315f);     wdd.put("NNW", 337.5f);

        // направления ветра
        wds.put("N", R.string.wind_N);  wds.put("NNE", R.string.wind_NNE);  wds.put("NE", R.string.wind_NE);  wds.put("ENE", R.string.wind_ENE);
        wds.put("E", R.string.wind_E);  wds.put("ESE", R.string.wind_ESE);  wds.put("SE", R.string.wind_SE);  wds.put("SSE", R.string.wind_SSE);
        wds.put("S", R.string.wind_S);  wds.put("SSW", R.string.wind_SSW);  wds.put("SW", R.string.wind_SW);  wds.put("WSW", R.string.wind_WSW);
        wds.put("W", R.string.wind_W);  wds.put("WNW", R.string.wind_WNW);  wds.put("NW", R.string.wind_NW);  wds.put("NNW", R.string.wind_NNW);

        // состояния погоды
        forecast.put("sunny", R.string.sunny);
        forecast.put("partlycloudy", R.string.partlycloudy);
        forecast.put("rainy", R.string.rainy);
        forecast.put("cloudy", R.string.cloudy);
        forecast.put("snowy", R.string.snowy);


        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();

        dY = displaymetrics.widthPixels / 240f;
        centerH = dY * 296.37f / 2; // он же радиус дуги солнца/луны

        // [layer 2] GridLayout
        sun_moon_up = (ImageView) findViewById(R.id.sun_moon_up);
        sun_moon_down = (ImageView) findViewById(R.id.sun_moon_down);
        when_up = (TextView) findViewById(R.id.when_up);
        when_down = (TextView) findViewById(R.id.when_down);

        humidity = (TextView) findViewById(R.id.humidity);
        pressure = (TextView) findViewById(R.id.pressure);
        wind = (TextView) findViewById(R.id.wind);
        // [/layer 2]

        // [layer 1] GridLayout
        wind_direction = (ImageView) findViewById(R.id.wind_direction);
        forecast_icon = (ImageView) findViewById(R.id.forecast_icon);
        temp_fact = (TextView) findViewById(R.id.temp_fact);
        forecast_text = (TextView) findViewById(R.id.forecast_text);
        wind_direction_text = (TextView) findViewById(R.id.wind_direction_text);
        rise_length = (TextView) findViewById(R.id.rise_length);
        logo_t = (ImageView) findViewById(R.id.logo_t);
        sun_moon_rise = (ImageView) findViewById(R.id.sun_moon_rise);
        // [/layer 1]


        wind_direction.setY(113.5f*dY);
        forecast_icon.setY(-30*dY);
        temp_fact.setY(5*dY);
        forecast_text.setY(30*dY);
        wind_direction_text.setY(67.8f*dY);
        rise_length.setY(-121f*dY);
        logo_t.setY(-80f*dY);
        sun_moon_rise.setY(-centerH);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        activity = this;
        aq = new AQuery(activity);

        initMyData();

        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("mylog", "timer run: loadData");
                loadData();
            }
        }, 0, 10000);
    }

    protected void onStop(){
        super.onStop();
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }
    }

    private JSONObject loadDataLocal() {
        JSONObject json = null;
        try {
            json = new JSONObject("{\"wind_direction\":\"NNE\",\"temp_fact\":\"12.7\"}");
        }catch (JSONException e){
            Log.d("mylog", "loadDataLocal: exeption :"+ e);
        }
        return json;
    }

    private void loadData(){
        Log.d("mylog", "loadData: called");
        aq.ajax(API_URL, null, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                if(json != null) {
                    showWeather(json);
                }else{
                    Log.d("mylog", "callback: empty json");
                    showWeather(loadDataLocal());
                }
            }
        });
    }

    protected void showWeather(JSONObject json){
        double  temp = 0,
                pr = 0,
                hm = 0,
                w = 0;

        try {

            dp = new DateParser();
            dp.setRiseLength(
                    json.getString("sunrise_text"),
                    json.getString("sunset_text")
            );
            dp.setCurrent(json.getLong("stamp"));

            wind_direction_text.setText(wds.get(json.getString("wind_direction")));
            wind_direction.setRotation(wdd.get(json.getString("wind_direction")));

            pr = json.getDouble("pressure");
            hm = json.getDouble("humidity");
            w = json.getDouble("wind");
            temp = json.getDouble("temp_fact");

            temp_fact.setText(((temp > 0) ? "+" : "")+temp+"°C");
            pressure.setText(String.valueOf(pr));
            humidity.setText((String.valueOf(hm) + " %"));
            wind.setText(String.valueOf(w) +" "+ getString(R.string.wind_metric));

            forecast_text.setText(forecast.get(json.getString("forecast")));

            if(dp.isNight()){
                sun_moon_up.setImageResource(R.drawable.ic_moon_up);
                sun_moon_down.setImageResource(R.drawable.ic_moon_down);
                sun_moon_rise.setImageResource(R.drawable.ic_full_moon);
            }else {
                sun_moon_down.setImageResource(R.drawable.ic_sun_down);
                sun_moon_up.setImageResource(R.drawable.ic_sun_up);
                sun_moon_rise.setImageResource(R.drawable.ic_full_sun);
            }

            when_up.setText(dp.myDateFormat(dp.getFrom()));
            when_down.setText(dp.myDateFormat(dp.getTo()));
            rise_length.setText(dp.getRiseLengthText());

            switch (json.getString("forecast")){
                case "sunny":
                    if(dp.isNight())
                        forecast_icon.setImageResource(R.drawable.ic_sunny_moon);
                    else
                        forecast_icon.setImageResource(R.drawable.ic_sunny_sun);
                    break;
                case "partlycloudy":
                    if(dp.isNight())
                        forecast_icon.setImageResource(R.drawable.ic_partlycloudy_moon);
                    else
                        forecast_icon.setImageResource(R.drawable.ic_partlycloudy_sun);
                    break;
                case "rainy":
                    if(dp.isNight())
                        forecast_icon.setImageResource(R.drawable.ic_rainy_moon);
                    else
                        forecast_icon.setImageResource(R.drawable.ic_rainy_sun);
                    break;
                case "cloudy":
                    forecast_icon.setImageResource(R.drawable.ic_cloudy);
                    break;
                case "snowy":
                    if(dp.isNight())
                        forecast_icon.setImageResource(R.drawable.ic_snowy_moon);
                    else
                        forecast_icon.setImageResource(R.drawable.ic_snowy_sun);
                    break;
            }

            float dx_right = 0, dx_left = 0, dy = 0;

            float rise_ax = 195f - 195f * 2 * dp.getRisePercentStage();
            float rise_ay = 296.37f;

            if(rise_ax > 0){
                dx_left = rise_ax * dY;
                dx_right = 0;
            }else{
                dx_right = -rise_ax * dY;
                dx_left = 0;
            }

            dy = (float) (rise_ay - Math.sqrt(rise_ay * rise_ay - rise_ax * rise_ax))*dY;

            sun_moon_rise.setPadding((int) dx_right,(int) dy, (int) dx_left, 0);

        }catch (JSONException e){
            Log.e("mylog", "showWeather: json error!", e);
        }
    }
}
