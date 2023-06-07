package com.gp.jitpack.example.utils;

import android.content.Context;

import com.gp.jitpack.example.preferences.UpdateManagerPreferences;

public class LibraryUtils {
    public static String endPointJsonFileUrl;
    public static Context preferenceContext;
    static String versionName;
    static int versionCode;
    static int minSdkVersion;
    static int checkboxSelected;
    static int checkboxUnselected;
    public static void setAppVersion(String currVersionName, int currVersionCode, int currMinSdkVersion,String manifestUrl, int checkbox_selected, int checkbox_unselected) {
        versionName= currVersionName;
        versionCode= currVersionCode;
        minSdkVersion= currMinSdkVersion;
        endPointJsonFileUrl = manifestUrl;
        checkboxSelected= checkbox_selected;
        checkboxUnselected= checkbox_unselected;
    }

    public static void initiatePreferenceManager(Context context) {
        preferenceContext = context;
        UpdateManagerPreferences.getInstance(context);
    }

    public static int getCheckboxSelected() {
        return checkboxSelected;
    }

    public static int getCheckboxUnselected() {
        return checkboxUnselected;
    }

    public static Context getPreferenceContext() {
        return preferenceContext;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static int getVersionCode() {
        return versionCode;
    }

    public static int getMinSdkVersion() {
        return minSdkVersion;
    }

    public static String getVersionNameFromSdkVersion(String sdkVer)
    {
        int sdkVersion = Utils.convertStringToInt(sdkVer);
        String versionName = "Unknown";

        try {
            Class<?> buildVersionClass = Class.forName("android.os.Build$VERSION");
            java.lang.reflect.Field sdkIntField = buildVersionClass.getField("SDK_INT");
            int sdkIntValue = sdkIntField.getInt(null);

            if (sdkIntValue == sdkVersion) {
                java.lang.reflect.Field releaseField = buildVersionClass.getField("RELEASE");
                Object releaseObj = releaseField.get(null);

                if (releaseObj != null && releaseObj instanceof String) {
                    versionName = (String) releaseObj;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}

