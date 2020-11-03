package kc.fyp.ecare.services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import kc.fyp.ecare.R;

public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String ONE_SIGNAL_URL = "https://onesignal.com/api/v1/notifications";
    private static final String APP_ID = "0794e1d8-e80a-43dc-9412-38fdf9f20b1d";

    public static void sendNotificationToAllUsers(Context context, final String message, final String type, final String user_id) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                try {
                    String jsonResponse;

                    URL url = new URL(ONE_SIGNAL_URL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setUseCaches(false);
                    con.setDoOutput(true);
                    con.setDoInput(true);

                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setRequestProperty("Authorization", "Basic " + context.getResources().getString(R.string.oneSignalAppSecret));
                    con.setRequestMethod("POST");
                    String strJsonBody = "{"
                            + "\"app_id\": \"" + APP_ID + "\","
                            + "\"filters\": [{\"field\": \"tag\", \"key\": \"id\", \"relation\": \"!=\", \"value\": \"" + user_id + "\"}],"
                            + "\"data\": {\"type\": \"" + type + "\"},"
                            + "\"contents\": {\"en\": \"" + message + "\"}"
                            + "}";
                    Log.e(TAG, strJsonBody);

                    byte[] sendBytes = strJsonBody.getBytes(StandardCharsets.UTF_8);
                    con.setFixedLengthStreamingMode(sendBytes.length);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(sendBytes);

                    int httpResponse = con.getResponseCode();
                    Log.e(TAG, "HttpResponse: " + httpResponse);

                    Scanner scanner;
                    if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                        scanner = new Scanner(con.getInputStream(), "UTF-8");
                    } else {
                        scanner = new Scanner(con.getErrorStream(), "UTF-8");
                    }
                    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    scanner.close();
                    Log.e(TAG, jsonResponse);
                } catch (Exception e) {
                    Log.e(TAG, "Notification not send");
                }
            }
        });
    }

    public static void sendNotificationToUser(Context context, final String message, final String id, final String type, final String user_id) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                try {
                    String jsonResponse;

                    URL url = new URL(ONE_SIGNAL_URL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setUseCaches(false);
                    con.setDoOutput(true);
                    con.setDoInput(true);

                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setRequestProperty("Authorization", "Basic " + context.getResources().getString(R.string.oneSignalAppSecret));
                    con.setRequestMethod("POST");
                    String strJsonBody = "{"
                            + "\"app_id\": \"" + APP_ID + "\","
                            + "\"filters\": [{\"field\": \"tag\", \"key\": \"id\", \"relation\": \"=\", \"value\": \"" + user_id + "\"}],"
                            + "\"data\": {\"id\": \"" + id + "\", \"type\": \"" + type + "\"},"
                            + "\"contents\": {\"en\": \"" + message + "\"}"
                            + "}";

                    Log.e(TAG, strJsonBody);

                    byte[] sendBytes = strJsonBody.getBytes(StandardCharsets.UTF_8);
                    con.setFixedLengthStreamingMode(sendBytes.length);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(sendBytes);

                    int httpResponse = con.getResponseCode();
                    Log.e(TAG, "HttpResponse: " + httpResponse);

                    Scanner scanner;
                    if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                        scanner = new Scanner(con.getInputStream(), "UTF-8");
                    } else {
                        scanner = new Scanner(con.getErrorStream(), "UTF-8");
                    }
                    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    scanner.close();
                    Log.e(TAG, jsonResponse);
                } catch (Exception e) {
                    Log.e(TAG, "Notification not send");
                }
            }
        });
    }
}
