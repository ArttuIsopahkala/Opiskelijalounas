package com.ardeapps.opiskelijalounas.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.Logger;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.StringUtil;
import com.ardeapps.opiskelijalounas.handlers.GetFeedbacksHandler;
import com.ardeapps.opiskelijalounas.handlers.GetRestaurantIdPairsHandler;
import com.ardeapps.opiskelijalounas.handlers.GetRestaurantRequestsHandler;
import com.ardeapps.opiskelijalounas.handlers.GetRestaurantsHandler;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs.amicaMap;
import static com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs.arkeaMap;
import static com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs.juvenesMap;
import static com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs.sodexoMap;
import static com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs.sonaattiMap;
import static com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs.unicafeMap;

public class FirebaseService {
    private final String RESTAURANTS = "restaurants";
    private final String RESTAURANT_REQUESTS = "restaurantRequests";
    private final String FEEDBACK = "feedback";
    private final String RESTAURANT_ID_PAIRS = "restaurantIdPairs";
    private final String SODEXO_MAP = "sodexoMap";
    private final String AMICA_MAP = "amicaMap";
    private final String UNICAFE_MAP = "unicafeMap";
    private final String SONAATTI_MAP = "sonaattiMap";
    private final String ARKEA_MAP = "arkeaMap";
    private final String JUVENES_MAP = "juvenesMap";

    private String TAG = "FirebaseService";
    private DatabaseReference database;

    private static FirebaseService instance;

