package com.ardeapps.opiskelijalounas;

import android.location.Location;
import android.text.Html;
import android.text.Spanned;

import com.ardeapps.opiskelijalounas.objects.Restaurant;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.ardeapps.opiskelijalounas.objects.Restaurant.AMICA;
import static com.ardeapps.opiskelijalounas.objects.Restaurant.SODEXO;
import static com.ardeapps.opiskelijalounas.objects.Restaurant.SONAATTI;
import static java.lang.String.format;

/**
 * Created by Arttu on 6.7.2017.
 */

public class StringUtil {

    public static boolean isEmptyString(String text) {
        return text == null || text.trim().equals("");
    }

    public static String convertToReadable(String text) {
        if(isEmptyString(text)) {
            return "";
        }
        String split1 = convertUppercase(text.trim().toLowerCase(), " ");
        String split2 = convertUppercase(split1, "-");
        String split3 = convertUppercase(split2, "/");
        return convertUppercase(split3, ":");
    }

    private static String convertUppercase(String name, String splitter) {
        String[] wordsToUppercase = name.split(splitter);
        StringBuilder builder = new StringBuilder();
        int wordsMatch = 1;
        for (String wordToUppercase : wordsToUppercase) {
            String word = wordToUppercase.trim();
            // kaikki isolla
            if(word.length() > 3) {
                builder.append(word.substring(0, 1).toUpperCase() + word.substring(1));
                /*String[] wordsWithSpaces = name.split(" ");
                if(wordsWithSpaces.length > 0 && !word.substring(0, 1).equals("n"))
                    builder.append(word.substring(0, 1).toUpperCase() + word.substring(1));
                else
                    builder.append(word.substring(0, 1) + word.substring(1));*/
            } else if (word.length() == 3 || word.length() == 1) {
                builder.append(word.toUpperCase());
            } else if (word.length() == 2 && !word.equals("ja")) {
                builder.append(word.toUpperCase());
            } else {
                builder.append(word);
            }
            if(wordsMatch < wordsToUppercase.length) {
                builder.append(splitter);
            }
            wordsMatch++;
        }
        return builder.toString();
    }

    public static String getDistanceText(double targetLat, double targetLng) {
        AppRes appRes = (AppRes) AppRes.getContext();
        Location location = appRes.getLocationPref();
        String distanceText = "";
        if(location != null) {
            float[] results = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), targetLat, targetLng, results);
            long meters = 10 * Math.round(results[0] / 10);
            if (meters < 1000) {
                if (meters < 10)
                    distanceText = ">10m";
                else
                    distanceText = meters + "m";
            } else {
                distanceText = format(Locale.ENGLISH, "%.1f", (float) meters / 1000) + "km";
            }
        }
        return distanceText;
    }

 /*   public static String getTimeText(int hours, int minutes) {
        String minutesString = minutes < 10 ? "0" + minutes : minutes + "";
        String hoursString = hours < 10 ? "0" + hours : hours + "";
        return hoursString + ":" + minutesString;
    }*/

    public static String getDateForUrl(String companyName) {
        SimpleDateFormat sdf;
        if(companyName.equals(SODEXO)) {
            sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        } else if(companyName.equals(AMICA) || companyName.equals(SONAATTI)) {
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        } else {
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return sdf.format(cal.getTime());
    }

    public static String getTimeText(long millisAfterMidnight) {
        long minuteMillis = millisAfterMidnight % (TimeUnit.MILLISECONDS.toHours(millisAfterMidnight) * 360000);
        int minutes = (int)TimeUnit.MILLISECONDS.toMinutes(minuteMillis);
        String minutesString = minutes < 10 ? "0" + minutes : minutes + "";

        int hours = (int)TimeUnit.MILLISECONDS.toHours(millisAfterMidnight);
        String hoursString = hours < 10 ? "0" + hours : hours + "";

        return hoursString + ":" + minutesString;
    }

    public static String getLunchTimeText(Restaurant restaurant) {
        return StringUtil.getTimeText(restaurant.lunchStartTimeAfterMidnight) + " - " + StringUtil.getTimeText(restaurant.lunchEndTimeAfterMidnight);
    }

    public static String getDateText(Calendar c) {
        String dateText = "";
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        int selectedDay = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
        switch(selectedDay) {
            case 0:
                dateText = "maanantai";
                break;
            case 1:
                dateText = "tiistai";
                break;
            case 2:
                dateText = "keskiviikko";
                break;
            case 3:
                dateText = "torstai";
                break;
            case 4:
                dateText = "perjantai";
                break;
            case 5:
                dateText = "lauantai";
                break;
            case 6:
                dateText = "sunnuntai";
                break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return dateText + " " + sdf.format(c.getTime());
    }

    public enum OpenType {
        OPEN,
        OPEN_WEEKEND,
        CLOSED
    }

    public static OpenType isOpen(Restaurant restaurant) {
        Long start = restaurant.lunchStartTimeAfterMidnight;
        Long end = restaurant.lunchEndTimeAfterMidnight;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long timeAfterMidnight = System.currentTimeMillis() - cal.getTimeInMillis();
        if(timeAfterMidnight > start && timeAfterMidnight < end) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
            int selectedDay = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
            if(selectedDay == 5 || selectedDay == 6) {
                return OpenType.OPEN_WEEKEND;
            } else {
                return OpenType.OPEN;
            }
        } else {
            return OpenType.CLOSED;
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
}
