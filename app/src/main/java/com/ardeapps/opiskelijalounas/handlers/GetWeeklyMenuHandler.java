package com.ardeapps.opiskelijalounas.handlers;

import com.ardeapps.opiskelijalounas.objects.Lunch;
import com.ardeapps.opiskelijalounas.objects.Meal;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Arttu on 2.10.2017.
 */

public interface GetWeeklyMenuHandler {
    // Weekday - meals
    void onGetWeeklyMealsSuccess(Map<String, ArrayList<Meal>> meals);
}
