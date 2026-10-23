package com.example.verticalloop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity implements SensorEventListener {
    private WebView web;
    private SensorManager sm;
    private boolean sensing = false;
    private boolean want = false;
    private long lastSend = 0;

    /** Lets the game ask for the accelerometer (used for motion-sensor steering). */
    public class Bridge {
        @JavascriptInterface
        public void startSensors() {
            want = true;
            runOnUiThread(new Runnable() { public void run() { startSense(); } });
        }
        @JavascriptInterface
        public void stopSensors() {
            want = false;
            runOnUiThread(new Runnable() { public void run() { stopSense(); } });
        }
    }

    private void startSense() {
        if (sensing || sm == null) return;
        Sensor a = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (a != null) {
            sm.registerListener(this, a, SensorManager.SENSOR_DELAY_GAME);
            sensing = true;
        }
    }

    private void stopSense() {
        if (sensing && sm != null) {
            sm.unregisterListener(this);
            sensing = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        long n = System.currentTimeMillis();
        if (n - lastSend < 20) return;
        lastSend = n;
        final String js = "window.__nativeAccel&&window.__nativeAccel("
                + e.values[0] + "," + e.values[1] + "," + e.values[2] + ")";
        web.post(new Runnable() { public void run() { web.evaluateJavascript(js, null); } });
    }

    @Override
    public void onAccuracyChanged(Sensor s, int a) { }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        lp.preferredRefreshRate = 120f; // lets the High (120 FPS) setting use a 120 Hz screen
        getWindow().setAttributes(lp);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        s.setSupportZoom(false);
        web.setBackgroundColor(0xFF000000);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.loadUrl("file:///android_asset/index.html");
    }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) immersive(); }
    @Override protected void onResume() { super.onResume(); web.onResume(); if (want) startSense(); }
    @Override protected void onPause() { stopSense(); web.onPause(); super.onPause(); }
    @Override protected void onDestroy() { stopSense(); web.destroy(); super.onDestroy(); }
}
