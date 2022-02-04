package com.ardeapps.opiskelijalounas.fragments;


import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.Logger;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.StringUtil;
import com.ardeapps.opiskelijalounas.handlers.GetFeedbacksHandler;
import com.ardeapps.opiskelijalounas.handlers.GetRestaurantIdPairsHandler;
import com.ardeapps.opiskelijalounas.handlers.GetRestaurantRequestsHandler;
import com.ardeapps.opiskelijalounas.handlers.GetRestaurantsHandler;
import com.ardeapps.opiskelijalounas.handlers.GetWeeklyMenusHandler;
import com.ardeapps.opiskelijalounas.objects.Meal;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.ardeapps.opiskelijalounas.services.FirebaseService;
import com.ardeapps.opiskelijalounas.services.FragmentListeners;
import com.ardeapps.opiskelijalounas.services.LunchService;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.ardeapps.opiskelijalounas.services.FragmentListeners.MY_PERMISSION_ACCESS_COARSE_LOCATION;

public class LoaderFragment extends Fragment implements GetRestaurantsHandler,
        GetRestaurantRequestsHandler, GetFeedbacksHandler, GetWeeklyMenusHandler, GetRestaurantIdPairsHandler {

    AppRes appRes = (AppRes) AppRes.getContext();
    ImageView loader_icon_inner;
    ImageView loader_icon_outer;
    Location location = null;
    private int ANIMATION_LENGTH = 500;

    public interface Listener {
        void onMainDataLoaded();
    }

    Listener mListener = null;

    public void setListener(Listener l) {
        mListener = l;
    }

    private void showLoader() {
        Animation scaleIn = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleIn.setDuration(ANIMATION_LENGTH);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(ANIMATION_LENGTH);

        Animation rotation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotation.setDuration(ANIMATION_LENGTH * 6);
        rotation.setRepeatCount(Animation.INFINITE);

        AnimationSet innerAnimation = new AnimationSet(true);
        innerAnimation.addAnimation(scaleIn);
        innerAnimation.addAnimation(fadeIn);

        AnimationSet outerAnimation = new AnimationSet(true);
        outerAnimation.setInterpolator(new LinearInterpolator());
        outerAnimation.addAnimation(fadeIn);
        outerAnimation.addAnimation(rotation);

        loader_icon_inner.startAnimation(innerAnimation);
        loader_icon_outer.startAnimation(outerAnimation);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentListeners.getInstance().setPermissionHandledListener(new FragmentListeners.PermissionHandledListener() {
            @Override
            public void onPermissionGranted(int MY_PERMISSION) {
                if(MY_PERMISSION == MY_PERMISSION_ACCESS_COARSE_LOCATION) {
                    loadLocation();
                }
            }
            @Override
            public void onPermissionDenied(int MY_PERMISSION) {
                if(MY_PERMISSION == MY_PERMISSION_ACCESS_COARSE_LOCATION) {
                    loadMainData();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_loader, container, false);
        loader_icon_outer = (ImageView) v.findViewById(R.id.loader_icon_outer);
        loader_icon_inner = (ImageView) v.findViewById(R.id.loader_icon_inner);
        showLoader();

        String android_id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        boolean isAdmin = android_id != null && android_id.equals("8d6349fed10ab504");
        //boolean isAdmin = false;
        appRes.setIsAdmin(isAdmin);

        if(appRes.getIsAdmin()) {
            FirebaseService.getInstance().getRestaurantRequests(this);
        } else {
            loadLocation();
        }
        return v;
    }

    @Override
    public void onGetRestaurantRequestsSuccess(ArrayList<Restaurant> restaurantRequests) {
        appRes.setRestaurantRequests(restaurantRequests);
        FirebaseService.getInstance().getFeedbacks(this);
    }

    @Override
    public void onGetFeedbacksSuccess(Map<String, String> feedbacks) {
        appRes.setFeedbacks(feedbacks);
        loadLocation();
    }

    private void loadLocation() {
        final boolean locationPermissionNeeded = ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        if (locationPermissionNeeded) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        appRes.setLocationPref(location);
                    }
                    loadMainData();
                }
            });
        }
    }

    private void loadMainData() {
        FirebaseService.getInstance().getRestaurantIdPairs(this);
    }

    @Override
    public void onGetRestaurantIdPairsSuccess() {
        FirebaseService.getInstance().getRestaurants(this);
    }

    @Override
    public void onGetRestaurantsSuccess(ArrayList<Restaurant> restaurants) {
        CameraPosition camera = appRes.getMapLocationPref();
        if(appRes.getLocationPref() != null) {
            location = appRes.getLocationPref();
        } else if(camera != null) {
            Location mapLocation = new Location("");
            mapLocation.setLatitude(camera.target.latitude);
            mapLocation.setLongitude(camera.target.longitude);
            location = mapLocation;
        }
        if(location != null) {
            Collections.sort(restaurants, new Comparator<Restaurant>() {

                @Override
                public int compare(Restaurant a, Restaurant b) {
                    Location locationA = new Location("");
                    locationA.setLatitude(a.latitude);
                    locationA.setLongitude(a.longitude);
                    Location locationB = new Location("");
                    locationB.setLatitude(b.latitude);
                    locationB.setLongitude(b.longitude);
                    float distanceOne = location.distanceTo(locationA);
                    float distanceTwo = location.distanceTo(locationB);
                    return Float.compare(distanceOne, distanceTwo);
                }
            });
        }

        ArrayList<String> cityNames = new ArrayList<>();
        Map<Double, ArrayList<Restaurant>> locationMatchingRestaurants = new HashMap<>();
        for(Restaurant restaurant : restaurants) {
            ArrayList<Restaurant> foundRestaurants = locationMatchingRestaurants.get(restaurant.longitude) != null ?
                    locationMatchingRestaurants.get(restaurant.longitude) : new ArrayList<Restaurant>();
            foundRestaurants.add(restaurant);
            locationMatchingRestaurants.put(restaurant.longitude, foundRestaurants);

            if(!cityNames.contains(StringUtil.convertToReadable(restaurant.city))) {
                cityNames.add(StringUtil.convertToReadable(restaurant.city));
            }
        }
        Map<String, Double> newLatitudes = new HashMap<>();
        for (Map.Entry<Double, ArrayList<Restaurant>> entry : locationMatchingRestaurants.entrySet()) {
            if(entry.getValue().size() > 1) {
                int index = 1;
                for (Restaurant convertRestaurant : entry.getValue()) {
                    newLatitudes.put(convertRestaurant.restaurantId, convertRestaurant.longitude + index * 0.0003); // 0.0000449 = 5m latitudea
                    index++;
                }
            }
        }
        for(Restaurant restaurant : restaurants) {
            if(newLatitudes.get(restaurant.restaurantId) != null) {
                restaurant.longitude = newLatitudes.get(restaurant.restaurantId);
            }
        }

        //Logger.logCityRestaurantSizes(restaurants);
        appRes.setCityNames(cityNames);
        appRes.setRestaurants(restaurants);

        LunchService.getInstance().getWeeklyMenus(appRes.getFavouriteRestaurantIdsPref(), this);
    }

    @Override
    public void onGetWeeklyMenusSuccess(Map<String, Map<String, ArrayList<Meal>>> weeklyMenus, Map<String, ArrayList<Meal>> todayMenus) {
        appRes.setTodayMenus(todayMenus);
        appRes.setWeeklyMenus(weeklyMenus);
        mListener.onMainDataLoaded();
    }
}
