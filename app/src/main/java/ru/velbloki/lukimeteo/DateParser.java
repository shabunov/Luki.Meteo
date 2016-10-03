package ru.velbloki.lukimeteo;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Integer.parseInt;

class DateParser {

    private static final long one_day = 24*60*60*1000;
    private static boolean night = false;

    private static Date from, to, current;
    private static float risePercentStage = 0;

    private static Long riseLength;

    private static Date getTimeFromString(String str){

        String[] split = str.split(" ");
        String[] split1 = split[0].split(":");

        int H, i, s;
        H = parseInt(split1[0]);
        i = parseInt(split1[1]);
        s = parseInt(split1[2]);

        return setTime(H, i, s);
    }

    private static Date setTime(int H, int i, int s){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, H);
        cal.set(Calendar.MINUTE, i);
        cal.set(Calendar.SECOND, s);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public void setRiseLength(Date begin, Date end){
        from = begin; to = end;
    };
    public void setRiseLength(String begin, String end){
        from = getTimeFromString(begin); to = getTimeFromString(end);
    };

    public String getRiseLengthText(){
        int H = (int) (riseLength / 3600000);
        int i = (int) ((riseLength % 3600000) / 60000);
        return H+"ч."+i+"м.";
    }
    public void setCurrent(Date date){
        current = (date != null) ? date : new Date();

        riseLength = to.getTime() - from.getTime();
        Long rise_current_interval = current.getTime() - from.getTime();

        if(riseLength == 0 ){
            risePercentStage = 0;
            return;
        }

        if(current.before(from) || current.after(to)){  // всё что не день, то ночь =)
            night = true;
            riseLength = one_day - riseLength;
            rise_current_interval = riseLength - (from.getTime() - current.getTime());
            Date buff = from;
            from = to;
            to = buff;
        }

        risePercentStage = (float) rise_current_interval / riseLength;
    };

    public void setCurrent(Long date){
        date = date * 1000;
        Date normal = new Date(date);
        setCurrent(normal);
    }

    public void setCurrent(){
        Date normal = new Date();
        setCurrent(normal);
    }

    public boolean isNight(){
        return night;
    }

    public float getRisePercentStage(){
        return risePercentStage;
    }

    public String myDateFormat(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(date);
    }

    public Date getFrom(){
        return from;
    }
    public Date getTo(){
        return to;
    }
    public Date getCurrent(){
        return current;
    }
}
