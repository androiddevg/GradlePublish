package com.gp.jitpack.example.network;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.gp.jitpack.example.event.ApiCallUpdateListener;
import com.gp.jitpack.example.response.UpdateData;
import com.gp.jitpack.example.utils.LibraryUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class ApiClient {

    public interface ApiResponseListener {
        void onSuccess(UpdateData response);
        void onFailure(String errorMessage);
    }

    public static boolean makeApiCall(ApiResponseListener listener, ApiCallUpdateListener apiCallUpdateListener) {
        ApiCallTask task = new ApiCallTask(listener,apiCallUpdateListener);
        task.execute();
        try {
            task.get();
            return task.isSuccess();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class ApiCallTask extends AsyncTask<Void, Void, Void> {
        private ApiResponseListener listener;
        private ApiCallUpdateListener apiCallUpdateListener;
        private UpdateData response;
        private String errorMessage;
        private boolean success;
        public ApiCallTask(ApiResponseListener listener, ApiCallUpdateListener apiCallUpdateListener) {
            this.listener = listener;
            this.apiCallUpdateListener = apiCallUpdateListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // Create URL object with the API endpoint
                URL url = new URL(LibraryUtils.endPointJsonFileUrl);

                // Open connection
                connection = (HttpURLConnection) url.openConnection();

                // Set request method
                connection.setRequestMethod("GET");

                // Make the API call
                connection.connect();

                // Read the response
                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                // Convert the response to a POJO class using Gson or your preferred JSON parsing library
                Gson gson = new Gson();
                response = gson.fromJson(stringBuilder.toString(), UpdateData.class);
                success = true; // API call succeeded
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                success = false; // API call failed
            } finally {
                // Close connections and resources
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (success) {
                listener.onSuccess(response);
                apiCallUpdateListener.onSuccess();
            } else {
                listener.onFailure(errorMessage);
                apiCallUpdateListener.onFailure();
            }
        }

        public boolean isSuccess() {
            return success;
        }

    }
}


