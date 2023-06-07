/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gp.jitpack.example.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gp.jitpack.example.utils.Utils;

import java.util.Map;
import java.util.Set;

/**
 * @author arash
 */
public final class UpdateManagerPreferences {

    private static UpdateManagerPreferences singleton = null;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    private static final String REMINDER_DATE = "Reminder Date";
    private static final String DO_NOT_REMIND_ME = "Don't Remind Me";

    public enum ReminderAddType {
        MONTH("Month"),
        DAY("Day"),
        YEAR("Year"),
        NONE("none");

        private final String value;

        ReminderAddType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    private UpdateManagerPreferences(Context context) {
        preferences = context.getSharedPreferences("updateManagerPreferences", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static UpdateManagerPreferences getInstance(Context context) {
        if (singleton == null) {
            synchronized (UpdateManagerPreferences.class) {
                if (singleton == null) {
                    singleton = new UpdateManagerPreferences(context);
                }
            }
        }
        return singleton;
    }

    public UpdateManagerPreferences putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();

        return this;
    }

    public UpdateManagerPreferences putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();

        return this;
    }

    public UpdateManagerPreferences putLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();

        return this;
    }

    public UpdateManagerPreferences putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();

        return this;
    }

    public UpdateManagerPreferences putStringSet(String key, Set<String> values) {
        editor.putStringSet(key, values);
        editor.apply();

        return this;
    }
    public void clearReminderDate(String appVersion) {
        putString(REMINDER_DATE + appVersion, null);
        editor.apply();
    }
    public void clearDoNotRemindOption(String appVersion) {
        putBoolean(DO_NOT_REMIND_ME + appVersion, false);
        editor.apply();
    }

    public void clearAllSession(String appVersion)
    {
        putString(REMINDER_DATE + appVersion, null);
        putBoolean(DO_NOT_REMIND_ME + appVersion, false);
        editor.apply();
    }
    public String getReminderDate(String appVersion){
        Log.d("UPDATE_DATA","getReminderDate"+appVersion);
        return preferences.getString(REMINDER_DATE + appVersion, null);
    }

    public void setReminderDate(String appVersion,String reminderCount)
    {
        String currentDate = Utils.getCurrentDate(); // In future we can change the logic
        Log.d("UPDATE_DATA","currentDate"+currentDate);
        int reminderCountIS = Utils.convertStringToInt(reminderCount);
        String nextReminderDateIS = Utils.increaseDateByReminderCont(currentDate,reminderCountIS,ReminderAddType.DAY);
        Log.d("UPDATE_DATA","nextReminderDateIS"+nextReminderDateIS);
        Log.d("UPDATE_DATA","appVersion"+appVersion);
        putString(REMINDER_DATE + appVersion, nextReminderDateIS);
        editor.apply();
        printPreferenceValues();
    }

    private void printPreferenceValues() {
        // Retrieve all key-value pairs
        Map<String, ?> allPreferences = preferences.getAll();

        // Iterate over the preferences and print their values
        for (Map.Entry<String, ?> entry : allPreferences.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Log.d("UPDATE_DATA",  "key is: " + key);
            Log.d("UPDATE_DATA",  "value is: " + value);
        }
    }

    public Boolean getReminderOption(String appVersion){
        printPreferenceValues();
        return preferences.getBoolean(DO_NOT_REMIND_ME + appVersion, false);
    }
    public void setReminderOption(String appVersion,Boolean remindMe)
    {
        Log.d("UPDATE_DATA","ReminderOption set is::"+remindMe);
        putBoolean(DO_NOT_REMIND_ME + appVersion, remindMe);
        editor.apply();
    }
}
