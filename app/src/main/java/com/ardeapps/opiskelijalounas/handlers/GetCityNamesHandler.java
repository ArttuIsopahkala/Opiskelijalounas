package com.ardeapps.opiskelijalounas.handlers;

import com.ardeapps.opiskelijalounas.objects.Restaurant;

import java.util.ArrayList;

/**
 * Created by Arttu on 2.10.2017.
 */

public interface GetCityNamesHandler {
    void onGetCityNamesSuccess(ArrayList<String> cityNames);
}
