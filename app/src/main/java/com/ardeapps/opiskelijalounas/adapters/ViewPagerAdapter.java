package com.ardeapps.opiskelijalounas.adapters;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.ardeapps.opiskelijalounas.fragments.FavouritesFragment;
import com.ardeapps.opiskelijalounas.fragments.InfoFragment;
import com.ardeapps.opiskelijalounas.fragments.MapFragment;
import com.ardeapps.opiskelijalounas.fragments.RestaurantsFragment;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    @Override
    public Parcelable saveState() {
        return null;
    }

    private MapFragment mapFragment;
    private RestaurantsFragment restaurantsFragment;
    private FavouritesFragment favouritesFragment;
    private InfoFragment infoFragment;

    private ArrayList<Fragment> fragments;

    public ViewPagerAdapter(FragmentManager supportFragmentManager, ArrayList<Fragment> fragments) {
        super(supportFragmentManager);
        this.fragments = fragments;
        mapFragment = (MapFragment)fragments.get(0);
        restaurantsFragment = (RestaurantsFragment)fragments.get(1);
        favouritesFragment = (FavouritesFragment)fragments.get(2);
        infoFragment = (InfoFragment)fragments.get(3);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return mapFragment;
            case 1:
                return restaurantsFragment;
            case 2:
                return favouritesFragment;
            case 3:
                return infoFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        //don't return POSITION_NONE, avoid fragment recreation.
        return super.getItemPosition(object);
    }

    public void updateMapFragment() {
        mapFragment.refreshData();
        mapFragment.update();
    }

    public void updateRestaurantsFragment() {
        restaurantsFragment.refreshData();
        restaurantsFragment.update();
    }

    public void updateFavouritesFragment() {
        favouritesFragment.refreshData();
        favouritesFragment.update();
    }

    public void updateInfoFragment() {
        infoFragment.refreshData();
        infoFragment.update();
    }
}
