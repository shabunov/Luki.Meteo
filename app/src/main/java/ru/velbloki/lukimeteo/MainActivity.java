package ru.velbloki.lukimeteo;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    MainActivity activity;
    AQuery aq;
    public static final String API_URL = "http://lukimeteo.ru/report/json.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        activity = this;
        aq = new AQuery(activity);

        final TextView status_text = (TextView) findViewById(R.id.status_text);
        final ImageView wind_direction = (ImageView) findViewById(R.id.wind_direction);

        aq.ajax(API_URL, null, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {

                Map<String, Float> wdd  = new HashMap<String, Float>();
                // градус поворота картинки со стрелкой, указывающей направление ветра
                wdd.put("WNW", -75.5f);
                wdd.put("W", -90f);
                wdd.put("N", 0f);
                wdd.put("E", 90f);
                wdd.put("S", 180f);
                //... и т.д.

                String wd = "N", wd_text = "";

                if(json != null) {
                    try {
                        wd = json.get("wind_direction").toString();
                        wd_text = json.get("wind_direction_text").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    wind_direction.setRotation(wdd.get(wd));
                    status_text.setText("WD: "+wd+", WDD: "+wdd.get(wd)+", WDT: "+wd_text);
                }else{
                    //скорее всего проблемы с интернетом
                }
            }

        });
    }
}
