package com.ardeapps.opiskelijalounas.services;

import android.util.Log;

import com.ardeapps.opiskelijalounas.Logger;
import com.ardeapps.opiskelijalounas.StringUtil;
import com.ardeapps.opiskelijalounas.handlers.GetWeeklyMenuHandler;
import com.ardeapps.opiskelijalounas.handlers.GetWeeklyMenusHandler;
import com.ardeapps.opiskelijalounas.objects.Lunch;
import com.ardeapps.opiskelijalounas.objects.Meal;
import com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.ardeapps.opiskelijalounas.R.id.dietText;
import static com.ardeapps.opiskelijalounas.objects.Restaurant.AMICA;
import static com.ardeapps.opiskelijalounas.objects.Restaurant.SODEXO;
import static com.ardeapps.opiskelijalounas.objects.Restaurant.SONAATTI;
import static com.ardeapps.opiskelijalounas.objects.RestaurantIdPairs.getIdForUrl;

/**
 * Created by Arttu on 18.6.2017.
 */

public class LunchService {

    private String TAG = "LunchService";

    final String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
    final List<String> diets = new ArrayList<>(Arrays.asList("G", "L", "VL", "M", "S"));

    private static LunchService instance;
    private Lunch lunch;

    public static LunchService getInstance() {
        if (instance == null) {
            instance = new LunchService();
        }
        return instance;
    }

    private String getNode(JSONObject object, String node) {
        String value = "";
        try {
            if (!object.isNull(node))
                value = decode(object.getString(node).trim());
        } catch (JSONException e) {
            Log.e(TAG, "getNodeError - " + node + " not found from " + object.toString());
        }
        return value;
    }

    private ArrayList<String> getArrayNode(JSONObject object, String node) {
        ArrayList<String> objects = new ArrayList<>();
        try {
            if (!object.isNull(node)) {
                JSONArray arrJson = object.getJSONArray(node);
                int length = arrJson.length();
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        objects.add(arrJson.getString(i));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "getNodeError - " + node + " not found from " + object.toString());
        }
        return objects;
    }

