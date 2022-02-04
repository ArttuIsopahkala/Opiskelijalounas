package com.ardeapps.opiskelijalounas.fragments;

import android.app.TimePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.Logger;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.StringUtil;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AddRestaurantFragment extends Fragment {

    public interface AddRestaurantListener
    {
        void onRestaurantChanged(Restaurant restaurant);
        void onRestaurantAdminChanged(Restaurant restaurant);
    }

    AddRestaurantListener mListener = null;

    public void setListener(AddRestaurantListener l) {
        mListener = l;
    }

    EditText webText;
    EditText nameText;
    AutoCompleteTextView cityText;
    EditText addressText;
    EditText postalCodeText;
    TextView startTimeText;
    TextView endTimeText;
    TextView menu_title;
    Button submitButton;
    Calendar startCalendar;
    Calendar endCalendar;
    LinearLayout addressContainer;
    AppRes appRes = (AppRes) AppRes.getContext();

    Restaurant restaurant;
    boolean requestWebUrl;

    Long startAfterMidnight;
    Long endAfterMidnight;
    int startHour = 11;
    int startMinute = 0;
    int endHour = 14;
    int endMinute = 0;

    public void setRestaurant(Restaurant restaurant, boolean requestWebUrl) {
        this.restaurant = restaurant;
        this.requestWebUrl = requestWebUrl;
    }

    public void update() {
        resetFields();
        if (requestWebUrl) {
            AppRes.showKeyBoard();
            webText.requestFocus();
        }
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        final boolean editAddress = appRes.getIsAdmin() || restaurant == null;
        addressContainer.setVisibility(editAddress ? View.VISIBLE : View.GONE);

        // Lisätään ravintola
        if (restaurant != null) {
            if (editAddress) {
                cityText.setText(restaurant.city);
                addressText.setText(restaurant.address);
                postalCodeText.setText(restaurant.postalCode);
            }
            nameText.setText(restaurant.name);
            webText.setText(restaurant.webUrl);

            if (restaurant.lunchStartTimeAfterMidnight != null && restaurant.lunchEndTimeAfterMidnight != null) {
                startAfterMidnight = restaurant.lunchStartTimeAfterMidnight;
                endAfterMidnight = restaurant.lunchEndTimeAfterMidnight;
                startCalendar.setTimeInMillis(restaurant.lunchStartTimeAfterMidnight);
                endCalendar.setTimeInMillis(restaurant.lunchEndTimeAfterMidnight);
                startHour = startCalendar.get(Calendar.HOUR);
                startMinute = startCalendar.get(Calendar.MINUTE);
                endHour = endCalendar.get(Calendar.HOUR);
                endMinute = endCalendar.get(Calendar.MINUTE);
                startTimeText.setText(StringUtil.getTimeText(restaurant.lunchStartTimeAfterMidnight));
                endTimeText.setText(StringUtil.getTimeText(restaurant.lunchEndTimeAfterMidnight));
            }
        } else {
            menu_title.setText(R.string.edit_restaurant_add_title);
        }

        final TimePickerDialog.OnTimeSetListener startTime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedStartHour, int selectedStartMinute) {
                startHour = selectedStartHour;
                startMinute = selectedStartMinute;
                startAfterMidnight = TimeUnit.HOURS.toMillis(selectedStartHour) + TimeUnit.MINUTES.toMillis(selectedStartMinute);
                startTimeText.setText(StringUtil.getTimeText(startAfterMidnight));
            }
        };

        final TimePickerDialog.OnTimeSetListener endTime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedEndHour, int selectedEndMinute) {
                endHour = selectedEndHour;
                endMinute = selectedEndMinute;
                endAfterMidnight = TimeUnit.HOURS.toMillis(selectedEndHour) + TimeUnit.MINUTES.toMillis(selectedEndMinute);
                endTimeText.setText(StringUtil.getTimeText(endAfterMidnight));
            }
        };

        startTimeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TimePickerDialog(getActivity(), startTime, startHour, startMinute, false).show();
            }
        });

        endTimeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TimePickerDialog(getActivity(), endTime, endHour, endMinute, false).show();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityText.getText().toString();
                String address = addressText.getText().toString();
                String postalCode = postalCodeText.getText().toString();
                String webUrl = webText.getText().toString();
                String name = nameText.getText().toString();
                List<String> domains = new ArrayList<>(Arrays.asList(".com", ".fi", ".net", ".org"));

                if (StringUtil.isEmptyString(name)) {
                    Logger.toast(R.string.edit_restaurant_add_name);
                    return;
                }
                if (!StringUtil.isEmptyString(webUrl)) {
                    boolean validUrl = false;
                    for(String domain : domains) {
                        if(webUrl.contains(domain)) {
                            validUrl = true;
                            break;
                        }
                    }
                    if(!validUrl) {
                        Logger.toast(R.string.edit_restaurant_invalid_url);
                        return;
                    }
                }
                if (startAfterMidnight != null && endAfterMidnight == null) {
                    Logger.toast(R.string.edit_restaurant_time_empty);
                    return;
                }
                if (startAfterMidnight == null && endAfterMidnight != null) {
                    Logger.toast(R.string.edit_restaurant_time_empty);
                    return;
                }
                if (startAfterMidnight != null && endAfterMidnight != null) {
                    if (startAfterMidnight > endAfterMidnight) {
                        Logger.toast(R.string.edit_restaurant_time_before);
                        return;
                    }
                }

                String fullAddress = "";
                if (!StringUtil.isEmptyString(address)) {
                    fullAddress += address.toLowerCase();
                }
                if (!StringUtil.isEmptyString(postalCode)) {
                    fullAddress += " " + postalCode;
                }
                if (!StringUtil.isEmptyString(city)) {
                    fullAddress += " " + city;
                }

                Restaurant restaurantToSave = new Restaurant();
                if (appRes.getIsAdmin()) {
                    if (restaurant != null) {
                        // Muokataan
                        restaurantToSave = restaurant.clone();
                    } else {
                        // Lisätään
                        ArrayList<Integer> ids = new ArrayList<>();
                        for (Restaurant rest : appRes.getRestaurants()) {
                            ids.add(Integer.valueOf(rest.restaurantId));
                        }
                        restaurantToSave.restaurantId = String.valueOf(Collections.max(ids) + 1);
                    }
                    LatLng location = getLocationFromAddress(fullAddress);
                    if(location != null) {
                        if(restaurantToSave.latitude != null && restaurantToSave.longitude != null &&
                                (location.latitude != restaurantToSave.latitude || location.longitude != restaurantToSave.longitude)) {
                            Logger.toast("Ravintolan sijainti muuttui!");
                        }
                        restaurantToSave.latitude = location.latitude;
                        restaurantToSave.longitude = location.longitude;
                    } else {
                        Logger.toast("Sijaintia ei löytynyt!");
                    }
                    restaurantToSave.name = name.toUpperCase();
                    restaurantToSave.city = city.toUpperCase();
                    restaurantToSave.address = address.toUpperCase();
                    restaurantToSave.postalCode = postalCode.toUpperCase();
                    restaurantToSave.webUrl = webUrl;
                    restaurantToSave.lunchStartTimeAfterMidnight = startAfterMidnight;
                    restaurantToSave.lunchEndTimeAfterMidnight = endAfterMidnight;
                    mListener.onRestaurantAdminChanged(restaurantToSave);
                } else {
                    // Käyttäjä muokkaa
                    if (restaurant != null) {
                        restaurantToSave.restaurantId = restaurant.restaurantId;
                        // name on aina täytetty
                        if (!restaurant.name.equals(name))
                            restaurantToSave.name = name;
                        if (startAfterMidnight != null && endAfterMidnight != null) {
                            if(!startAfterMidnight.equals(restaurant.lunchStartTimeAfterMidnight))
                                restaurantToSave.lunchStartTimeAfterMidnight = startAfterMidnight;
                            if(!endAfterMidnight.equals(restaurant.lunchEndTimeAfterMidnight))
                                restaurantToSave.lunchEndTimeAfterMidnight = endAfterMidnight;
                        }

                        if (!StringUtil.isEmptyString(webUrl) && !webUrl.equals(restaurant.webUrl))
                            restaurantToSave.webUrl = webUrl;
                    } else {
                        // Käyttäjä lisää
                        ArrayList<Integer> ids = new ArrayList<>();
                        for (Restaurant rest : appRes.getRestaurants()) {
                            ids.add(Integer.valueOf(rest.restaurantId));
                        }
                        restaurantToSave.restaurantId = String.valueOf(Collections.max(ids) + 1);
                        restaurantToSave.name = name;
                        restaurantToSave.city = city;
                        restaurantToSave.address = address;
                        restaurantToSave.postalCode = postalCode;
                        restaurantToSave.webUrl = webUrl;
                        restaurantToSave.lunchStartTimeAfterMidnight = startAfterMidnight;
                        restaurantToSave.lunchEndTimeAfterMidnight = endAfterMidnight;
                        LatLng location = getLocationFromAddress(fullAddress);
                        restaurantToSave.latitude = location.latitude;
                        restaurantToSave.longitude = location.longitude;
                    }
                    mListener.onRestaurantChanged(restaurantToSave);
                }
            }
        });
    }

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(getActivity());
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return p1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_restaurant, container, false);
        submitButton = (Button) v.findViewById(R.id.submitButton);
        startTimeText = (TextView) v.findViewById(R.id.startTimeText);
        endTimeText = (TextView) v.findViewById(R.id.endTimeText);
        nameText = (EditText) v.findViewById(R.id.nameText);
        cityText = (AutoCompleteTextView) v.findViewById(R.id.cityText);
        addressText = (EditText) v.findViewById(R.id.addressText);
        postalCodeText = (EditText) v.findViewById(R.id.postalCodeText);
        webText = (EditText) v.findViewById(R.id.webText);
        addressContainer = (LinearLayout) v.findViewById(R.id.addressContainer);
        menu_title = (TextView) v.findViewById(R.id.menu_title);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.popup_item, appRes.getCityNames());
        cityText.setThreshold(1);
        cityText.setAdapter(adapter);

        // UPDATE fragment in onResume call

        return v;
    }

    private void resetFields() {
        setDefaultDateTime();
        nameText.setText("");
        cityText.setText("");
        addressText.setText("");
        postalCodeText.setText("");
        webText.setText("");
        menu_title.setText("");
    }

    private void setDefaultDateTime() {
        startAfterMidnight = null;
        endAfterMidnight = null;
        startHour = 11;
        startMinute = 0;
        endHour = 14;
        endMinute = 0;
        startTimeText.setText(getString(R.string.edit_restaurant_start));
        endTimeText.setText(getString(R.string.edit_restaurant_end));
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }
}
