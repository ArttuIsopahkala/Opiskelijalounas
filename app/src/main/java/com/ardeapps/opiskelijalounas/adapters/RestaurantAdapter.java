package com.ardeapps.opiskelijalounas.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.StringUtil;
import com.ardeapps.opiskelijalounas.handlers.RestaurantHolderListener;
import com.ardeapps.opiskelijalounas.objects.Lunch;
import com.ardeapps.opiskelijalounas.objects.LunchHolder;
import com.ardeapps.opiskelijalounas.objects.Meal;
import com.ardeapps.opiskelijalounas.objects.MealHolder;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.ardeapps.opiskelijalounas.objects.RestaurantHolder;
import com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ardeapps.opiskelijalounas.R.id.openIcon;

/**
 * Created by Arttu on 13.10.2017.
 */

public class RestaurantAdapter extends BaseAdapter {

    static RestaurantHolderListener mListener = null;

    public static void setListener(RestaurantHolderListener l) {
        mListener = l;
    }

    private ArrayList<Restaurant> restaurants;
    private AppRes appRes = (AppRes) AppRes.getContext();
    private static LayoutInflater inflater = null;
    private Activity activity;
    private PopupWindow optionsPopup;
    private Restaurant selectedRestaurant;
    private String day;

    public RestaurantAdapter(Activity activity) {
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        List<String> options = new ArrayList<>();
        options.add(activity.getString(R.string.restaurant_option_edit));

        // initialize a pop up window type
        optionsPopup = new PopupWindow(activity);

        // the drop down list is a list view
        ListView optionsList = new ListView(activity);

        // set our adapter and pass our pop up window contents
        optionsList.setAdapter(optionsAdapter(options));

        // some other visual settings
        optionsPopup.setFocusable(true);
        optionsPopup.setWidth(600);
        optionsPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // set the list view as pop up window content
        optionsPopup.setContentView(optionsList);

        // set the item click listener
        optionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                optionsPopup.dismiss();
                String selectedItemTag = v.getTag().toString();
                switch (selectedItemTag) {
                    case "0":
                        mListener.onEditClicked(selectedRestaurant);
                        break;
                }
            }
        });
    }

    public void setRestaurants(ArrayList<Restaurant> restaurants, String day) {
        this.restaurants = restaurants;
        this.day = day;
    }

    @Override
    public int getCount() {
        return restaurants.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final RestaurantHolder holder = new RestaurantHolder();
        if (v == null) {
            v = inflater.inflate(R.layout.restaurant_item, null);
        }
        holder.nameText = (TextView) v.findViewById(R.id.nameText);
        holder.distanceText = (TextView) v.findViewById(R.id.distanceText);
        holder.cityText = (TextView) v.findViewById(R.id.cityText);
        holder.timeToOpenText = (TextView) v.findViewById(R.id.timeToOpenText);
        holder.addLunchLink = (TextView) v.findViewById(R.id.addLunchLink);
        holder.lunchInfo = (TextView) v.findViewById(R.id.lunchInfo);
        holder.starIcon = (ImageView) v.findViewById(R.id.starIcon);
        holder.editIcon = (ImageView) v.findViewById(R.id.editIcon);
        holder.mealsContainer = (LinearLayout) v.findViewById(R.id.mealsContainer);
        holder.distanceContent = (LinearLayout) v.findViewById(R.id.distanceContent);
        holder.openIcon = v.findViewById(openIcon);

        final Restaurant restaurant = restaurants.get(position);
        final boolean isFavourite = appRes.getFavouriteRestaurantIdsPref().contains(restaurant.restaurantId);
        holder.nameText.setText(restaurant.name);
        holder.starIcon.setImageResource(isFavourite ? R.drawable.star_icon_full : R.drawable.star_icon_empty);

        holder.distanceContent.setVisibility(appRes.getLocationPref() != null ? View.VISIBLE : View.GONE);
        holder.distanceText.setText(StringUtil.getDistanceText(restaurant.latitude, restaurant.longitude));
        holder.cityText.setText(restaurant.city);

        boolean lunchAdded = !StringUtil.isEmptyString(RestaurantIdPairs.getIdForUrl(restaurant.restaurantId));
        boolean webAdded = !StringUtil.isEmptyString(restaurant.webUrl);
        holder.lunchInfo.setVisibility(lunchAdded || webAdded ? View.VISIBLE : View.GONE);
        holder.lunchInfo.setText(R.string.restaurant_lunch_added);
        holder.addLunchLink.setVisibility(!lunchAdded && !webAdded ? View.VISIBLE : View.GONE);
        if (restaurant.lunchStartTimeAfterMidnight != null && restaurant.lunchEndTimeAfterMidnight != null) {
            holder.timeToOpenText.setVisibility(View.VISIBLE);
            holder.openIcon.setVisibility(View.VISIBLE);
            holder.timeToOpenText.setText(AppRes.getContext().getString(R.string.restaurants_student_lunch) + " " + StringUtil.getLunchTimeText(restaurant));
            switch (StringUtil.isOpen(restaurant)) {
                case OPEN:
                    holder.openIcon.setBackgroundResource(R.drawable.green_circle);
                    break;
                case OPEN_WEEKEND:
                    holder.openIcon.setBackgroundResource(R.drawable.yellow_circle);
                    break;
                case CLOSED:
                    holder.openIcon.setBackgroundResource(R.drawable.red_circle);
                    break;
            }
        } else {
            holder.timeToOpenText.setVisibility(View.GONE);
            holder.openIcon.setVisibility(View.GONE);
        }

        holder.mealsContainer.removeAllViews();
        // Lounaslista halutaan näyttää
        if (!StringUtil.isEmptyString(day)) {
            if (lunchAdded) {
                Map<String, ArrayList<Meal>> weeklyMenus = appRes.getWeeklyMenus().get(restaurant.restaurantId);
                ArrayList<Meal> meals = weeklyMenus != null ? weeklyMenus.get(day) : new ArrayList<Meal>();
                // Lounaslista löytyy valitulle päivälle
                if (meals != null && meals.size() > 0) {
                    holder.lunchInfo.setVisibility(View.GONE);
                    holder.addLunchLink.setVisibility(View.GONE);
                    for (Meal meal : meals) {
                        if (meal.lunches != null && meal.lunches.size() > 0) {
                            MealHolder mealHolder = new MealHolder();
                            View mv = inflater.inflate(R.layout.meal_item, holder.mealsContainer, false);
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
                            holder.mealsContainer.addView(mv);
                        }
                    }
                } else {
                    // Lounaslistaa ei löydy valitulle päivälle
                    holder.lunchInfo.setText(R.string.restaurant_no_lunch);
                }
            } else {
                // Lounaslistaa ei löydy, mutta web sivu on lisätty
                if (webAdded) {
                    holder.lunchInfo.setText(R.string.restaurant_lunch_added);
                }
            }
        }

        holder.starIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onStarClicked(restaurant, !isFavourite);
            }
        });

        holder.editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRestaurant = restaurant;
                optionsPopup.showAsDropDown(holder.editIcon, 10, -20);
            }
        });

        holder.addLunchLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddLunchClicked(restaurant);
            }
        });

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onShowLunchClicked(restaurant);
            }
        });

        if (appRes.getIsAdmin()) {
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity, R.style.AppTheme);
                    alert.setMessage(restaurant.name + ": " + activity.getString(R.string.restaurant_remove_restaurant));
                    alert.setPositiveButton(activity.getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onAdminRemoveRestaurantClicked(restaurant.restaurantId);
                            dialog.dismiss();
                        }
                    });

                    alert.setNegativeButton(activity.getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alert.show();
                    return false;
                }
            });
        }
        return v;
    }

    private ArrayAdapter<String> optionsAdapter(List<String> options) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, options) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // setting the ID and text for every items in the list
                String text = getItem(position);
                String id = String.valueOf(position);
                // visual settings for the list item
                TextView listItem = new TextView(activity);

                listItem.setText(text);
                listItem.setTag(id);
                listItem.setTextSize(16);
                listItem.setPadding(10, 10, 10, 10);
                listItem.setTextColor(ContextCompat.getColor(activity, R.color.color_text_light));

                return listItem;
            }
        };

        return adapter;
    }
}