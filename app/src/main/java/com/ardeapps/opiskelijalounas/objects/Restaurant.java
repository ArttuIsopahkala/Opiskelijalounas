package com.ardeapps.opiskelijalounas.objects;

import java.util.Map;

/**
 * Created by Arttu on 28.9.2017.
 */

public class Restaurant {

    final public static String SODEXO = "sodexo";
    final public static String AMICA = "amica";
    final public static String UNICAFE = "unicafe";
    final public static String SONAATTI = "sonaatti";
    final public static String ARKEA = "arkea";

    public String restaurantId;
    public Double latitude;
    public Double longitude;
    public String name;
    public String address;
    public String postalCode;
    public String companyUrl;
    public String city;
    public String webUrl;
    public Long lunchStartTimeAfterMidnight;
    public Long lunchEndTimeAfterMidnight;

    public Restaurant clone() {
        Restaurant clone = new Restaurant();
        clone.restaurantId = this.restaurantId;
        clone.latitude = this.latitude;
        clone.longitude = this.longitude;
        clone.name = this.name;
        clone.address = this.address;
        clone.postalCode = this.postalCode;
        clone.city = this.city;
        clone.webUrl = this.webUrl;
        clone.lunchStartTimeAfterMidnight = this.lunchStartTimeAfterMidnight;
        clone.lunchEndTimeAfterMidnight = this.lunchEndTimeAfterMidnight;
        return clone;
    }

    /*public boolean isSame(Restaurant restaurant) {
        if(this.restaurantId.equals(restaurant.restaurantId)
                && this.name.equals(restaurant.name)
                && this.address.equals(restaurant.address)
                && this.postalCode.equals(restaurant.postalCode)
                && this.city.equals(restaurant.city)
                && this.webUrl.equals(restaurant.webUrl)
                && this.lunchStartTimeAfterMidnight == restaurant.lunchStartTimeAfterMidnight
                && this.lunchEndTimeAfterMidnight == restaurant.lunchEndTimeAfterMidnight
                && this.latitude == restaurant.latitude
                && this.longitude == restaurant.longitude) {
            return true;
        } else {
            return false;
        }
    }*/
}
