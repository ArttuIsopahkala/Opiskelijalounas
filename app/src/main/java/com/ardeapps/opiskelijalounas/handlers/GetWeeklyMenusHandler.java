package com.ardeapps.opiskelijalounas.handlers;

import com.ardeapps.opiskelijalounas.objects.Meal;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Arttu on 2.10.2017.
 */

public interface GetWeeklyMenusHandler {
    // Map: restaurantId, weekDay->meals....restaurantId->meals
    void onGetWeeklyMenusSuccess(Map<String, Map<String, ArrayList<Meal>>> weeklyMenus, Map<String, ArrayList<Meal>> todayMenus);
}
