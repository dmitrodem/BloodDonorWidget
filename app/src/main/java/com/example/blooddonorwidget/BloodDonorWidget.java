package com.example.blooddonorwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class BloodDonorWidget extends AppWidgetProvider {

    private static final String PREFS_NAME = "BloodDonorPrefs";
    private static final String BLOOD_TYPE_KEY = "blood_type";
    private static final String IMAGE_URL = "https://fnkc.ru/donor_tl2.jsp";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update all widgets
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String bloodType = prefs.getString(BLOOD_TYPE_KEY, "A+");

        // Create RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.blood_donor_widget);

        // Set blood type text
        views.setTextViewText(R.id.widget_text, bloodType);

        // Set initial color (will be updated by background thread)
        views.setTextColor(R.id.widget_text, Color.GRAY);
        views.setTextColor(R.id.status_indicator, Color.GRAY);
        
        // Set initial update date
        updateDateText(views);

        // Set click intent to force update widget
        Intent intent = new Intent(context, BloodDonorWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Start background thread to update color based on image analysis
        updateWidgetColorInBackground(context, appWidgetId, bloodType);
    }

    private static void updateWidgetColorInBackground(final Context context, final int appWidgetId,
            final String bloodType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Get ROI coordinates based on blood type
                    int[] roi = getRoiCoordinates(bloodType);

                    // Analyze image color
                    int color = analyzeImageColor(roi[0], roi[1], roi[2], roi[3]);

                    // Update widget on UI thread
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            updateWidgetColor(context, appWidgetId, color, bloodType);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    // Update widget with error state on UI thread
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            updateWidgetError(context, appWidgetId);
                        }
                    });
                }
            }
        }).start();
    }

    private static int[] getRoiCoordinates(String bloodType) {
        // ROI coordinates for different blood types on the 900x450 image
        // These are example coordinates - you'll need to adjust based on actual image
        // layout
        switch (bloodType) {
            case "O+":
                return new int[] {  50, 160, 135, 195 };
            case "O-":
                return new int[] { 150, 160, 235, 195 };
            case "A+":
                return new int[] { 250, 160, 340, 195 }; // x1, y1, x2, y2
            case "A-":
                return new int[] { 350, 160, 440, 195 };
            case "B+":
                return new int[] { 455, 160, 540, 195 };
            case "B-":
                return new int[] { 555, 160, 645, 195 };
            case "AB+":
                return new int[] { 660, 160, 750, 195 };
            case "AB-":
                return new int[] { 760, 160, 850, 195 };
            default:
                return new int[] { 250, 160, 340, 195 };
        }
    }

    private static int analyzeImageColor(int x1, int y1, int x2, int y2) {
        try {
            // Disable SSL certificate validation
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Disable hostname verification
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            URI uri = new URI(IMAGE_URL);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            if (bitmap != null) {
                // Calculate average color in ROI
                long redSum = 0, greenSum = 0, blueSum = 0;
             
                // Extract ROI pixels efficiently using getPixels()
                int roiWidth = x2 - x1;
                int roiHeight = y2 - y1;
                int[] pixels = new int[roiWidth * roiHeight];
                bitmap.getPixels(pixels, 0, roiWidth, x1, y1, roiWidth, roiHeight);
                
                // Calculate average color
                for (int pixel : pixels) {
                    redSum += Color.red(pixel);
                    greenSum += Color.green(pixel);
                    blueSum += Color.blue(pixel);
                }

                int avgRed = (int) (redSum / pixels.length);
                int avgGreen = (int) (greenSum / pixels.length);
                int avgBlue = (int) (blueSum / pixels.length);

                // Classify color based on semaphore logic
                return classifyColor(avgRed, avgGreen, avgBlue);
             
            }

            input.close();
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Color.GREEN; // Default to green if analysis fails
    }

    private static int classifyColor(int red, int green, int blue) {
        // Simple color classification based on RGB values
        // Adjust thresholds based on actual image colors

        // Calculate color intensity ratios
        float total = red + green + blue;
        if (total == 0)
            return Color.GREEN;

        float redRatio = red / total;
        float greenRatio = green / total;

        // Red dominant (critical)
        if (redRatio > 0.5 && red > green * 1.5 && red > blue * 1.5) {
            return Color.RED;
        }
        // Green dominant (not needed)
        else if (greenRatio > 0.4 && green > red * 1.2 && green > blue * 1.2) {
            return Color.GREEN;
        }
        // Yellow (need some) - high red and green, low blue
        else if (red > 150 && green > 150 && blue < 100) {
            return Color.YELLOW;
        }
        // Default to green
        else {
            return Color.GREEN;
        }
    }

    private static void updateWidgetColor(Context context, int appWidgetId, int color, String bloodType) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.blood_donor_widget);

        views.setTextViewText(R.id.widget_text, bloodType);
        views.setTextColor(R.id.widget_text, color);
        views.setTextColor(R.id.status_indicator, color);
        
        // Update date
        updateDateText(views);

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {
            // Update all widgets
            ComponentName thisWidget = new ComponentName(context, BloodDonorWidget.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            for (int widgetId : allWidgetIds) {
                appWidgetManager.updateAppWidget(widgetId, views);
            }
        }
    }

    private static void updateWidgetError(Context context, int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.blood_donor_widget);

        views.setTextViewText(R.id.widget_text, context.getString(R.string.widget_error));
        views.setTextColor(R.id.widget_text, Color.GRAY);
        views.setTextColor(R.id.status_indicator, Color.GRAY);
        
        // Update date even on error
        updateDateText(views);

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {
            ComponentName thisWidget = new ComponentName(context, BloodDonorWidget.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            for (int widgetId : allWidgetIds) {
                appWidgetManager.updateAppWidget(widgetId, views);
            }
        }
    }

    public static void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, BloodDonorWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    private static void updateDateText(RemoteViews views) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        views.setTextViewText(R.id.update_date, currentDate);
    }
}
