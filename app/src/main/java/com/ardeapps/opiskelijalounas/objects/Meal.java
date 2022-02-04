package com.ardeapps.opiskelijalounas.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arttu on 18.10.2017.
 */

public class Meal {
    public List<Lunch> lunches;
    public String price;
    public String description;

    public Meal() {
        lunches = new ArrayList<>();
    }
}