    public static FirebaseService getInstance() {
        if(instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    private FirebaseService() {
        database = FirebaseDatabase.getInstance().getReference();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) AppRes.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
    private void onNetworkError() {
        Logger.toast(R.string.network_error);
    }

   /* public void setRestaurants(List<Restaurant> restaurants) {
        for(Restaurant restaurant : restaurants) {
            if(StringUtil.isEmptyString(restaurant.webUrl))
                database.child(RESTAURANTS).child(restaurant.restaurantId).child("webUrl").setValue("");
        }
    }*/

    public void sendFeedback(String message) {
        database.child(FEEDBACK).push().setValue(message);
    }

    public void setRestaurant(Restaurant restaurant) {
        database.child(RESTAURANTS).child(restaurant.restaurantId).setValue(restaurant);
    }

    public void sendRestaurantRequest(Restaurant restaurant) {
        if(StringUtil.isEmptyString(restaurant.restaurantId)) {
            database.child(RESTAURANT_REQUESTS).push().setValue(restaurant);
        } else {
            if(!StringUtil.isEmptyString(restaurant.restaurantId))
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("restaurantId").setValue(restaurant.restaurantId);
            if(restaurant.latitude != null)
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("latitude").setValue(restaurant.latitude);
            if(restaurant.longitude != null)
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("longitude").setValue(restaurant.longitude);
            if(!StringUtil.isEmptyString(restaurant.name))
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("name").setValue(restaurant.name);
            if(!StringUtil.isEmptyString(restaurant.address))
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("address").setValue(restaurant.address);
            if(!StringUtil.isEmptyString(restaurant.postalCode))
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("postalCode").setValue(restaurant.postalCode);
            if(!StringUtil.isEmptyString(restaurant.city))
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("city").setValue(restaurant.city);
            if(!StringUtil.isEmptyString(restaurant.webUrl))
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("webUrl").setValue(restaurant.webUrl);
            if(restaurant.lunchStartTimeAfterMidnight != null)
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("lunchStartTimeAfterMidnight").setValue(restaurant.lunchStartTimeAfterMidnight);
            if(restaurant.lunchEndTimeAfterMidnight != null)
                database.child(RESTAURANT_REQUESTS).child(restaurant.restaurantId).child("lunchEndTimeAfterMidnight").setValue(restaurant.lunchEndTimeAfterMidnight);
        }
    }

    public void getRestaurantRequests(final GetRestaurantRequestsHandler handler) {
        Log.i(TAG, "getRestaurantRequests");
        if(isNetworkAvailable()) {
            database.child(RESTAURANT_REQUESTS).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Restaurant> restaurantRequests = new ArrayList<>();
                    for(DataSnapshot object : dataSnapshot.getChildren()) {
                        Restaurant restaurant = object.getValue(Restaurant.class);
                        if(restaurant != null) {
                            restaurantRequests.add(restaurant);
                        }
                    }
                    handler.onGetRestaurantRequestsSuccess(restaurantRequests);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else onNetworkError();
    }

    public void getFeedbacks(final GetFeedbacksHandler handler) {
        Log.i(TAG, "getFeedbacks");
        if(isNetworkAvailable()) {
            database.child(FEEDBACK).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, String> feedbacks = new HashMap<>();
                    for(DataSnapshot object : dataSnapshot.getChildren()) {
                        String feedback = object.getValue(String.class);
                        if(feedback != null) {
                            feedbacks.put(object.getKey(), feedback);
                        }
                    }
                    handler.onGetFeedbacksSuccess(feedbacks);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else onNetworkError();
    }

    public void removeRestaurantRequest(String restaurantId) {
        Log.i(TAG, "removeRestaurantRequest");
        database.child(RESTAURANT_REQUESTS).child(restaurantId).setValue(null);
    }

    public void removeFeedback(String key) {
        Log.i(TAG, "removeFeedback");
        database.child(FEEDBACK).child(key).setValue(null);
    }

    public void getRestaurants(final GetRestaurantsHandler handler) {
        Log.i(TAG, "getRestaurants");
        if(isNetworkAvailable()) {
            database.child(RESTAURANTS).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Restaurant> restaurants = new ArrayList<>();
                    for(DataSnapshot object : dataSnapshot.getChildren()) {
                        Restaurant restaurant = object.getValue(Restaurant.class);
                        if(restaurant != null) {
                            restaurant.name = StringUtil.convertToReadable(restaurant.name);
                            restaurant.address = StringUtil.convertToReadable(restaurant.address);
                            restaurant.city = StringUtil.convertToReadable(restaurant.city);
                            restaurants.add(restaurant);
                        }
                    }

                    handler.onGetRestaurantsSuccess(restaurants);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else onNetworkError();
    }

    public void getRestaurantIdPairs(final GetRestaurantIdPairsHandler handler) {
        if(isNetworkAvailable()) {
            database.child(RESTAURANT_ID_PAIRS).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    sodexoMap = convertToMap(dataSnapshot.child(SODEXO_MAP));
                    amicaMap = convertToMap(dataSnapshot.child(AMICA_MAP));
                    unicafeMap = convertToMap(dataSnapshot.child(UNICAFE_MAP));
                    sonaattiMap = convertToMap(dataSnapshot.child(SONAATTI_MAP));
                    arkeaMap = convertToMap(dataSnapshot.child(ARKEA_MAP));
                    juvenesMap = convertToMap(dataSnapshot.child(JUVENES_MAP));

                    handler.onGetRestaurantIdPairsSuccess();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else onNetworkError();
    }

    private Map<String, String> convertToMap(DataSnapshot dataSnapshot) {
        Map<String, String> restaurantIdMap = new HashMap<>();
        for(DataSnapshot object : dataSnapshot.getChildren()) {
            restaurantIdMap.put(object.getKey(), object.getValue(String.class));
        }
        return restaurantIdMap;
    }

    public void removeRestaurant(String restaurantId) {
        database.child(RESTAURANTS).child(restaurantId).setValue(null);
        if(RestaurantIdPairs.getSodexoRestaurantIds().contains(restaurantId)) {
            database.child(RESTAURANT_ID_PAIRS).child(SODEXO_MAP).child(restaurantId).setValue(null);
        } else if(RestaurantIdPairs.getAmicaRestaurantIds().contains(restaurantId)) {
            database.child(RESTAURANT_ID_PAIRS).child(AMICA_MAP).child(restaurantId).setValue(null);
        } else if(RestaurantIdPairs.getUnicafeRestaurantIds().contains(restaurantId)) {
            database.child(RESTAURANT_ID_PAIRS).child(UNICAFE_MAP).child(restaurantId).setValue(null);
        } else if(RestaurantIdPairs.getSonaattiRestaurantIds().contains(restaurantId)) {
            database.child(RESTAURANT_ID_PAIRS).child(SONAATTI_MAP).child(restaurantId).setValue(null);
        } else if(RestaurantIdPairs.getArkeaRestaurantIds().contains(restaurantId)) {
            database.child(RESTAURANT_ID_PAIRS).child(ARKEA_MAP).child(restaurantId).setValue(null);
        }
    }
}
