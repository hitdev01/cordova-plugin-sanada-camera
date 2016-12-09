package com.hitdev01.cordova.plugin.sanada;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Display;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * This class echoes a string called from JavaScript.
 */
public class CameraPlugin extends CordovaPlugin implements SensorEventListener {
    private String TAG = "SanadaCameraPlugin";
    private CameraFragment fragment;
    private SensorManager sensorManager;    // Sensor manager
    public CallbackContext callbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.sensorManager = (SensorManager) cordova.getActivity().getSystemService(Context.SENSOR_SERVICE);
        Log.d(TAG, "initialize");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute[" + action + "]");
        this.callbackContext = callbackContext;
        if (action.equals("start")) {
            startCamera();
            return true;
        }
        return false;
    }

    private void startCamera() {
      Log.d(TAG, "startCamera");
      fragment = new CameraFragment();

      cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
              try {
                  Log.d(TAG, "startCamera#runOnUiThread");
                  DisplayMetrics displayMetrics = cordova.getActivity().getResources().getDisplayMetrics();
                  Display display = cordova.getActivity().getWindowManager().getDefaultDisplay();
                  display.getMetrics(displayMetrics);
                  float width = display.getWidth();
                  float height = display.getHeight();
                  Log.d(TAG, "display x=" + String.valueOf(width) + ", y=" + String.valueOf(height));

                  String alpha = "30";
                  fragment.setBackButtonListener(backButtonListener);
                  fragment.setShootButtonListener(shootButtonListener);

                  //create or update the layout params for the container view
                  FrameLayout containerView = (FrameLayout)cordova.getActivity().findViewById(1);
                  if(containerView == null){
                      containerView = new FrameLayout(cordova.getActivity().getApplicationContext());
                      containerView.setId(1);

                      FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                      Log.d(TAG, "startCamera#addContentView");
                      cordova.getActivity().addContentView(containerView, containerLayoutParams);
                  }
                  //display camera bellow the webview
                  Log.d(TAG, "startCamera#setBackgroundColorAlpha");
                  // containerView.setAlpha(Float.parseFloat(alpha));
                  containerView.bringToFront();

                  //add the fragment to the container
                  Log.d(TAG, "startCamera#addfragmentContainer");
                  FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
                  FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                  fragmentTransaction.add(containerView.getId(), fragment);
                  fragmentTransaction.commit();
              }
              catch(Exception e){
                  Log.e(TAG, e.getLocalizedMessage(), e);
              }
          }
      });
    }

    CameraFragment.BackButtonListener backButtonListener = new CameraFragment.BackButtonListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick");
            callbackContext.success();

            // CameraFragmentをデタッチ
            FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.detach(fragment);
            fragmentTransaction.commit();
        }
    };

    CameraFragment.ShootButtonListener shootButtonListener = new CameraFragment.ShootButtonListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick");
            Uri uri = fragment.getLastCaptureImageURI();
            if (uri != null) {
                callbackContext.success(uri.getPath());
            }

            // CameraFragmentをデタッチ
            FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.detach(fragment);
            fragmentTransaction.commit();
        }
    };


    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
