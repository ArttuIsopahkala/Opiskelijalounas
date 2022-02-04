package com.ardeapps.opiskelijalounas.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.R;
import com.ardeapps.opiskelijalounas.objects.Restaurant;
import com.ardeapps.opiskelijalounas.services.AppInviteService;

import java.util.ArrayList;
import java.util.Map;

public class InfoFragment extends Fragment {

    public interface Listener {
        void addRestaurantClick();
        void onAdminRestaurantSelected(Restaurant restaurant);
        void onAdminFeedbackSelected(String text);
        void onAdminRemoveRestaurantRequest(String restaurantId);
        void onAdminRemoveFeedback(String key);
    }

    Listener mListener = null;

    public void setListener(Listener l) {
        mListener = l;
    }

    Button writeFeedback;
    Button addRestaurant;
    Button inviteFriends;
    Button rate;
    Button more;
    LinearLayout restaurantRequestContainer;
    LinearLayout feedbackContainer;
    LinearLayout adminContent;
    TextView privacyPolicyText;

    ArrayList<Restaurant> restaurantRequests;
    Map<String, String> feedbacks;

    Context context = AppRes.getContext();
    AppRes appRes = (AppRes)AppRes.getContext();

    public void refreshData() {
        restaurantRequests = appRes.getRestaurantRequests();
        feedbacks = appRes.getFeedbacks();
    }

    public void update() {
        if(appRes.getIsAdmin()) {
            adminContent.setVisibility(View.VISIBLE);
            restaurantRequestContainer.removeAllViews();
            feedbackContainer.removeAllViews();
            for(final Restaurant restaurantRequest : restaurantRequests) {
                TextView nameText = new TextView(getActivity());
                for(Restaurant restaurant : appRes.getRestaurants()) {
                    if(restaurant.restaurantId.equals(restaurantRequest.restaurantId)) {
                        nameText.setText(restaurant.name);
                        break;
                    }
                }
                nameText.setPadding(10, 10, 10, 10);
                nameText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onAdminRestaurantSelected(restaurantRequest);
                    }
                });
                nameText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showRemoveItemDialog(false, restaurantRequest.restaurantId);
                        return false;
                    }
                });
                restaurantRequestContainer.addView(nameText);
            }

            for (final Map.Entry<String, String> entry : feedbacks.entrySet()) {
                TextView feedbackText = new TextView(getActivity());
                feedbackText.setText(entry.getValue());
                feedbackText.setPadding(5, 5, 5, 5);
                feedbackText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onAdminFeedbackSelected(entry.getValue());
                    }
                });
                feedbackText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showRemoveItemDialog(true, entry.getKey());
                        return false;
                    }
                });
                feedbackContainer.addView(feedbackText);
            }
        } else {
            adminContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        restaurantRequestContainer = (LinearLayout) v.findViewById(R.id.restaurantRequestContainer);
        feedbackContainer = (LinearLayout) v.findViewById(R.id.feedbackContainer);
        adminContent = (LinearLayout) v.findViewById(R.id.adminContent);
        writeFeedback = (Button) v.findViewById(R.id.writeFeedback);
        addRestaurant = (Button) v.findViewById(R.id.addRestaurant);
        inviteFriends = (Button) v.findViewById(R.id.inviteFriends);
        rate = (Button) v.findViewById(R.id.rate);
        more = (Button) v.findViewById(R.id.more);
        privacyPolicyText = (TextView) v.findViewById(R.id.privacyPolicy);

        update();

        writeFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendFeedbackDialogFragment dialog = new SendFeedbackDialogFragment();
                dialog.show(getFragmentManager(), "Lähetä palaute");
            }
        });

        addRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addRestaurantClick();
            }
        });

        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppInviteService.openChooser();
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrl("http://play.google.com/store/apps/details?id=com.ardeapps.opiskelijalounas");
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrl("http://play.google.com/store/apps/developer?id=Arde+Apps");
            }
        });

        privacyPolicyText.setText(Html.fromHtml("<u>" + getString(R.string.info_link_privacy) + "</u>"));
        privacyPolicyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrl("https://docs.google.com/document/d/e/2PACX-1vS-dowFeHApNzL6f708qxGK0exl37rkDxBgccfhaMNztUgJMmTuGw0t8VdIOMjrrvXxkyXWU7bFebkT/pub");
            }
        });

        return v;
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void showRemoveItemDialog(final boolean feedback, final String key) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AppTheme);
        alert.setMessage(feedback ? getString(R.string.info_remove_feedback) : getString(R.string.info_remove_restaurant_request));
        alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(feedback) {
                    mListener.onAdminRemoveFeedback(key);
                } else {
                    mListener.onAdminRemoveRestaurantRequest(key);
                }
                dialog.dismiss();
            }
        });

        alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

}
