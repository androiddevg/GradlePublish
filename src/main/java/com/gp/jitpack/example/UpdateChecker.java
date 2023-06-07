package com.gp.jitpack.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gp.jitpack.example.event.ApiCallUpdateListener;
import com.gp.jitpack.example.event.UpdateCheckListener;
import com.gp.jitpack.example.network.ApiClient;
import com.gp.jitpack.example.preferences.UpdateManagerPreferences;
import com.gp.jitpack.example.response.UpdateData;
import com.gp.jitpack.example.utils.LibraryUtils;
import com.gp.jitpack.example.utils.Utils;

import gp.jitpack.example.R;

public class UpdateChecker implements ApiClient.ApiResponseListener {
    Context mContext;
    UpdateCheckListener mListener;
    private static volatile UpdateChecker singleton;

    UpdateData updateData = null;
    UpdateType updateTypeIS = UpdateType.NoUpdate;

    public enum UpdateOption{
        Optional("Optional"),
        Mandatory("Mandatory"),
        NONE("None");
        private final String value;

        UpdateOption(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    public enum UpdateType {
        NoUpdate("NoUpdate"),
        MandatoryUpdate("MandatoryUpdate"),
        MandatoryWithGracePeriodUpdate("MandatoryUpdateWithGracePeriod"),
        MandatoryUpdateOldNonSupportedDevice("MandatoryUpdateOldNonSupportedDevice"),
        OptionalUpdateOldNonSupportedDevice("OptionalUpdateOldNonSupportedDevice"),
        OptionalUpdate("OptionalUpdate");

        private final String value;

        UpdateType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    public UpdateChecker(Context context) {
        mContext = context;
    }

    public void setListener(UpdateCheckListener listener) {
        mListener = listener;
    }


    public static UpdateChecker getInstance(Context context) {
        if (singleton == null) {
            synchronized (UpdateChecker.class) {
                if (singleton == null) {
                    singleton = new UpdateChecker(context);
                }
            }
        }

        return singleton;   // gets initialized when the class loads into memory.
    }

    public boolean callJsonFile(ApiCallUpdateListener listener) {
        return ApiClient.makeApiCall(this, listener);
    }

    public void checkForUpdates(ApiCallUpdateListener listener) {
        if (updateData == null) {
            if (callJsonFile(listener)) {
                continueWithAppUpdate(false);
            } else {
                listener.onFailure();
            }
        } else {
            continueWithAppUpdate(false);
            listener.onFailure();
        }
    }

    /**
     * Check if an update is required
     */
    public boolean continueWithAppUpdate(Boolean skipNonSupportedPlatformVersion) {
        int currentVersion = LibraryUtils.getVersionCode();
        int minSdkVersion = LibraryUtils.getMinSdkVersion();
        int latestVersion = (getLatestVersionFromServer() != null && getLatestVersionFromServer().length() > 0) ? Integer.parseInt(getLatestVersionFromServer()) : -1;
        int minPlatformTargetFromServer = (getMinPlatformTargetFromServer() != null && getMinPlatformTargetFromServer().length() > 0) ? Integer.parseInt(getMinPlatformTargetFromServer()) : -1;
        if (updateData != null) {
            Log.d("UPDATE_DATA", "updateData getVersionCode" + updateData.getVersionCode());
            Log.d("UPDATE_DATA", "updateData getType" + updateData.getType());
        }
        Log.d("UPDATE_DATA", "latestVersion" + latestVersion);
        Log.d("UPDATE_DATA", "currentVersion" + currentVersion);
        Log.d("UPDATE_DATA", "minPlatformTargetFromServer" + minPlatformTargetFromServer);
        Log.d("UPDATE_DATA", "minSdkVersion" + minSdkVersion);

        if (latestVersion == -1 || ((currentVersion >= latestVersion)&&!skipNonSupportedPlatformVersion)) {
            // No update required
            updateTypeIS = UpdateType.NoUpdate;
            if (mListener != null) {
                mListener.onNoUpdateRequired();
            }
            UpdateManagerPreferences.getInstance(LibraryUtils.getPreferenceContext()).clearAllSession(getLatestVersionFromServer());
            Log.d("UPDATE_DATA", "is in NoUpdate" + updateTypeIS.value);
        } else if (isMandatoryUpdateRequired()) {
            // Mandatory update required
            boolean updateByDate;
            String updateBy = getUpdateBy();
            //updateBy date is null or equal to current Date
            updateByDate = updateBy == null || updateBy.equalsIgnoreCase(Utils.getCurrentDate());
            if (minPlatformTargetFromServer > minSdkVersion) {
                updateTypeIS = UpdateType.MandatoryUpdateOldNonSupportedDevice;

            }else{
                if (updateByDate)
                {
                    updateTypeIS = UpdateType.MandatoryUpdate;
                }else {
                    String reminderDate = getStoredReminderDate();
                    Log.d("UPDATE_DATA", "reminderDate is::" + reminderDate);

                    boolean isReminderDateNull = reminderDate == null;
                    boolean checkReminderDate=true;
                    boolean compareDates=true;
                    if (!isReminderDateNull)
                    {
                        checkReminderDate = reminderDate.equalsIgnoreCase(Utils.getCurrentDate());

                    }
                    if (!isReminderDateNull)
                    {
                        compareDates = Utils.compareTwoDates(reminderDate, updateBy);

                    }
                    boolean isReminderDateEqualCurrentDate = isReminderDateNull || checkReminderDate || compareDates;
                    Log.d("UPDATE_DATA", "isReminderDateEqualCurrentDate" + isReminderDateEqualCurrentDate);

                    updateTypeIS = isReminderDateEqualCurrentDate?UpdateType.MandatoryWithGracePeriodUpdate:UpdateType.NoUpdate;

                }
            }
            if (mListener != null) {
                mListener.onMandatoryUpdateAvailable(String.valueOf(latestVersion), updateByDate);
            }
            Log.d("UPDATE_DATA", "is in isMandatoryUpdateRequired" + updateTypeIS.value);

            return updateByDate;
        } else if (isOptionalUpdateRequired()) {
            // Optional update available
            Boolean isGracePeriod = isInGracePeriod();
            int previousMandatoryVersion= Utils.convertStringToInt(previousMandatoryVersion());

            String reminderDate = getStoredReminderDate();
            String currentDate = Utils.getCurrentDate();

            Boolean isDoNotRemindSelected = getSelectedRemindMeOption();
            //reminderDate is null or equal to current Date
            boolean isReminderDateEqualCurrentDate = reminderDate == null || reminderDate.equalsIgnoreCase(currentDate);
            Log.d("UPDATE_DATA", "isReminderDateEqualCurrentDate" + isReminderDateEqualCurrentDate);
            Log.d("UPDATE_DATA", "reminderDate" + reminderDate);
            Log.d("UPDATE_DATA", "currentDate" + currentDate);
            Log.d("UPDATE_DATA", "isDoNotRemindSelected" + isDoNotRemindSelected);

            if (isDoNotRemindSelected)
            {
                updateTypeIS =UpdateType.NoUpdate;
            }
            else if (isReminderDateEqualCurrentDate)
            {
                if (previousMandatoryVersion>currentVersion)
                {
                    updateTypeIS =UpdateType.MandatoryUpdate;

                }else {
                    updateTypeIS = minPlatformTargetFromServer < minSdkVersion ? UpdateType.OptionalUpdate : UpdateType.OptionalUpdateOldNonSupportedDevice;
                }
                if (mListener != null) {
                    mListener.onOptionalUpdateAvailable(String.valueOf(latestVersion), isGracePeriod);
                }
            }else {
                updateTypeIS = UpdateType.NoUpdate;
                if (mListener != null) {
                    mListener.onNoUpdateRequired();
                }
            }
            Log.d("UPDATE_DATA", "is in isOptionalUpdateRequired" + updateTypeIS.value);

        }

        return false;
    }

    private String getLatestVersionFromServer() {
        // Make API call to get the latest version from the server
        // Return the latest version or null if an error occurred
        return updateData != null ? updateData.getVersionCode() : "";
    }

    private String getMinPlatformTargetFromServer() {
        // Make API call to get the latest version from the server
        // Return the latest version or null if an error occurred
        return updateData != null ? updateData.getPlatformMinTarget() : "";
    }

    private Boolean isMandatoryUpdateRequired() {
        // Check if a mandatory update is required
        // Return true if a mandatory update is required, false otherwise
        return updateData != null && updateData.getType().equalsIgnoreCase(UpdateOption.Mandatory.value);
    }

    private Boolean isOptionalUpdateRequired() {
        // Check if a mandatory update is required
        // Return true if a mandatory update is required, false otherwise
        return updateData != null && updateData.getType().equalsIgnoreCase(UpdateOption.Optional.value);
    }
    private String getUpdateBy() {
        // Make API call to get the latest version from the server
        // Return the latest version or null if an error occurred
        return updateData != null ? updateData.getUpdateBy() : "";
    }

    private String getUpdateUrl() {
        // Make API call to get the latest version from the server
        // Return the latest version or null if an error occurred
        return updateData != null ? updateData.getUpdateURL() : "";
    }
    private String getStoredReminderDate() {
        // Make API call to get the latest version from the server
        // Return the latest version or null if an error occurred
        return UpdateManagerPreferences.getInstance(LibraryUtils.getPreferenceContext()).getReminderDate(getLatestVersionFromServer());
    }
    private Boolean getSelectedRemindMeOption() {
        // Make API call to get the latest version from the server
        // Return the latest version or null if an error occurred
        return UpdateManagerPreferences.getInstance(LibraryUtils.getPreferenceContext()).getReminderOption(getLatestVersionFromServer());
    }
    private String getReminder() {
        // Make API call to get the latest version from the server
        // Return the latest version or null if an error occurred
        return updateData != null ? updateData.getReminders() : "";
    }

    private Boolean isInGracePeriod() {
        // Check if the app is in the grace period for mandatory updates
        // Return true if the app is in the grace period, false otherwise
        return updateData != null && !updateData.getUpdateBy().isEmpty();
    }

    private String previousMandatoryVersion() {
        // Check if the app is in the grace period for mandatory updates
        // Return true if the app is in the grace period, false otherwise
        return updateData != null?updateData.getPreviousMandatoryVersionCode():"";
    }
    @Override
    public void onSuccess(UpdateData response) {
        updateData = response;
        /*To Test Updates with different Scenarios*/
        updateData.setUpdateBy("2023-05-31 12:00.00+000");
        updateData.setVersionCode("804");
        /* updateData.setType(UpdateOption.Optional.value);*/
        Log.d("UPDATE_DATA", "onSuccess getUpdateBy" + response.getUpdateBy());
        Log.d("UPDATE_DATA", "onSuccess getType" + response.getType());
        Log.d("UPDATE_DATA", "onSuccess getVersionCode" + response.getVersionCode());

    }

    @Override
    public void onFailure(String errorMessage) {
        updateData = null;
    }

    /**
     * based on update type manage dialogs
     */
    public void showUpdateDialog(Activity activity,Boolean skipNonSupportedPlatformVersion) {
        continueWithAppUpdate(skipNonSupportedPlatformVersion);
        if (updateTypeIS!=UpdateType.NoUpdate) {
            Dialog dialog = new Dialog(activity != null ? activity : mContext);
            dialog.setContentView(R.layout.dialog_rounded);

            TextView titleTextView = dialog.findViewById(R.id.dialog_title);
            TextView messageTextView = dialog.findViewById(R.id.dialog_message);
            TextView positiveButton = dialog.findViewById(R.id.dialog_button_positive);
            TextView negativeButton = dialog.findViewById(R.id.dialog_button_negative);
            View lineView = dialog.findViewById(R.id.lineView);
            LinearLayout remindMeAgainArea = dialog.findViewById(R.id.remindMeAgainArea);
            ImageView selectIv = dialog.findViewById(R.id.selectIv);

            titleTextView.setText(mContext.getString(R.string.update_required));
            positiveButton.setText(mContext.getString(R.string.ok_btn));
            negativeButton.setText(mContext.getString(R.string.ok_cancel));

            if (updateTypeIS == UpdateType.MandatoryUpdate) {
                messageTextView.setText(String.format(mContext.getString(R.string.mandatory_msg),getLatestVersionFromServer()));
            } else if (updateTypeIS == UpdateType.OptionalUpdate) {
                titleTextView.setText(mContext.getString(R.string.update_available));
                messageTextView.setText(String.format(mContext.getString(R.string.optional_msg),getLatestVersionFromServer()));
                lineView.setVisibility(View.VISIBLE);
                negativeButton.setVisibility(View.VISIBLE);
                remindMeAgainArea.setVisibility(View.VISIBLE);
                remindMeAgainArea.setOnClickListener(view -> {
                    if (remindMeAgainArea.getTag().equals("false"))
                    {
                        remindMeAgainArea.setTag("true");
                        selectIv.setImageResource(LibraryUtils.getCheckboxSelected());
                    }else {
                        remindMeAgainArea.setTag("false");
                        selectIv.setImageResource(LibraryUtils.getCheckboxUnselected());
                    }
                });
            }else if (updateTypeIS == UpdateType.MandatoryWithGracePeriodUpdate) {
                Log.d("UPDATE_DATA", "getUpdateBy" + getUpdateBy());
                String date = Utils.convertToYyyyDdMm(getUpdateBy());
                Log.d("UPDATE_DATA", "getUpdate date" + date);
                messageTextView.setText(String.format(mContext.getString(R.string.mandatory_grace_msg),date,getLatestVersionFromServer()));
                lineView.setVisibility(View.VISIBLE);
                negativeButton.setVisibility(View.VISIBLE);
            } else if (updateTypeIS == UpdateType.MandatoryUpdateOldNonSupportedDevice) {
                messageTextView.setText(String.format(mContext.getString(R.string.mandatory_non_supported_msg),LibraryUtils.getVersionNameFromSdkVersion(getMinPlatformTargetFromServer())));
            } else if (updateTypeIS == UpdateType.OptionalUpdateOldNonSupportedDevice) {
                titleTextView.setText(mContext.getString(R.string.update_available));
                messageTextView.setText(String.format(mContext.getString(R.string.optional_non_supported_msg),LibraryUtils.getVersionNameFromSdkVersion(getMinPlatformTargetFromServer())));
            }
            positiveButton.setOnClickListener(v -> {
                // Perform positive button action
                if (updateTypeIS == UpdateType.OptionalUpdate) {
                    UpdateManagerPreferences.getInstance(LibraryUtils.getPreferenceContext()).clearAllSession(getLatestVersionFromServer());
                    UpdateManagerPreferences.getInstance(LibraryUtils.getPreferenceContext()).setReminderOption(getLatestVersionFromServer(), remindMeAgainArea.getTag().equals("true"));
                    dialog.dismiss();
                    return;
                }

                if (remindMeAgainArea.getTag().equals("false") && updateTypeIS == UpdateType.OptionalUpdate) {
                   dialog.dismiss();
                   return;
                }

                if (!skipNonSupportedPlatformVersion && (updateTypeIS == UpdateType.OptionalUpdateOldNonSupportedDevice || updateTypeIS == UpdateType.MandatoryUpdateOldNonSupportedDevice)) {
                    showUpdateDialog(activity,true);
                    dialog.dismiss();
                    return;
                }

                Utils.redirectToPlayStore(mContext, getUpdateUrl());

                if (activity!=null)
                {
                    killApp(activity);
                }

            });

            negativeButton.setOnClickListener(v -> {
                // Perform negative button action
                if (updateTypeIS != UpdateType.MandatoryUpdate) {
                    UpdateManagerPreferences.getInstance(LibraryUtils.getPreferenceContext()).setReminderDate(getLatestVersionFromServer(), getReminder());
                }

                if (updateTypeIS == UpdateType.OptionalUpdate) {
                    UpdateManagerPreferences.getInstance(LibraryUtils.getPreferenceContext()).setReminderOption(getLatestVersionFromServer(), remindMeAgainArea.getTag().equals("true"));
                }
                dialog.dismiss();
            });

            dialog.setCancelable(updateTypeIS == UpdateType.MandatoryUpdate);
            dialog.show();
        }
    }

    public void killApp(Activity activity) {
        activity.finishAffinity();
        System.exit(0);
    }
}
