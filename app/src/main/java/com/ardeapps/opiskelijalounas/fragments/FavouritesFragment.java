package com.ardeapps.opiskelijalounas.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.StringUtil;
import com.ardeapps.opiskelijalounas.adapters.RestaurantAdapter;
import com.ardeapps.opiskelijalounas.objects.Lunch;
import com.ardeapps.opiskelijalounas.objects.LunchHolder;
import com.ardeapps.opiskelijalounas.objects.Meal;
import com.ardeapps.opiskelijalounas.objects.MealHolder;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.ardeapps.opiskelijalounas.objects.RestaurantHolder;

import java.util.ArrayList;
import java.util.Calendar;

import static android.os.Build.VERSION_CODES.M;

public class FavouritesFragment extends Fragment {

    RestaurantAdapter restaurantAdapter;
    AppRes appRes = (AppRes) AppRes.getContext();

    TextView no_favourites_info;
    ListView favourite_list;
    RelativeLayout calendarContainer;
    TextView dateText;
    ImageView nextIcon;
    ImageView previousIcon;

    ArrayList<String> favouriteRestaurantIds;
    ArrayList<Restaurant> restaurants;
    ArrayList<Restaurant> favouriteRestaurants;
    Calendar c;
    final String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

    public void update() {
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        int selectedDay = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
        String day = weekDays[selectedDay];

        favouriteRestaurants = new ArrayList<>();
        no_favourites_info.setVisibility(favouriteRestaurantIds.size() == 0 ? View.VISIBLE : View.GONE);
        boolean lunchListFound = false;
        if(favouriteRestaurantIds.size() > 0) {
            for (Restaurant restaurant : restaurants) {
                if (favouriteRestaurantIds.contains(restaurant.restaurantId)) {
                    favouriteRestaurants.add(restaurant);
                }
            }
            for(String id : favouriteRestaurantIds) {
                ArrayList<Meal> meals = appRes.getTodayMenus().get(id);
                if (meals != null && meals.size() > 0) {
                    lunchListFound = true;
                    break;
                }
            }
        }

        calendarContainer.setVisibility(lunchListFound ? View.VISIBLE : View.GONE);
        dateText.setText(StringUtil.getDateText(c));

        if(restaurantAdapter == null) {
            restaurantAdapter = new RestaurantAdapter(getActivity());
            restaurantAdapter.setRestaurants(favouriteRestaurants, day);
            favourite_list.setAdapter(restaurantAdapter);
        } else {
            restaurantAdapter.setRestaurants(favouriteRestaurants, day);
            restaurantAdapter.notifyDataSetChanged();
        }
    }

    public void refreshData() {
        favouriteRestaurantIds = appRes.getFavouriteRestaurantIdsPref();
        restaurants = appRes.getRestaurants();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favourites, container, false);
        favourite_list = (ListView) v.findViewById(R.id.favourite_list);
        no_favourites_info = (TextView) v.findViewById(R.id.no_favourites_info);
        calendarContainer = (RelativeLayout) v.findViewById(R.id.calendarContainer);
        dateText = (TextView) v.findViewById(R.id.dateText);
        nextIcon = (ImageView) v.findViewById(R.id.nextIcon);
        previousIcon = (ImageView) v.findViewById(R.id.previousIcon);

        c = Calendar.getInstance();

        update();

        previousIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
                int selectedDay = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
                if(selectedDay > 0) {
                    c.set(Calendar.HOUR, c.get(Calendar.HOUR) - 24);
                    update();
                }
            }
        });

        nextIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
                int selectedDay = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
                if(selectedDay < 6) {
                    c.set(Calendar.HOUR, c.get(Calendar.HOUR) + 24);
                    update();
                }
            }
        });
        return v;
    }
}
