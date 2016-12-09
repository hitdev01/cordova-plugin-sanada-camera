package com.hitdev01.cordova.plugin.sanada;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;


public class SanadaImageView extends ImageView {
    private String TAG = "SanadaCameraSanadaImageView";
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private final float SCALE_MAX = 3.0f;
    private final float SCALE_MIN = 1.0f;
    private final float PINCH_SENSITIVITY = 1.0f;
    private float scaleFactor = 1.0f;

    public SanadaImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "SanadaImageView(Context context, AttributeSet attrs, int defStyleAttr)");
    }

    public SanadaImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "SanadaImageView(Context context, AttributeSet attrs)");
        init(context);
    }

    public SanadaImageView(Context context) {
        super(context);
        Log.d(TAG, "SanadaImageView(Context context)");
    }

    private void init(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, simpleOnScaleGestureListener);
        gestureDetector = new GestureDetector(context,simpleOnGestureListener);
        Log.d(TAG, "init");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        setImageMatrix(matrix);
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private ScaleGestureDetector.SimpleOnScaleGestureListener simpleOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d(TAG, "onScale: " + detector.getScaleFactor());
            float newscaleFactor = scaleFactor * detector.getScaleFactor();
            if (newscaleFactor > SCALE_MAX || newscaleFactor < SCALE_MIN) {
              return false;
            }
            scaleFactor = newscaleFactor;
            Log.d(TAG, "scaleFactor=" + String.valueOf(scaleFactor));
            setScaleY(scaleFactor);
            setScaleX(scaleFactor);
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            Log.d(TAG, "onScaleBegin: " + detector.getScaleFactor());
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d(TAG, "onScaleEnd: " + detector.getScaleFactor());
            super.onScaleEnd(detector);
        }

    };

    private final GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll x=" + String.valueOf(distanceX) + ", y=" + String.valueOf(distanceY));
            // Viewの位置
            float preX = getTranslationX();
            float preY = getTranslationY();

            // 移動距離を算出
            float x = preX - distanceX * PINCH_SENSITIVITY;
            float y = preY - distanceY * PINCH_SENSITIVITY;

            Log.d(TAG, "Translation x=" + String.valueOf(x) + ", y=" + String.valueOf(y));
            // 位置の再設定
            setTranslationY(y);
            setTranslationX(x);

            //再描画
            invalidate();

            return super.onScroll(event1, event2, distanceX, distanceY);
        }
    };
}
