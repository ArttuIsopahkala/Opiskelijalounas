package com.ardeapps.opiskelijalounas.fragments;


import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.Logger;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.StringUtil;
import com.ardeapps.opiskelijalounas.objects.Lunch;
import com.ardeapps.opiskelijalounas.objects.LunchHolder;
import com.ardeapps.opiskelijalounas.objects.Meal;
import com.ardeapps.opiskelijalounas.objects.MealHolder;
import com.ardeapps.opiskelijalounas.objects.Restaurant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class RestaurantFragment extends Fragment {

    public interface Listener {
        void addLunchClick(Restaurant restaurant);
    }

    Listener mListener = null;

    public void setListener(Listener l) {
        mListener = l;
    }

    TextView nameText;
    TextView addressText;
    TextView timeToOpenText;
    TextView menu_title;
    TextView noMealsText;
    LinearLayout mealsContainer;
    RelativeLayout calendarContainer;
    ImageView previousIcon;
    ImageView nextIcon;
    TextView dateText;
    WebView web_menu;
    Button actionButton;
    ScrollView scrollView;
    View openIcon;

    Calendar c;
    Restaurant restaurant;
    Map<String, ArrayList<Meal>> weeklyMeals;
    boolean calendarViewVisible;

    final String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void setWeeklyMeals(Map<String, ArrayList<Meal>> weeklyMeals) {
        this.weeklyMeals = weeklyMeals;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_restaurant, container, false);
        previousIcon = (ImageView) v.findViewById(R.id.previousIcon);
        nextIcon = (ImageView) v.findViewById(R.id.nextIcon);
        nameText = (TextView) v.findViewById(R.id.nameText);
        addressText = (TextView) v.findViewById(R.id.addressText);
        timeToOpenText = (TextView) v.findViewById(R.id.timeToOpenText);
        dateText = (TextView) v.findViewById(R.id.dateText);
        mealsContainer = (LinearLayout) v.findViewById(R.id.mealsContainer);
        web_menu = (WebView) v.findViewById(R.id.web_menu);
        calendarContainer = (RelativeLayout) v.findViewById(R.id.calendarContainer);
        actionButton = (Button) v.findViewById(R.id.actionButton);
        menu_title = (TextView) v.findViewById(R.id.menu_title);
        noMealsText = (TextView) v.findViewById(R.id.noMealsText);
        scrollView = (ScrollView) v.findViewById(R.id.scrollView);
        openIcon = v.findViewById(R.id.openIcon);

        web_menu.getSettings().setJavaScriptEnabled(true);
        web_menu.getSettings().setLoadWithOverviewMode(true);
        web_menu.getSettings().setUseWideViewPort(true);
        web_menu.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        web_menu.setScrollbarFadingEnabled(false);
        web_menu.setWebViewClient(new WebViewClient());

        menu_title.setText(StringUtil.convertToReadable(restaurant.name));

        String address = "";
        if (!StringUtil.isEmptyString(restaurant.address)) {
            address += StringUtil.convertToReadable(restaurant.address);
            if(!StringUtil.isEmptyString(restaurant.postalCode))
                address += ", ";
        }
        if (!StringUtil.isEmptyString(restaurant.postalCode)) {
            address += restaurant.postalCode;
            if(!StringUtil.isEmptyString(restaurant.city))
                address += ", ";
        }
        if (!StringUtil.isEmptyString(restaurant.city)) {
            address += restaurant.city;
        }
        addressText.setVisibility(StringUtil.isEmptyString(address) ? View.GONE : View.VISIBLE);
        addressText.setText(address);

        if(restaurant.lunchStartTimeAfterMidnight != null && restaurant.lunchEndTimeAfterMidnight != null) {
            timeToOpenText.setVisibility(View.VISIBLE);
            openIcon.setVisibility(View.VISIBLE);
            timeToOpenText.setText(AppRes.getContext().getString(R.string.restaurants_student_lunch) + " " + StringUtil.getLunchTimeText(restaurant));
            switch (StringUtil.isOpen(restaurant)) {
                case OPEN:
                    openIcon.setBackgroundResource(R.drawable.green_circle);
                    break;
                case OPEN_WEEKEND:
                    openIcon.setBackgroundResource(R.drawable.yellow_circle);
                    break;
                case CLOSED:
                    openIcon.setBackgroundResource(R.drawable.red_circle);
                    break;
            }
        } else {
            timeToOpenText.setVisibility(View.GONE);
            openIcon.setVisibility(View.GONE);
        }

        final boolean hasCalendarLunch = weeklyMeals != null && weeklyMeals.size() > 0;
        final boolean hasWebLunch = !StringUtil.isEmptyString(restaurant.webUrl);
        if(hasCalendarLunch && !hasWebLunch) {
            actionButton.setVisibility(View.GONE);
            showCalendarView();
        } else if(!hasCalendarLunch && hasWebLunch) {
            actionButton.setVisibility(View.GONE);
            showWebView();
        } else if(hasCalendarLunch && hasWebLunch) {
            actionButton.setVisibility(View.VISIBLE);
            actionButton.setText(R.string.restaurant_show_web_lunch);
            showCalendarView();
        } else {
            actionButton.setText(R.string.restaurant_add_lunch);
            actionButton.setVisibility(View.VISIBLE);
            calendarContainer.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
            web_menu.setVisibility(View.GONE);
            noMealsText.setVisibility(View.GONE);
        }

        previousIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
                int selectedDay = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
                if(selectedDay > 0) {
                    c.set(Calendar.HOUR, c.get(Calendar.HOUR) - 24);
                    showLunch();
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
                    showLunch();
                }
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasCalendarLunch && !hasWebLunch) {
                    mListener.addLunchClick(restaurant);
                } else {
                    if(calendarViewVisible) {
                        actionButton.setText(R.string.restaurant_show_calendar_lunch);
                        showWebView();
                    } else {
                        actionButton.setText(R.string.restaurant_show_web_lunch);
                        showCalendarView();
                    }
                }
            }
        });

        return v;
    }

    private void showCalendarView() {
        calendarViewVisible = true;
        calendarContainer.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
        web_menu.setVisibility(View.GONE);
        c = Calendar.getInstance();
        showLunch();
    }

    private void showWebView() {
        calendarViewVisible = false;
        calendarContainer.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
        web_menu.setVisibility(View.VISIBLE);
        web_menu.loadUrl(restaurant.webUrl);
    }

    private void showLunch() {
        mealsContainer.removeAllViews();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        int selectedDay = dayOfWeek == 0 ? 6 : dayOfWeek - 1;

        dateText.setText(StringUtil.getDateText(c));
        ArrayList<Meal> meals = weeklyMeals.get(weekDays[selectedDay]);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        if(meals != null && meals.size() > 0) {
            noMealsText.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            for(Meal meal : meals) {
                if(meal.lunches != null && meal.lunches.size() > 0) {
                    MealHolder mealHolder = new MealHolder();
                    View mv = inflater.inflate(R.layout.meal_item, mealsContainer, false);
                    mealHolder.lunchesContainer = (LinearLayout) mv.findViewById(R.id.lunchesContainer);
                    mealHolder.descriptionText = (TextView) mv.findViewById(R.id.descriptionText);
                    mealHolder.priceText = (TextView) mv.findViewById(R.id.priceText);

                    mealHolder.priceText.setVisibility(StringUtil.isEmptyString(meal.price) ? View.GONE : View.VISIBLE);
                    mealHolder.priceText.setText(StringUtil.isEmptyString(meal.price) ? "" : meal.price);
                    mealHolder.descriptionText.setVisibility(StringUtil.isEmptyString(meal.description) ? View.GONE : View.VISIBLE);
                    mealHolder.descriptionText.setText(StringUtil.isEmptyString(meal.description) ? "" : meal.description);

                    for (Lunch lunch : meal.lunches) {
                        LunchHolder lunchHolder = new LunchHolder();
                        View lv = inflater.inflate(R.layout.lunch_item, mealHolder.lunchesContainer, false);
                        lunchHolder.nameText = (TextView) lv.findViewById(R.id.nameText);
                        lunchHolder.dietText = (TextView) lv.findViewById(R.id.dietText);

                        lunchHolder.nameText.setVisibility(StringUtil.isEmptyString(lunch.name) ? View.GONE : View.VISIBLE);
                        lunchHolder.nameText.setText(StringUtil.isEmptyString(lunch.name) ? "" : lunch.name);
                        lunchHolder.dietText.setVisibility(StringUtil.isEmptyString(lunch.diet) ? View.GONE : View.VISIBLE);
                        lunchHolder.dietText.setText(StringUtil.isEmptyString(lunch.diet) ? "" : "(" + lunch.diet + ")");
                        mealHolder.lunchesContainer.addView(lv);
                    }
                    mealsContainer.addView(mv);
                }
            }
            int childHeight = mealsContainer.getHeight();
            boolean isScrollable = scrollView.getHeight() < childHeight + scrollView.getPaddingTop() + scrollView.getPaddingBottom();
            scrollView.setScrollbarFadingEnabled(!isScrollable);
        } else {
            scrollView.setVisibility(View.GONE);
            noMealsText.setVisibility(View.VISIBLE);
        }
    }
}
