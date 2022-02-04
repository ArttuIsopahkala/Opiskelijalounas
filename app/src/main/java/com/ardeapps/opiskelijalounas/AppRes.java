package com.ardeapps.opiskelijalounas;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ardeapps.opiskelijalounas.objects.Meal;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by Arttu on 4.5.2017.
 */
public class AppRes extends Application {

    private static Context mContext;
    private ArrayList<Restaurant> restaurants = new ArrayList<>();
    private ArrayList<Restaurant> visibleRestaurants = new ArrayList<>();
    private ArrayList<String> cityNames = new ArrayList<>();
    private Map<String, ArrayList<Meal>> todayMenus = new HashMap<>();
    private Map<String, Map<String, ArrayList<Meal>>> weeklyMenus = new HashMap<>();

    // Admin kentät
    private boolean isAdmin;
    private ArrayList<Restaurant> restaurantRequests = new ArrayList<>();
    private Map<String, String> feedbacks = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static void showKeyBoard() {
        final InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    public static void hideKeyBoard(View tokenView) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(tokenView.getWindowToken(), 0);
    }

    public static Context getContext(){
        return mContext;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setRestaurantRequests(ArrayList<Restaurant> restaurantRequests) {
        this.restaurantRequests = restaurantRequests;
    }

    public ArrayList<Restaurant> getRestaurantRequests() {
        return restaurantRequests;
    }

    public void setFeedbacks(Map<String, String> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Map<String, String> getFeedbacks() {
        return feedbacks;
    }

    public void setRestaurants(ArrayList<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public ArrayList<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setVisibleRestaurants(ArrayList<Restaurant> visibleRestaurants) {
        this.visibleRestaurants = visibleRestaurants;
    }

    public ArrayList<Restaurant> getVisibleRestaurants() {
        return visibleRestaurants;
    }

    public void setCityNames(ArrayList<String> cityNames) {
        this.cityNames = cityNames;
    }

    public ArrayList<String> getCityNames() {
        return cityNames;
    }

    public void setTodayMenus(Map<String, ArrayList<Meal>> todayMenus) {
        this.todayMenus = todayMenus;
    }

    public Map<String, ArrayList<Meal>> getTodayMenus() {
        return todayMenus;
    }

    public void setWeeklyMenus(Map<String, Map<String, ArrayList<Meal>>> weeklyMenus) {
        this.weeklyMenus = weeklyMenus;
    }

    public Map<String, Map<String, ArrayList<Meal>>> getWeeklyMenus() {
        return weeklyMenus;
    }

    public void setFavouriteRestaurantIdsPref(ArrayList<String> favouriteRestaurantIds) {
        SharedPreferences userPref = getSharedPreferences("user", 0);
        SharedPreferences.Editor editor = userPref.edit();
        editor.putStringSet("favouriteRestaurantIds", new HashSet<>(favouriteRestaurantIds));
        editor.apply();
    }

    public ArrayList<String> getFavouriteRestaurantIdsPref() {
        SharedPreferences userPref = getSharedPreferences("user", 0);
        Set<String> emptySet = new HashSet<>();
        ArrayList<String> favouriteRestaurantIds = new ArrayList<>();
        favouriteRestaurantIds.addAll(userPref.getStringSet("favouriteRestaurantIds", emptySet));
        return favouriteRestaurantIds;
    }

    public void setMapLocationPref(CameraPosition camera) {
        SharedPreferences userPref = getSharedPreferences("user", 0);
        SharedPreferences.Editor editor = userPref.edit();
        editor.putFloat("mapLatitude", (float)camera.target.latitude);
        editor.putFloat("mapLongitude", (float)camera.target.longitude);
        editor.putFloat("mapZoom", camera.zoom);
        editor.apply();
    }

    public CameraPosition getMapLocationPref() {
        SharedPreferences userPref = getSharedPreferences("user", 0);
        double latitude = userPref.getFloat("mapLatitude", 0);
        double longitude = userPref.getFloat("mapLongitude", 0);
        float zoom = userPref.getFloat("mapZoom", 0);
        if(latitude > 0 && longitude > 0 && zoom > 0) {
            return new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(zoom).build();
        } else {
            return null;
        }
    }

    public void setLocationPref(Location location) {
        SharedPreferences userPref = getSharedPreferences("user", 0);
        SharedPreferences.Editor editor = userPref.edit();
        editor.putFloat("latitude", (float)location.getLatitude());
        editor.putFloat("longitude", (float)location.getLongitude());
        editor.apply();
    }

    public Location getLocationPref() {
        SharedPreferences userPref = getSharedPreferences("user", 0);
        Location location = new Location("");
        double latitude = userPref.getFloat("latitude", 0);
        double longitude = userPref.getFloat("longitude", 0);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        if(latitude > 0 && longitude > 0) {
            return location;
        } else {
            return null;
        }
    }
}
