package com.ardeapps.opiskelijalounas;

import android.util.Log;
import android.widget.Toast;

import com.ardeapps.opiskelijalounas.objects.Lunch;
import com.ardeapps.opiskelijalounas.objects.Meal;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Arttu on 21.8.2017.
 */

public class Logger {
    public static void log(Object message) {
        String className = new Exception().getStackTrace()[1].getFileName();
        Log.e(className, message+"");
    }
    public static void toast(Object message) {
        Toast.makeText(AppRes.getContext(), message+"", Toast.LENGTH_LONG).show();
    }
    public static void toast(int resourceId) {
        Toast.makeText(AppRes.getContext(), resourceId, Toast.LENGTH_LONG).show();
    }

    public static void logDaysMenu(String weekday, Map<String, ArrayList<Meal>> todayMeals) {
        for (Map.Entry<String, ArrayList<Meal>> entry : todayMeals.entrySet())
        {
            Logger.log(entry.getKey());
            if(entry.getValue() != null) {
                for (Meal meal : entry.getValue()) {
                    String desc = "";
                    if (!StringUtil.isEmptyString(meal.price))
                        desc += meal.price;
                    if (!StringUtil.isEmptyString(meal.description))
                        desc += " - " + meal.description;
                    Logger.log(desc);
                    for (Lunch lunch : meal.lunches) {
                        if (!StringUtil.isEmptyString(lunch.name))
                            desc = lunch.name;
                        if (!StringUtil.isEmptyString(lunch.diet))
                            desc += " - " + lunch.diet;
                        Logger.log(desc);
                    }
                }
            }
        }
    }

    public static void logTodayMenu(Map<String, ArrayList<Meal>> todayMeals) {
        for (Map.Entry<String, ArrayList<Meal>> entry : todayMeals.entrySet())
        {
            Logger.log(entry.getKey());
            if(entry.getValue() != null) {
                for (Meal meal : entry.getValue()) {
                    String desc = "";
                    if (!StringUtil.isEmptyString(meal.price))
                        desc += meal.price;
                    if (!StringUtil.isEmptyString(meal.description))
                        desc += " - " + meal.description;
                    Logger.log(desc);
                    for (Lunch lunch : meal.lunches) {
                        if (!StringUtil.isEmptyString(lunch.name))
                            desc = lunch.name;
                        if (!StringUtil.isEmptyString(lunch.diet))
                            desc += " - " + lunch.diet;
                        Logger.log(desc);
                    }
                }
            }
        }
    }

    // day of week and meals
    // with day of week
    // params = restaurantId
    public static void logWeeklyMenu(Map<String, ArrayList<Meal>> meals) {
        for (Map.Entry<String, ArrayList<Meal>> entry : meals.entrySet())
        {
            Logger.log(entry.getKey());
            if(entry.getValue() != null) {
                for (Meal meal : entry.getValue()) {
                    String desc = "";
                    if (!StringUtil.isEmptyString(meal.price))
                        desc += meal.price;
                    if (!StringUtil.isEmptyString(meal.description))
                        desc += " - " + meal.description;
                    Logger.log(desc);
                    for (Lunch lunch : meal.lunches) {
                        if (!StringUtil.isEmptyString(lunch.name))
                            desc = lunch.name;
                        if (!StringUtil.isEmptyString(lunch.diet))
                            desc += " - " + lunch.diet;
                        Logger.log(desc);
                    }
                }
            }
        }
    }

    // params: restuarntId, weekday: weekdayAsString
    public static void logDayMenu(String restaurantId, Map<String, Map<String, ArrayList<Meal>>> weeklyMenus) {
        Map<String, ArrayList<Meal>> meals = weeklyMenus.get(restaurantId);
        for (Map.Entry<String, ArrayList<Meal>> weakly : meals.entrySet()) {
            Logger.log(weakly.getKey());
            if(weakly.getValue() != null) {
                for (Meal meal : weakly.getValue()) {
                    String desc = "";
                    if (!StringUtil.isEmptyString(meal.price))
                        desc += meal.price;
                    if (!StringUtil.isEmptyString(meal.description))
                        desc += " - " + meal.description;
                    Logger.log(desc);
                    for (Lunch lunch : meal.lunches) {
                        if (!StringUtil.isEmptyString(lunch.name))
                            desc = lunch.name;
                        if (!StringUtil.isEmptyString(lunch.diet))
                            desc += " - " + lunch.diet;
                        Logger.log(desc);
                    }
                }
            }
        }
    }

    public static void logCityRestaurantSizes(ArrayList<Restaurant> restaurants) {
        Map<String, ArrayList<Boolean>> citySizes = new HashMap<>();
        for(Restaurant restaurant : restaurants) {
            ArrayList<Boolean> size = citySizes.get(restaurant.city) != null ? citySizes.get(restaurant.city) : new ArrayList<Boolean>();
            size.add(!StringUtil.isEmptyString(RestaurantIdPairs.getIdForUrl(restaurant.restaurantId)) || !StringUtil.isEmptyString(restaurant.webUrl));
            citySizes.put(restaurant.city, size);
        }
        List<Map.Entry<String, ArrayList<Boolean>>> list = new LinkedList<>(citySizes.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, ArrayList<Boolean>>>() {
            public int compare(Map.Entry<String, ArrayList<Boolean>> o1, Map.Entry<String, ArrayList<Boolean>> o2) {
                return o2.getValue().size() - o1.getValue().size();
            }
        });
        String text = "";
        int totalAdded = 0;
        int totalSize = 0;
        for (Map.Entry<String, ArrayList<Boolean>> entry : list) {
            int added = 0;
            for(Boolean lunchAdded : entry.getValue()) {
                if(lunchAdded) {
                    added++;
                }
            }
            text += entry.getKey() + " " + added + "/" + entry.getValue().size() + "\n";
            totalAdded += added;
            totalSize += entry.getValue().size();
        }
        Logger.log("Yhteensä: " + totalAdded + "/" + totalSize);
        Logger.log(text);

    }
}
