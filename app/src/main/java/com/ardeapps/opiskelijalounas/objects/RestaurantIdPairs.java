package com.ardeapps.opiskelijalounas.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arttu on 1.11.2017.
 */

public class RestaurantIdPairs {
    public static Map<String, String> sodexoMap;
    public static Map<String, String> amicaMap;
    public static Map<String, String> unicafeMap;
    public static Map<String, String> sonaattiMap;
    public static Map<String, String> arkeaMap;
    public static Map<String, String> juvenesMap;

    public static String getIdForUrl(String restaurantId) {
        if (sodexoMap.get(restaurantId) != null) {
            return sodexoMap.get(restaurantId);
        } else if (amicaMap.get(restaurantId) != null) {
            return amicaMap.get(restaurantId);
        } else if (unicafeMap.get(restaurantId) != null) {
            return unicafeMap.get(restaurantId);
        } else if (sonaattiMap.get(restaurantId) != null) {
            return sonaattiMap.get(restaurantId);
        } else if (arkeaMap.get(restaurantId) != null) {
            return arkeaMap.get(restaurantId);
        } else if (juvenesMap.get(restaurantId) != null) {
            return juvenesMap.get(restaurantId);
        } else {
            return "";
        }
    }

    public static List<String> getSodexoRestaurantIds() {
        return new ArrayList<>(sodexoMap.keySet());
    }
    public static List<String> getAmicaRestaurantIds() {
        return new ArrayList<>(amicaMap.keySet());
    }
    public static List<String> getUnicafeRestaurantIds() {
        return new ArrayList<>(unicafeMap.keySet());
    }
    public static List<String> getSonaattiRestaurantIds() {
        return new ArrayList<>(sonaattiMap.keySet());
    }
    public static List<String> getArkeaRestaurantIds() {
        return new ArrayList<>(arkeaMap.keySet());
    }
    public static List<String> getJuvenesRestaurantIds() {
        return new ArrayList<>(juvenesMap.keySet());
    }
}
