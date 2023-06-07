package com.gp.jitpack.example.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.gp.jitpack.example.preferences.UpdateManagerPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    static String datePattern_yyyyMMdd_HHmmssZ = "yyyy-MM-dd HH:mm.SSS";
    static String datePattern_yyyyMMdd = "yyyy-MM-dd";
    static Boolean isGetDateWithTime = false;

    public static Boolean getIsGetDateWithTime() {
        return isGetDateWithTime;
    }

    public static void setIsGetDateWithTime(Boolean isGetDateWithTime) {
        Utils.isGetDateWithTime = isGetDateWithTime;
    }

    public static String getCurrentDate()
    {
        // Get the current date and time
        Calendar currentCalendar = Calendar.getInstance();
        TimeZone currentTimeZone = currentCalendar.getTimeZone();

        // Get the current date
        Date currentDate = new Date();

        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat(isGetDateWithTime?datePattern_yyyyMMdd_HHmmssZ:datePattern_yyyyMMdd);

        // Parse the target date and time string
        dateFormat.setTimeZone(currentTimeZone);
        try {
            // Format the date using the SimpleDateFormat object
            String formattedDate = dateFormat.format(currentDate);
            // Print the formatted date
            return formattedDate;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean compareTwoDates(String date1String, String date2String)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(isGetDateWithTime?datePattern_yyyyMMdd_HHmmssZ:datePattern_yyyyMMdd);

        try {
            // Parse the date strings into Date objects
            Date date1 = dateFormat.parse(date1String);
            Date date2 = dateFormat.parse(getActualDateWithTimeZoneDifference(date2String));

            // Compare the dates using compareTo
            int comparison = date1.compareTo(date2);

            // Print the comparison result
            if (comparison < 0) {
                System.out.println("Date 1 is before Date 2");
                return true;
            } else if (comparison > 0) {
                System.out.println("Date 1 is after Date 2");
                return false;
            } else {
                System.out.println("Date 1 is equal to Date 2");
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String convertToYyyyDdMm(String dateString)
    {
        SimpleDateFormat inputFormat = new SimpleDateFormat(datePattern_yyyyMMdd_HHmmssZ);
        // Set the UTC time zone explicitly
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat(datePattern_yyyyMMdd);

        try {
            // Parse the input date string into a Date object
            Date date = inputFormat.parse(dateString);
            // Format the date to the desired output format
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int convertStringToInt(String numberString)
    {
        try {
            return Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            // Parsing failed, handle the exception
            return 0; // Provide a default value or fallback option
        }
    }
    public static String increaseDateByReminderCont(String dateString,int reminderCount, UpdateManagerPreferences.ReminderAddType reminderAddType)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(isGetDateWithTime?datePattern_yyyyMMdd_HHmmssZ:datePattern_yyyyMMdd);

        try {
            // Parse the input date string into a Date object
            Date date = dateFormat.parse(dateString);

            // Create a Calendar instance and set the date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            if (reminderAddType== UpdateManagerPreferences.ReminderAddType.DAY)
            {
                // Increment the date by 3 days
                calendar.add(Calendar.DAY_OF_MONTH, reminderCount);
            }else if (reminderAddType== UpdateManagerPreferences.ReminderAddType.MONTH)
            {
               // Increase the month by 1
                calendar.add(Calendar.MONTH, reminderCount);
            }
            else if (reminderAddType== UpdateManagerPreferences.ReminderAddType.YEAR)
            {
                // Increase the year by 1
                calendar.add(Calendar.YEAR, reminderCount);
            }
            // Get the updated date from the Calendar
            Date updatedDate = calendar.getTime();
            // Format the updated date to the desired output format
            String updatedDateString = dateFormat.format(updatedDate);
            Log.d("UPDATE_DATA", "Updated Date: " + updatedDateString);
            return updatedDateString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getActualDateWithTimeZoneDifference(String dateString) {
        Log.d("UPDATE_DATA", "Original Date: " + dateString);
        // Get the current date and time
        Calendar currentCalendar = Calendar.getInstance();
        TimeZone currentTimeZone = currentCalendar.getTimeZone();

        // Parse the target date and time string
        SimpleDateFormat sdf = new SimpleDateFormat(isGetDateWithTime?datePattern_yyyyMMdd_HHmmssZ:datePattern_yyyyMMdd);
        sdf.setTimeZone(currentTimeZone);
        Calendar targetCalendar = Calendar.getInstance();
        try {
            targetCalendar.setTime(sdf.parse(dateString));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        TimeZone targetTimeZone = TimeZone.getTimeZone("UTC");

        // Calculate the time difference
        long currentTimeMillis = currentCalendar.getTimeInMillis();
        long targetTimeMillis = targetCalendar.getTimeInMillis();
        long timeDifferenceMillis = targetTimeMillis - currentTimeMillis;

        // Add the time difference to the current date and time
        long updatedTimeMillis = System.currentTimeMillis() + timeDifferenceMillis;

        // Set the updated time
        Calendar updatedCalendar = Calendar.getInstance();
        updatedCalendar.setTimeInMillis(updatedTimeMillis);
        updatedCalendar.setTimeZone(targetTimeZone);

        // Format the updated date and time
        sdf.setTimeZone(targetTimeZone);
        String updatedDateTime = sdf.format(updatedCalendar.getTime());

        // Display the updated date and time
        Log.d("UPDATE_DATA", "Updated Date and Time: " + updatedDateTime);
        return updatedDateTime;
    }

    public static void redirectToPlayStore(Context context,String updateUrl)
    {
        String packageName= updateUrl.replace("https://play.google.com/store/apps/details?id=","");
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }catch (ActivityNotFoundException e1)
            {
                e1.printStackTrace();
                // Google Play Store app is not installed, open the Play Store website instead
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

    }
}