    private JSONObject convertToJSONObject(String json) {
        JSONObject obj;
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            obj = new JSONObject();
            Log.e(TAG, "convertToJSONObject " + json);
        }
        return obj;
    }

    private JSONObject getJSONObject(JSONArray objects, int index) {
        JSONObject obj = new JSONObject();
        try {
            obj = objects.getJSONObject(index);
        } catch (JSONException e) {
            Log.e(TAG, "getJSONObjectError - index " + index + " not found from " + objects.toString());
        }
        return obj;
    }

    private JSONObject getJSONObject(JSONObject object, String node) {
        JSONObject obj = new JSONObject();
        try {
            if (!object.isNull(node))
                obj = object.getJSONObject(node);
        } catch (JSONException e) {
            Log.e(TAG, "getJSONObjectError - " + node + " not found from " + object.toString());
        }
        return obj;
    }

    private JSONArray getJSONArray(JSONObject object, String node) {
        JSONArray arr = new JSONArray();
        try {
            if (!object.isNull(node))
                arr = object.getJSONArray(node);
        } catch (JSONException e) {
            Log.e(TAG, "getJSONArrayError - " + node + " not found from " + object.toString());
        }
        return arr;
    }

    public void getWeeklyMenus(final List<String> restaurantIds, final GetWeeklyMenusHandler handler) {
        // Map: restaurantId, weekDay->meals
        final Map<String, Map<String, ArrayList<Meal>>> weeklyMenus = new HashMap<>();
        final Map<String, ArrayList<Meal>> todayMenus = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        int formattedDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        final int dayOfWeek = formattedDay == 0 ? 6 : formattedDay - 1;

        if (restaurantIds.size() > 0) {
            for (final String restaurantId : restaurantIds) {
                getWeeklyMenu(restaurantId, new GetWeeklyMenuHandler() {
                    @Override
                    public void onGetWeeklyMealsSuccess(Map<String, ArrayList<Meal>> meals) {
                        weeklyMenus.put(restaurantId, meals);

                        todayMenus.put(restaurantId, meals.get(weekDays[dayOfWeek]));

                        if (weeklyMenus.size() == restaurantIds.size()) {
                            handler.onGetWeeklyMenusSuccess(weeklyMenus, todayMenus);
                        }
                    }
                });
            }
        } else {
            handler.onGetWeeklyMenusSuccess(weeklyMenus, todayMenus);
        }
    }

    public void getWeeklyMenu(String restaurantId, final GetWeeklyMenuHandler handler) {
        String url;
        if (RestaurantIdPairs.getSodexoRestaurantIds().contains(restaurantId)) {
            url = "https://www.sodexo.fi/ruokalistat/output/weekly_json/" + getIdForUrl(restaurantId) + "/" + StringUtil.getDateForUrl(SODEXO) + "/fi";
            new GetDocumentFromUrlTask(new GetDocumentFromUrlTask.GetDocumentHandler() {
                @Override
                public void onDocumentLoaded(String result) {
                    Map<String, ArrayList<Meal>> meals = new HashMap<>();
                    if (result != null) {
                        JSONObject json = convertToJSONObject(result);
                        JSONObject menu = getJSONObject(json, "menus");
                        for (int i = 0; i < weekDays.length; i++) {
                            meals.put(weekDays[i], getSodexoMeals(menu, weekDays[i]));
                        }
                    }
                    handler.onGetWeeklyMealsSuccess(meals);
                }
            }).execute(url);
        } else if (RestaurantIdPairs.getAmicaRestaurantIds().contains(restaurantId)) {
            url = "http://www.amica.fi/modules/json/json/Index?costNumber=" + getIdForUrl(restaurantId) + "&language=fi&firstDay=" + StringUtil.getDateForUrl(AMICA);
            new GetDocumentFromUrlTask(new GetDocumentFromUrlTask.GetDocumentHandler() {
                @Override
                public void onDocumentLoaded(String result) {
                    Map<String, ArrayList<Meal>> meals = new HashMap<>();
                    if (result != null) {
                        JSONObject json = convertToJSONObject(result);
                        JSONArray days = getJSONArray(json, "MenusForDays");
                        for (int i = 0; i < days.length(); i++) {
                            JSONObject day = getJSONObject(days, i);
                            String[] dateArray = getNode(day, "Date").split("T")[0].split("-");
                            Calendar c = Calendar.getInstance();
                            c.set(Integer.valueOf(dateArray[0]), (Integer.valueOf(dateArray[1]) - 1), Integer.valueOf(dateArray[2]));

                            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
                            dayOfWeek = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
                            JSONArray menus = getJSONArray(day, "SetMenus");
                            meals.put(weekDays[dayOfWeek], getSonaattiOrAmicaMeals(menus));
                        }
                    }
                    handler.onGetWeeklyMealsSuccess(meals);
                }
            }).execute(url);
        } else if (RestaurantIdPairs.getUnicafeRestaurantIds().contains(restaurantId)) {
            url = "http://messi.hyyravintolat.fi/rss/fin/" + getIdForUrl(restaurantId);
            new GetDocumentFromUrlTask(new GetDocumentFromUrlTask.GetDocumentHandler() {
                @Override
                public void onDocumentLoaded(String result) {
                    Map<String, ArrayList<Meal>> meals = new HashMap<>();
                    if (result != null) {
                        try {
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(new InputSource(new StringReader(result)));
                            doc.getDocumentElement().normalize();

                            NodeList nodeList = doc.getElementsByTagName("item");
                            for (int i = 0; i < nodeList.getLength(); i++) {
                                Element element = (Element) nodeList.item(i);
                                NodeList descList = element.getElementsByTagName("description");
                                String description = descList.item(0).getTextContent();

                                meals.put(weekDays[i], getUnicafeMeals(description));
                            }
                        } catch (Exception e) {
                            //Logger.log("XML Pasing Excpetion = " + e);
                        }
                    }
                    handler.onGetWeeklyMealsSuccess(meals);
                }
            }).execute(url);
        } else if (RestaurantIdPairs.getSonaattiRestaurantIds().contains(restaurantId)) {
            url = "http://www.semma.fi/modules/json/json/Index?costNumber=" + getIdForUrl(restaurantId) + "&language=fi&firstDay=" + StringUtil.getDateForUrl(SONAATTI);
            new GetDocumentFromUrlTask(new GetDocumentFromUrlTask.GetDocumentHandler() {
                @Override
                public void onDocumentLoaded(String result) {
                    Map<String, ArrayList<Meal>> meals = new HashMap<>();
                    if (result != null) {
                        JSONObject json = convertToJSONObject(result);
                        JSONArray days = getJSONArray(json, "MenusForDays");
                        for (int i = 0; i < days.length(); i++) {
                            JSONObject day = getJSONObject(days, i);
                            String[] dateArray = getNode(day, "Date").split("T")[0].split("-");
                            Calendar c = Calendar.getInstance();
                            c.set(Integer.valueOf(dateArray[0]), (Integer.valueOf(dateArray[1]) - 1), Integer.valueOf(dateArray[2]));

                            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
                            dayOfWeek = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
                            JSONArray menus = getJSONArray(day, "SetMenus");
                            meals.put(weekDays[dayOfWeek], getSonaattiOrAmicaMeals(menus));
                        }
                    }
                    handler.onGetWeeklyMealsSuccess(meals);
                }
            }).execute(url);
        } else if (RestaurantIdPairs.getArkeaRestaurantIds().contains(restaurantId)) {
            url = "http://www.arkea.fi/fi/ruokalista/" + getIdForUrl(restaurantId) + "/lista";
            new GetDocumentFromUrlTask(new GetDocumentFromUrlTask.GetDocumentHandler() {
                @Override
                public void onDocumentLoaded(String result) {
                    Map<String, ArrayList<Meal>> meals = new HashMap<>();
                    if (result != null) {
                        JSONObject json = convertToJSONObject(result);
                        JSONArray days = getJSONArray(json, "MenusForDays");
                        for (int i = 0; i < days.length(); i++) {
                            JSONObject day = getJSONObject(days, i);
                            String[] dateArray = getNode(day, "Date").split("T")[0].split("-");
                            Calendar c = Calendar.getInstance();
                            c.set(Integer.valueOf(dateArray[0]), (Integer.valueOf(dateArray[1]) - 1), Integer.valueOf(dateArray[2]));

                            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
                            dayOfWeek = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
                            JSONObject menus = getJSONObject(day, "SetMenus");
                            meals.put(weekDays[dayOfWeek], getArkeaMeals(menus));
                        }
                    }
                    handler.onGetWeeklyMealsSuccess(meals);
                }
            }).execute(url);
        } else if (RestaurantIdPairs.getJuvenesRestaurantIds().contains(restaurantId)) {
            String[] urlIds = RestaurantIdPairs.getIdForUrl(restaurantId).split(",");
            url = "http://www.juvenes.fi/tabid/" + urlIds[0] + "/moduleid/" + urlIds[1] + "/RSS.aspx";
            new GetDocumentFromUrlTask(new GetDocumentFromUrlTask.GetDocumentHandler() {
                @Override
                public void onDocumentLoaded(String result) {
                    Map<String, ArrayList<Meal>> meals = new HashMap<>();
                    if (result != null) {
                        try {
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(new InputSource(new StringReader(result)));
                            doc.getDocumentElement().normalize();

                            NodeList nodeList = doc.getElementsByTagName("item");
                            Map<Integer, Element> elementMap = new HashMap<>();
                            for (int i = 0; i < nodeList.getLength(); i++) {
                                Element element = (Element) nodeList.item(i);
                                NodeList titleList = element.getElementsByTagName("title");
                                String title = titleList.item(0).getTextContent();
                                if (title.contains("(FI)")) {
                                    List<String> words = Arrays.asList(title.split(" "));
                                    Date date = getDate(words);
                                    if (date != null && isOnThisWeek(date)) {
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(date);
                                        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
                                        int selectedDay = dayOfWeek == 0 ? 6 : dayOfWeek - 1;
                                        elementMap.put(selectedDay, element);
                                    }
                                }
                            }

                            // Käydään kaikki viikonpäivät läpi
                            for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                                Element element = elementMap.get(dayOfWeek);
                                String html = "";
                                if (element != null) {
                                    NodeList descList = element.getElementsByTagName("description");
                                    html = descList.item(0).getTextContent();
                                }
                                meals.put(weekDays[dayOfWeek], getJuvenesMeals(getDescription(html)));
                            }
                        } catch (Exception e) {
                            //Logger.log("XML Pasing Excpetion = " + e);
                        }
                    }
                    handler.onGetWeeklyMealsSuccess(meals);
                }
            }).execute(url);
        } else {
            Map<String, ArrayList<Meal>> meals = new HashMap<>();
            handler.onGetWeeklyMealsSuccess(meals);
        }
    }

    private boolean isOnThisWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        Calendar compareCal = Calendar.getInstance();
        compareCal.setTime(date);

        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
        int compareWeek = compareCal.get(Calendar.WEEK_OF_YEAR);
        return currentWeek == compareWeek;
    }

    private Date getDate(List<String> words) {
        DateFormat dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        for (String word : words) {
            try {
                return dateFormat.parse(word);
            } catch (ParseException ex) {
                // string contains invalid date
            }
        }
        return null;
    }

    private String getDescription(String html) {
        String[] words = html.split("</strong></p>");
        if (words.length > 0 && words.length == 1) {
            return "";
        } else {
            return words[1];
        }
    }

    private ArrayList<Meal> getJuvenesMeals(String html) {
        ArrayList<Meal> meals = new ArrayList<>();
        List<String> mealsString = Arrays.asList(html.replace("<ul>", "").split("</ul>"));

        for (String mealHtml : mealsString) {
            if(!StringUtil.isEmptyString(mealHtml)) {
                Meal meal = new Meal();
                String[] lunchLiString = mealHtml.split("<li>");
                ArrayList<Lunch> lunches = new ArrayList<>();
                for (String lunchLi : lunchLiString) {
                    String lunchString = StringUtil.fromHtml(lunchLi).toString();
                    if (!StringUtil.isEmptyString(lunchString)) {
                        String[] lunchArr = lunchString.split("\\(");
                        String name = lunchArr[0].trim();
                        String diets = "";
                        Lunch lunch = new Lunch();
                        if (lunchArr.length > 1) {
                            String dietString = lunchArr[1].replace(")", "");
                            String[] dietsArray = dietString.split(",");
                            for (int i = 0; i < dietsArray.length; i++) {
                                String dietText = dietsArray[i].trim();

                                if (!StringUtil.isEmptyString(diets) && i > 0 && dietText.length() <= 3) {
                                    diets += ", ";
                                }

                                if (dietText.length() <= 3) {
                                    diets += dietText.toUpperCase();
                                }
                            }
                        }
                        lunch.name = name;
                        lunch.diet = diets.trim();
                        lunches.add(lunch);
                    }
                }
                meal.lunches = lunches;
                meals.add(meal);
            }
        }
        return meals;
    }

    private ArrayList<Meal> getArkeaMeals(JSONObject menus) {
        ArrayList<Meal> meals = new ArrayList<>();
        final List<String> notDiets = new ArrayList<>(Arrays.asList("*", "A", "VEG", "VS"));

        Iterator<?> keys = menus.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject node = getJSONObject(menus, key);
            Meal meal = new Meal();

            ArrayList<Lunch> lunches = new ArrayList<>();
            // Lisätään lounaan nimi. Esim @Arki
            Lunch mealName = new Lunch();
            mealName.name = key.trim();
            lunches.add(mealName);

            for (String object : getArrayNode(getJSONObject(node, "Components"), "Dish")) {
                String formattedName = object.replace("(", " ").replace(")", " ").replace(",", " ");
                String[] words = formattedName.split(" ");
                String lunchName = "";
                List<String> dietArr = new ArrayList<>();
                for (int wordIdx = 0; wordIdx < words.length; wordIdx++) {
                    String trimWord = words[wordIdx].trim();
                    if (!diets.contains(trimWord.toUpperCase()) && !notDiets.contains(trimWord.toUpperCase())) {
                        lunchName += trimWord + " ";
                    } else {
                        dietArr.add(trimWord);
                    }
                }
                int dietIdx = 0;
                String dietString = "";
                for (String diet : dietArr) {
                    if (dietIdx != 0) {
                        dietString += ", ";
                    }
                    dietString += diet;
                    dietIdx++;
                }
                Lunch lunch = new Lunch();
                lunch.name = lunchName.trim();
                lunch.diet = dietString.trim();

                lunches.add(lunch);
            }
            meal.lunches = lunches;
            meals.add(meal);
        }

        return meals;
    }

    private ArrayList<Meal> getUnicafeMeals(String description) {
        String[] foundSplits = description.split("\\.");
        ArrayList<Meal> meals = new ArrayList<>();
        for (int i = 0; i < foundSplits.length; i++) {
            if (foundSplits[i].contains(":")) {
                Meal meal = new Meal();
                Lunch lunch = new Lunch();
                String[] categories = foundSplits[i].split(":");
                String value = !StringUtil.isEmptyString(categories[1]) ? categories[1].trim() : "";

                String diets = "";
                if (foundSplits[i + 1] != null) {
                    String[] nextCategories = foundSplits[i + 1].split(":");
                    String nextProperty = !StringUtil.isEmptyString(nextCategories[0]) ? nextCategories[0].trim() : "";
                    String nextValue = !StringUtil.isEmptyString(nextCategories[1]) ? nextCategories[1].trim() : "";
                    if (nextProperty.toLowerCase().equals("allergeenit")) {
                        String[] dietsArray = nextValue.split(",");
                        for (int j = 0; j < dietsArray.length; j++) {
                            String dietText = dietsArray[j].trim();

                            if (!StringUtil.isEmptyString(diets) && j > 0 && dietText.length() <= 3) {
                                diets += ", ";
                            }

                            if (dietText.length() <= 3) {
                                diets += dietText.toUpperCase();
                            }
                        }
                        i++;
                    }
                }

                lunch.name = value;
                lunch.diet = diets;
                meal.lunches.add(lunch);
                meals.add(meal);
            }
        }

        return meals;
    }

    private ArrayList<Meal> getSodexoMeals(JSONObject json, String dailyNodeName) {
        ArrayList<Meal> meals = new ArrayList<>();
        for (int i = 0; i < getJSONArray(json, dailyNodeName).length(); i++) {
            JSONObject node = getJSONObject(getJSONArray(json, dailyNodeName), i);
            Meal meal = new Meal();
            meal.description = getNode(node, "desc_fi");
            meal.price = getNode(node, "price");

            Lunch lunch = new Lunch();
            lunch.name = getNode(node, "title_fi");
            lunch.diet = getNode(node, "properties");
            meal.lunches.add(lunch);

            meals.add(meal);
        }
        return meals;
    }

    private ArrayList<Meal> getSonaattiOrAmicaMeals(JSONArray menus) {
        ArrayList<Meal> meals = new ArrayList<>();
        final List<String> notDiets = new ArrayList<>(Arrays.asList("*", "A", "VEG", "VS"));

        for (int i = 0; i < menus.length(); i++) {
            JSONObject node = getJSONObject(menus, i);
            Meal meal = new Meal();

            String[] priceArr = getNode(node, "Price").split("LOUNAS ");
            meal.price = priceArr[priceArr.length > 1 ? 1 : 0];

            ArrayList<Lunch> lunches = new ArrayList<>();
            for (String object : getArrayNode(node, "Components")) {
                String formattedName = object.replace("(", " ").replace(")", " ").replace(",", " ");
                String[] words = formattedName.split(" ");
                String lunchName = "";
                List<String> dietArr = new ArrayList<>();
                for (int wordIdx = 0; wordIdx < words.length; wordIdx++) {
                    String trimWord = words[wordIdx].trim();
                    if (!diets.contains(trimWord.toUpperCase()) && !notDiets.contains(trimWord.toUpperCase())) {
                        lunchName += trimWord + " ";
                    } else {
                        dietArr.add(trimWord);
                    }
                }
                int dietIdx = 0;
                String dietString = "";
                for (String diet : dietArr) {
                    if (dietIdx != 0) {
                        dietString += ", ";
                    }
                    dietString += diet;
                    dietIdx++;
                }
                Lunch lunch = new Lunch();
                lunch.name = lunchName.trim();
                lunch.diet = dietString.trim();

                lunches.add(lunch);
            }
            meal.lunches = lunches;
            meals.add(meal);
        }

        return meals;
    }

    private String decode(String value) {
        return value.replace("&amp;", "&");
    }
}
