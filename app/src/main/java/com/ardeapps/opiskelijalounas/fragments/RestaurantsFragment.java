package com.ardeapps.opiskelijalounas.fragments;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.Logger;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.adapters.RestaurantAdapter;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.google.android.gms.maps.model.CameraPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
public class RestaurantsFragment extends Fragment {

    AppRes appRes = (AppRes) AppRes.getContext();
    RestaurantAdapter restaurantAdapter;

    ArrayList<Restaurant> restaurants;
    ArrayList<String> cityNames;
    ArrayList<String> restaurantHints;
    ArrayList<String> cityHints;

    ListView restaurant_list;
    AutoCompleteTextView searchText;
    Button visibleRestaurantsButton;
    Button closeRestaurantsButton;
    ArrayList<Restaurant> filteredResults = new ArrayList<>();
    ArrayList<Restaurant> defaultRestaurants;

    public void refreshData() {
        restaurants = appRes.getRestaurants();
        cityNames = appRes.getCityNames();
    }

    public void update() {
        if(restaurantAdapter == null) {
            restaurantAdapter = new RestaurantAdapter(getActivity());
            restaurantAdapter.setRestaurants(filteredResults.size() == 0 ? defaultRestaurants : filteredResults, null);
            restaurant_list.setAdapter(restaurantAdapter);
        } else {
            restaurantAdapter.setRestaurants(filteredResults.size() == 0 ? defaultRestaurants : filteredResults, null);
            restaurantAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_restaurants, container, false);
        restaurant_list = (ListView) v.findViewById(R.id.restaurant_list);
        searchText = (AutoCompleteTextView) v.findViewById(R.id.searchText);
        visibleRestaurantsButton = (Button) v.findViewById(R.id.visibleRestaurantsButton);
        closeRestaurantsButton = (Button) v.findViewById(R.id.closeRestaurantsButton);

        ArrayList<String> displayHints = new ArrayList<>();
        restaurantHints = new ArrayList<>();
        cityHints = new ArrayList<>();
        for(Restaurant restaurant : restaurants) {
            displayHints.add(restaurant.name);
            restaurantHints.add(restaurant.name.toLowerCase());
        }
        for(String cityName : cityNames) {
            displayHints.add(cityName);
            cityHints.add(cityName.toLowerCase());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.popup_item, displayHints);
        searchText.setAdapter(adapter);

        setDefaultRestaurants();
        update();

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        searchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }
        });

        visibleRestaurantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filteredResults = appRes.getVisibleRestaurants();
                searchText.setText("");
                update();
            }
        });

        closeRestaurantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filteredResults = new ArrayList<>();
                searchText.setText("");
                setDefaultRestaurants();
                update();
            }
        });
        return v;
    }

    public void setDefaultRestaurants() {
        defaultRestaurants = new ArrayList<>();
        CameraPosition camera = appRes.getMapLocationPref();
        Location location = null;
        if(appRes.getLocationPref() != null) {
            location = appRes.getLocationPref();
        } else if(camera != null) {
            Location mapLocation = new Location("");
            mapLocation.setLatitude(camera.target.latitude);
            mapLocation.setLongitude(camera.target.longitude);
            location = mapLocation;
        }
        if (location != null) {
            for (Restaurant restaurant : restaurants) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), restaurant.latitude, restaurant.longitude, results);
                long meters = 10 * Math.round(results[0] / 10);
                if (meters < 20000) {
                    defaultRestaurants.add(restaurant);
                }
            }
            if (defaultRestaurants.size() == 0) {
                for (int i = 0; i < 10; i++) {
                    defaultRestaurants.add(restaurants.get(i));
                }
            }
        } else {
            defaultRestaurants = restaurants;
        }
    }

    private void performSearch() {
        String keyword = searchText.getText().toString().toLowerCase();
        filteredResults = new ArrayList<>();
        if(cityHints.contains(keyword)) {
            for(Restaurant restaurant : restaurants) {
                if(restaurant.city.toLowerCase().contains(keyword)) {
                    filteredResults.add(restaurant);
                }
            }
        } else {
            for(Restaurant restaurant : restaurants) {
                if(restaurant.name.toLowerCase().contains(keyword)) {
                    filteredResults.add(restaurant);
                }
            }
        }
        if(filteredResults.size() > 0) {
            AppRes.hideKeyBoard(searchText);
            update();
        } else {
            Logger.toast(R.string.map_no_results);
        }
    }
}
