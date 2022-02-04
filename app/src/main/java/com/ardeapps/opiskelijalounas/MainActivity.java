package com.ardeapps.opiskelijalounas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.ardeapps.opiskelijalounas.adapters.RestaurantAdapter;
import com.ardeapps.opiskelijalounas.adapters.ViewPagerAdapter;
import com.ardeapps.opiskelijalounas.fragments.AddRestaurantFragment;
import com.ardeapps.opiskelijalounas.fragments.FavouritesFragment;
import com.ardeapps.opiskelijalounas.fragments.InfoDialogFragment;
import com.ardeapps.opiskelijalounas.fragments.InfoFragment;
import com.ardeapps.opiskelijalounas.fragments.LoaderFragment;
import com.ardeapps.opiskelijalounas.fragments.MapFragment;
import com.ardeapps.opiskelijalounas.fragments.RestaurantFragment;
import com.ardeapps.opiskelijalounas.fragments.RestaurantsFragment;
import com.ardeapps.opiskelijalounas.fragments.SendFeedbackDialogFragment;
import com.ardeapps.opiskelijalounas.handlers.GetRestaurantsHandler;
import com.ardeapps.opiskelijalounas.handlers.GetWeeklyMenuHandler;
import com.ardeapps.opiskelijalounas.handlers.RestaurantHolderListener;
import com.ardeapps.opiskelijalounas.objects.Meal;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.ardeapps.opiskelijalounas.services.FirebaseService;
import com.ardeapps.opiskelijalounas.services.FragmentListeners;
import com.ardeapps.opiskelijalounas.services.LunchService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements RestaurantHolderListener {

    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    FrameLayout container;
    AdView mAdView;

    LoaderFragment loaderFragment;
    MapFragment mapFragment;
    RestaurantsFragment restaurantsFragment;
    FavouritesFragment favouritesFragment;
    InfoFragment infoFragment;
    AddRestaurantFragment addRestaurantFragment;
    RestaurantFragment restaurantFragment;

    AppRes appRes;
    SharedPreferences appPref;
    SharedPreferences userPref;
    SharedPreferences.Editor editor;
    boolean appStartedFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        container = (FrameLayout) findViewById(R.id.fragment_container);
        mAdView = (AdView) findViewById(R.id.adView);

        // Create fragments
        loaderFragment = new LoaderFragment();
        mapFragment = new MapFragment();
        restaurantsFragment = new RestaurantsFragment();
        favouritesFragment = new FavouritesFragment();
        infoFragment = new InfoFragment();
        addRestaurantFragment = new AddRestaurantFragment();
        restaurantFragment = new RestaurantFragment();

        setListeners();

        // Initialize local variables
        userPref = getSharedPreferences("user", 0);
        appRes = (AppRes) getApplicationContext();
        appPref = getSharedPreferences("app", 0);
        appStartedFirstTime = appPref.getBoolean("appStartedFirstTime", true);

        if(appStartedFirstTime) {
            Intent shortcutIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent intent = new Intent();

            // Create Implicit intent and assign Shortcut Application Name, Icon
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(
                            getApplicationContext(), R.mipmap.ic_launcher));
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(intent);

            editor = appPref.edit();
            editor.putBoolean("appStartedFirstTime", false);
            editor.apply();
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, loaderFragment)
                .commitAllowingStateLoss();
        container.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            FragmentListeners.getInstance().getPermissionHandledListener().onPermissionGranted(requestCode);
        } else {
            FragmentListeners.getInstance().getPermissionHandledListener().onPermissionDenied(requestCode);
        }
    }

    @Override
    public void onBackPressed() {
        AppRes.hideKeyBoard(getWindow().getDecorView());

        //go to previous fragment or close app if user is on mainscreen
        int backStack = getSupportFragmentManager().getBackStackEntryCount();

        if (backStack > 0) {
            if(backStack == 1) {
                container.setVisibility(View.INVISIBLE);
            }
            getSupportFragmentManager().popBackStack();
        } else super.onBackPressed();
    }

    public void onBackIconPressed(View view) {
        onBackPressed();
    }

    /** SET FRAGMENT LISTENERS */
    private void setListeners() {
        RestaurantAdapter.setListener(this);
        mapFragment.setListener(this);

        loaderFragment.setListener(new LoaderFragment.Listener() {
            @Override
            public void onMainDataLoaded() {
                openMainApp();
            }
        });
        addRestaurantFragment.setListener(new AddRestaurantFragment.AddRestaurantListener() {
            @Override
            public void onRestaurantChanged(Restaurant restaurant) {
                FirebaseService.getInstance().sendRestaurantRequest(restaurant);
                onBackPressed();
                InfoDialogFragment info = InfoDialogFragment.newInstance(getString(R.string.edit_restaurant_submit_title), getString(R.string.edit_restaurant_submit_desc));
                info.show(getSupportFragmentManager(), "Kiitos avusta!");
            }
            @Override
            public void onRestaurantAdminChanged(final Restaurant restaurant) {
                FirebaseService.getInstance().setRestaurant(restaurant);
                FirebaseService.getInstance().getRestaurants(new GetRestaurantsHandler() {
                    @Override
                    public void onGetRestaurantsSuccess(ArrayList<Restaurant> restaurants) {
                        appRes.setRestaurants(restaurants);
                        onBackPressed();
                        String title = "";
                        if(restaurant.latitude == null || restaurant.longitude == null) {
                            title += "HUOM: LAT LNG ei löytynyt! ";
                        }
                        title += "Vaihdoit juuri tietokantaan ravintolaa: " + restaurant.name + " ID: " + restaurant.restaurantId;
                        InfoDialogFragment info = InfoDialogFragment.newInstance("Olet Admin", title);
                        info.show(getSupportFragmentManager(), "Kiitos avusta ADMIN!");
                        removeRestaurantRequest(restaurant.restaurantId);

                        pagerAdapter.updateRestaurantsFragment();
                    }
                });
            }
        });
        SendFeedbackDialogFragment.setListener(new SendFeedbackDialogFragment.Listener() {
            @Override
            public void onFeedbackSent(String message) {
                FirebaseService.getInstance().sendFeedback(message);
                Logger.toast(R.string.info_feedback_sent);
            }
        });
        infoFragment.setListener(new InfoFragment.Listener() {
            @Override
            public void addRestaurantClick() {
                addRestaurantFragment.setRestaurant(null, false);
                switchToFragment(addRestaurantFragment);
            }
            @Override
            public void onAdminRestaurantSelected(Restaurant restaurant) {
                Restaurant restaurantToSave = new Restaurant();
                for(Restaurant existRestaurant : appRes.getRestaurants()) {
                    // Yhdistetään vanhat tiedot keskenään, jos on
                    restaurantToSave = existRestaurant.clone();
                    break;
                }
                // Lisätään tiedot joita käyttäjä on muokannut
                if(!StringUtil.isEmptyString(restaurant.restaurantId))
                    restaurantToSave.restaurantId = restaurant.restaurantId;
                if(!StringUtil.isEmptyString(restaurant.name))
                    restaurantToSave.name = restaurant.name;
                if(restaurant.latitude != null)
                    restaurantToSave.latitude = restaurant.latitude;
                if(restaurant.longitude != null)
                    restaurantToSave.longitude = restaurant.longitude;
                if(!StringUtil.isEmptyString(restaurant.address))
                    restaurantToSave.address = restaurant.address;
                if(!StringUtil.isEmptyString(restaurant.postalCode))
                    restaurantToSave.postalCode = restaurant.postalCode;
                if(!StringUtil.isEmptyString(restaurant.city))
                    restaurantToSave.city = restaurant.city;
                if(!StringUtil.isEmptyString(restaurant.webUrl))
                    restaurantToSave.webUrl = restaurant.webUrl;
                if(restaurant.lunchStartTimeAfterMidnight != null)
                    restaurantToSave.lunchStartTimeAfterMidnight = restaurant.lunchStartTimeAfterMidnight;
                if(restaurant.lunchEndTimeAfterMidnight != null)
                    restaurantToSave.lunchEndTimeAfterMidnight = restaurant.lunchEndTimeAfterMidnight;

                addRestaurantFragment.setRestaurant(restaurantToSave, false);
                switchToFragment(addRestaurantFragment);
            }
            @Override
            public void onAdminFeedbackSelected(String feedback) {
                InfoDialogFragment info = InfoDialogFragment.newInstance("", feedback);
                info.show(getSupportFragmentManager(), "Palaute");
            }
            @Override
            public void onAdminRemoveRestaurantRequest(String restaurantId) {
                removeRestaurantRequest(restaurantId);
            }
            @Override
            public void onAdminRemoveFeedback(String key) {
                FirebaseService.getInstance().removeFeedback(key);
                appRes.getFeedbacks().remove(key);
                pagerAdapter.updateInfoFragment();
            }
        });
        restaurantFragment.setListener(new RestaurantFragment.Listener() {
            @Override
            public void addLunchClick(Restaurant restaurant) {
                addRestaurantFragment.setRestaurant(restaurant, true);
                switchToFragment(addRestaurantFragment);
            }
        });
    }

    private void removeRestaurantRequest(String restaurantId) {
        FirebaseService.getInstance().removeRestaurantRequest(restaurantId);
        Iterator<Restaurant> itr = appRes.getRestaurantRequests().iterator();
        while(itr.hasNext())  {
            Restaurant restaurant = itr.next();
            if(restaurantId.equals(restaurant.restaurantId)) {
                itr.remove();
                break;
            }
        }
        pagerAdapter.updateInfoFragment();
    }

    @Override
    public void onAddLunchClicked(Restaurant restaurant) {
        addRestaurantFragment.setRestaurant(restaurant, true);
        switchToFragment(addRestaurantFragment);
    }

    @Override
    public void onShowLunchClicked(final Restaurant restaurant) {
        restaurantFragment.setRestaurant(restaurant);
        Map<String, ArrayList<Meal>> meals = appRes.getWeeklyMenus().get(restaurant.restaurantId);
        if(meals == null) {
            final ProgressDialog progress = new ProgressDialog(this, R.style.LoadingDialog);
            progress.setMessage(getString(R.string.loading));
            progress.setCancelable(false);
            progress.show();
            LunchService.getInstance().getWeeklyMenu(restaurant.restaurantId, new GetWeeklyMenuHandler() {
                @Override
                public void onGetWeeklyMealsSuccess(Map<String, ArrayList<Meal>> meals) {
                    progress.dismiss();
                    Map<String, Map<String, ArrayList<Meal>>> weeklyMenus = appRes.getWeeklyMenus();
                    weeklyMenus.put(restaurant.restaurantId, meals);
                    appRes.setWeeklyMenus(weeklyMenus);
                    restaurantFragment.setWeeklyMeals(meals);
                    switchToFragment(restaurantFragment);
                }
            });
        } else {
            restaurantFragment.setWeeklyMeals(meals);
            switchToFragment(restaurantFragment);
        }
    }

    @Override
    public void onStarClicked(final Restaurant restaurant, boolean addFavourite) {
        if(addFavourite) {
            final ProgressDialog progress = new ProgressDialog(this, R.style.LoadingDialog);
            progress.setMessage(getString(R.string.loading));
            progress.setCancelable(false);
            progress.show();
            LunchService.getInstance().getWeeklyMenu(restaurant.restaurantId, new GetWeeklyMenuHandler() {
                @Override
                public void onGetWeeklyMealsSuccess(Map<String, ArrayList<Meal>> meals) {
                    progress.dismiss();
                    Map<String, Map<String, ArrayList<Meal>>> weeklyMenus = appRes.getWeeklyMenus();
                    weeklyMenus.put(restaurant.restaurantId, meals);
                    appRes.setWeeklyMenus(weeklyMenus);

                    ArrayList<String> favouriteRestaurantIds = appRes.getFavouriteRestaurantIdsPref();
                    favouriteRestaurantIds.add(restaurant.restaurantId);
                    appRes.setFavouriteRestaurantIdsPref(favouriteRestaurantIds);
                    pagerAdapter.updateFavouritesFragment();
                    pagerAdapter.updateRestaurantsFragment();
                    pagerAdapter.updateMapFragment();
                }
            });
        } else {
            ArrayList<String> favouriteRestaurantIds = appRes.getFavouriteRestaurantIdsPref();
            Iterator<String> itr = favouriteRestaurantIds.iterator();
            while(itr.hasNext())  {
                String restaurantId = itr.next();
                if(restaurantId.equals(restaurant.restaurantId)) {
                    itr.remove();
                    break;
                }
            }
            appRes.setFavouriteRestaurantIdsPref(favouriteRestaurantIds);
            pagerAdapter.updateFavouritesFragment();
            pagerAdapter.updateRestaurantsFragment();
            pagerAdapter.updateMapFragment();
        }
    }
    @Override
    public void onEditClicked(Restaurant restaurant) {
        addRestaurantFragment.setRestaurant(restaurant, false);
        switchToFragment(addRestaurantFragment);
    }

    @Override
    public void onAdminRemoveRestaurantClicked(String restaurantId) {
        FirebaseService.getInstance().removeRestaurant(restaurantId);
        Iterator<Restaurant> itr = appRes.getRestaurants().iterator();
        while(itr.hasNext())  {
            Restaurant restaurant = itr.next();
            if(restaurantId.equals(restaurant.restaurantId)) {
                InfoDialogFragment info = InfoDialogFragment.newInstance("Olet Admin", "Poistit ravintolan: " + restaurant.name + " ID: " + restaurant.restaurantId);
                info.show(getSupportFragmentManager(), "Poistit ravintolan ADMIN!");
                itr.remove();
                break;
            }
        }
        pagerAdapter.updateRestaurantsFragment();
    }

    public void openMainApp() {
        // Remove loader fragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
        container.setVisibility(View.INVISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
        if(!appRes.getIsAdmin()) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setVisibility(View.VISIBLE);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        mapFragment.refreshData();
        restaurantsFragment.refreshData();
        favouritesFragment.refreshData();
        infoFragment.refreshData();
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(mapFragment);
        fragments.add(restaurantsFragment);
        fragments.add(favouritesFragment);
        fragments.add(infoFragment);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        setTabIcons();
    }

    private void switchToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        container.setVisibility(View.VISIBLE);
    }

    private void setTabIcons() {
        setTabIcon(tabLayout.getTabAt(0), R.drawable.marker_icon, null);
        setTabIcon(tabLayout.getTabAt(1), R.drawable.list_icon, null);
        setTabIcon(tabLayout.getTabAt(2), R.drawable.star_icon, null);
        setTabIcon(tabLayout.getTabAt(3), R.drawable.info_icon, null);

        final int colorUnSelected = ContextCompat.getColor(MainActivity.this, R.color.color_text_light);
        final int colorSelected = ContextCompat.getColor(MainActivity.this, R.color.color_colored);

        boolean hasFavourites = appRes.getFavouriteRestaurantIdsPref().size() > 0;
        viewPager.setCurrentItem(hasFavourites ? 2 : 1);
        setTabIcon(tabLayout.getTabAt(0), null, colorUnSelected);
        setTabIcon(tabLayout.getTabAt(1), null, hasFavourites ? colorUnSelected : colorSelected);
        setTabIcon(tabLayout.getTabAt(2), null, hasFavourites ? colorSelected : colorUnSelected);
        setTabIcon(tabLayout.getTabAt(3), null, colorUnSelected);

        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        setTabIcon(tab, null, colorSelected);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        setTabIcon(tab, null, colorUnSelected);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
    }

    private void setTabIcon(TabLayout.Tab tab, Integer icon, Integer color) {
        if(icon != null && color == null) {
            tab.setIcon(icon);
            return;
        }

        Drawable tabIcon = tab.getIcon();
        if(tabIcon != null)
            tabIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
