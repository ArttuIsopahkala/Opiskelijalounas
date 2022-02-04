package com.ardeapps.opiskelijalounas.services;

/**
 * Created by Arttu on 18.6.2017.
 */

public class FragmentListeners {

    public final static int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;

    private static FragmentListeners instance;

    public static FragmentListeners getInstance() {
        if(instance == null) {
            instance = new FragmentListeners();
        }
        return instance;
    }

    public interface PermissionHandledListener {
        void onPermissionGranted(int MY_PERMISSION);
        void onPermissionDenied(int MY_PERMISSION);
    }

    private PermissionHandledListener permissionHandledListener;

    public PermissionHandledListener getPermissionHandledListener() {
        return permissionHandledListener;
    }

    public void setPermissionHandledListener(PermissionHandledListener permissionHandledListener) {
        this.permissionHandledListener = permissionHandledListener;
    }
}
