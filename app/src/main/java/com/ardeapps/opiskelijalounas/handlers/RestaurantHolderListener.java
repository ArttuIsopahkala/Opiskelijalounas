package com.ardeapps.opiskelijalounas.handlers;

import com.ardeapps.opiskelijalounas.objects.Restaurant;

/**
 * Created by Arttu on 2.11.2017.
 */

public interface RestaurantHolderListener {
    void onStarClicked(Restaurant restaurant, boolean addFavourite);
    void onEditClicked(Restaurant restaurant);
    void onShowLunchClicked(Restaurant restaurant);
    void onAddLunchClicked(Restaurant restaurant);
    void onAdminRemoveRestaurantClicked(String restaurantId);
}
