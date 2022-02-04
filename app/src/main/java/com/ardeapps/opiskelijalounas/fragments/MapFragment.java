package com.ardeapps.opiskelijalounas.fragments;


import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.Logger;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.StringUtil;
import com.ardeapps.opiskelijalounas.handlers.RestaurantHolderListener;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    RestaurantHolderListener mListener = null;

    public void setListener(RestaurantHolderListener l) {
        mListener = l;
    }

    MapView mMapView;
    GoogleMap googleMap;
    AutoCompleteTextView searchText;
    View restaurantDetails;
    TextView nameText;
    TextView distanceText;
    TextView cityText;
    TextView timeToOpenText;
    TextView addLunchLink;
    TextView lunchInfo;
    ImageView starIcon;
    ImageView editIcon;
    LinearLayout mealsContainer;
    LinearLayout distanceContent;
    View openIcon;

    AppRes appRes = (AppRes) AppRes.getContext();
    List<Restaurant> restaurants;
    List<String> cityNames;
    ArrayList<String> searchHints;
    Map<String, Marker> markers = new HashMap<>();
    Marker lastMarker;
    Restaurant lastRestaurant;

    public void update() {
    }

    public void refreshData() {
        restaurants = appRes.getRestaurants();
        cityNames = appRes.getCityNames();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        searchText = (AutoCompleteTextView) v.findViewById(R.id.searchText);
        restaurantDetails = v.findViewById(R.id.restaurantDetails);
        nameText = (TextView) v.findViewById(R.id.nameText);
        distanceText = (TextView) v.findViewById(R.id.distanceText);
        cityText = (TextView) v.findViewById(R.id.cityText);
        timeToOpenText = (TextView) v.findViewById(R.id.timeToOpenText);
        addLunchLink = (TextView) v.findViewById(R.id.addLunchLink);
        lunchInfo = (TextView) v.findViewById(R.id.lunchInfo);
        starIcon = (ImageView) v.findViewById(R.id.starIcon);
        editIcon = (ImageView) v.findViewById(R.id.editIcon);
        mealsContainer = (LinearLayout) v.findViewById(R.id.mealsContainer);
        distanceContent = (LinearLayout) v.findViewById(R.id.distanceContent);
        openIcon = v.findViewById(R.id.openIcon);

        ArrayList<String> displayHints = new ArrayList<>();
        searchHints = new ArrayList<>();
        for(Restaurant restaurant : restaurants) {
            displayHints.add(restaurant.name);
            searchHints.add(restaurant.name.toLowerCase());
        }
        for(String cityName : cityNames) {
            displayHints.add(cityName);
            searchHints.add(cityName.toLowerCase());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.popup_item, displayHints);
        searchText.setAdapter(adapter);

        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(AppRes.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.clear();
                restaurantDetails.setTranslationY(0 - restaurantDetails.getHeight());

                // For showing a move to my location button
                final boolean locationPermissionGranted = ContextCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (locationPermissionGranted) {
                    googleMap.setMyLocationEnabled(true);
                }

                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        appRes.setMapLocationPref(googleMap.getCameraPosition());
                        appRes.setVisibleRestaurants(getVisibleRestaurants());
                    }
                });
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        hideRestaurantDetails();
                    }
                });
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        for(Restaurant restaurant : restaurants) {
                            if(restaurant.name.toLowerCase().equals(marker.getTitle().toLowerCase())) {
                                if(lastRestaurant == null || !lastRestaurant.name.equals(restaurant.name))
                                    showRestaurantDetails(marker, restaurant);
                                break;
                            }
                        }
                        return false;
                    }
                });

                for(Restaurant restaurant : restaurants) {
                    LatLng position = new LatLng(restaurant.latitude, restaurant.longitude);
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(position).title(restaurant.name));
                    setMarkerIcon(marker, restaurant);
                    markers.put(restaurant.name.toLowerCase(), marker);
                }

                Location location = appRes.getLocationPref();
                if(location != null) {
                    moveCamera(location.getLatitude(), location.getLongitude(), false);
                } else if(appRes.getMapLocationPref() != null){
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(appRes.getMapLocationPref()));
                } else {
                    // Suomen sijainti
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(61.92411, 25.748151)).zoom(5).build();
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

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

        return v;
    }

    private void hideRestaurantDetails() {
        restaurantDetails.animate().translationY(0 - restaurantDetails.getHeight());
        if(lastMarker != null && lastRestaurant != null)
            setMarkerIcon(lastMarker, lastRestaurant);

        lastMarker = null;
        lastRestaurant = null;
    }

    private void showRestaurantDetails(Marker selectedMarker, final Restaurant restaurant) {
        restaurantDetails.animate().translationY(0);
        googleMap.setPadding(0, restaurantDetails.getHeight(), 0, 0);

        setMarkerIcon(selectedMarker, null);
        if(lastMarker != null && lastRestaurant != null)
            setMarkerIcon(lastMarker, lastRestaurant);
        lastMarker = selectedMarker;
        lastRestaurant = restaurant;

        nameText.setText(restaurant.name);
        cityText.setText(restaurant.city);
        distanceText.setText(StringUtil.getDistanceText(restaurant.latitude, restaurant.longitude));
        distanceContent.setVisibility(appRes.getLocationPref() != null ? View.VISIBLE : View.GONE);

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

        final boolean isFavourite = appRes.getFavouriteRestaurantIdsPref().contains(restaurant.restaurantId);
        boolean lunchAdded = !StringUtil.isEmptyString(RestaurantIdPairs.getIdForUrl(restaurant.restaurantId)) || !StringUtil.isEmptyString(restaurant.webUrl);
        lunchInfo.setVisibility(lunchAdded ? View.VISIBLE : View.GONE);
        addLunchLink.setVisibility(!lunchAdded ? View.VISIBLE : View.GONE);

        starIcon.setImageResource(isFavourite ? R.drawable.star_icon_full : R.drawable.star_icon_empty);
        starIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onStarClicked(restaurant, !appRes.getFavouriteRestaurantIdsPref().contains(restaurant.restaurantId));
                // Tässä välissä ravintola lisätään suosikkeihin
                starIcon.setImageResource(appRes.getFavouriteRestaurantIdsPref().contains(restaurant.restaurantId) ? R.drawable.star_icon_full : R.drawable.star_icon_empty);
            }
        });

        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onEditClicked(restaurant);
            }
        });

        addLunchLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddLunchClicked(restaurant);
            }
        });
        restaurantDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onShowLunchClicked(restaurant);
            }
        });
    }

    private void performSearch() {
        String keyword = searchText.getText().toString().toLowerCase();
        if(searchHints.contains(keyword)) {
            AppRes.hideKeyBoard(searchText);
            Marker marker = markers.get(keyword);
            if(marker != null) {
                for(Restaurant restaurant : restaurants) {
                    if(restaurant.name.toLowerCase().equals(keyword)) {
                        showRestaurantDetails(marker, restaurant);
                        break;
                    }
                }
                moveCamera(marker.getPosition().latitude, marker.getPosition().longitude, true);
            } else {
                LatLng cityPosition = getLocationFromAddress(keyword);
                if(cityPosition != null) {
                    moveCamera(cityPosition.latitude, cityPosition.longitude, true);
                } else {
                    Logger.toast(R.string.map_no_results);
                }
            }
        } else {
            for(Restaurant restaurant : restaurants) {
                if(restaurant.name.toLowerCase().contains(keyword)) {
                    searchText.setText(restaurant.name);
                    searchText.setSelection(searchText.getText().length());
                    searchText.dismissDropDown();
                    Marker marker = markers.get(restaurant.name.toLowerCase());
                    showRestaurantDetails(marker, restaurant);
                    moveCamera(marker.getPosition().latitude, marker.getPosition().longitude, true);
                    return;
                }
            }
            for(String cityName : cityNames) {
                if(cityName.contains(keyword)) {
                    searchText.setText(cityName);
                    searchText.setSelection(searchText.getText().length());
                    searchText.dismissDropDown();
                    LatLng cityPosition = getLocationFromAddress(cityName);
                    if(cityPosition != null) {
                        moveCamera(cityPosition.latitude, cityPosition.longitude, true);
                    }
                    return;
                }
            }
            Logger.toast(R.string.map_no_results);
        }
    }

    public ArrayList<Restaurant> getVisibleRestaurants() {
        ArrayList<Restaurant> visibleRestaurants = new ArrayList<>();
        for (Map.Entry<String, Marker> entry : markers.entrySet()) {
            Marker marker = entry.getValue();
            if(isVisibleMarker(marker)) {
                for(Restaurant restaurant : restaurants) {
                    if(restaurant.name.toLowerCase().equals(entry.getKey())) {
                        visibleRestaurants.add(restaurant);
                        break;
                    }
                }
            }
        }
        return visibleRestaurants;
    }

    private boolean isVisibleMarker(Marker marker) {
        if (googleMap != null) {
            LatLngBounds latLongBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
            return latLongBounds.contains(marker.getPosition());
        }
        return false;
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

    private void moveCamera(double latitude, double longitude, boolean moveSmoothly) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(15).build();
        if(moveSmoothly) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void setMarkerIcon(Marker marker, Restaurant restaurant) {
        float colorFloat = 0; // RED
        if(restaurant == null) {
            colorFloat = 60; // YELLOW
        } else {
            if(restaurant.lunchStartTimeAfterMidnight != null && restaurant.lunchEndTimeAfterMidnight != null) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                long timeAfterMidnight = System.currentTimeMillis() - cal.getTimeInMillis();
                if(timeAfterMidnight > restaurant.lunchStartTimeAfterMidnight && timeAfterMidnight < restaurant.lunchEndTimeAfterMidnight) {
                    colorFloat = 120; // GREEN
                }
            }
        }
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(colorFloat));
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
