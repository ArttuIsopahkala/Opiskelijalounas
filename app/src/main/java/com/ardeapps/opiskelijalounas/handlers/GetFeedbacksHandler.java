package com.ardeapps.opiskelijalounas.handlers;

import com.ardeapps.opiskelijalounas.objects.Restaurant;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Arttu on 2.10.2017.
 */

public interface GetFeedbacksHandler {
    void onGetFeedbacksSuccess(Map<String, String> feedbacks);
}
