package com.hitdev01.cordova.plugin.sanada;

import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Environment;
import android.os.Bundle;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Camera.PreviewCallback;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Log;
import android.text.format.DateFormat;
import android.net.Uri;
import android.app.Fragment;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Button;

import java.util.List;

/**
 * Camera Fragment
 */
public class CameraFragment extends Fragment {
    public interface BackButtonListener extends OnClickListener {
        public void onClick(View v);
    }
    public interface ShootButtonListener extends OnClickListener {
        public void onClick(View v);
    }
    private BackButtonListener mBackButtonListener;
    private ShootButtonListener mShootButtonListener;

    private String TAG = "SanadaCameraFragment";
    private Camera mCamera;
    private View mView;
    private SurfaceView mPreviewView;
    private SanadaImageView mSanadaView;
    private Button mShootButton;
    private Button mBackButton;

    private Bitmap mPreviewBitmap;
    private Uri mLastCaptureUri;

    private String appResourcesPackage;

    public float scaleX;
    public float scaleY;
    public float rotation;
    public float x;
    public float y;

    public CameraFragment() {
    }

    public void setPictureSize(float x, float y, float scaleX, float scaleY, float rotation) {
      Log.d(TAG, String.format("setRect x=%s, y=%s, scaleX=%s, scaleY=%s, ratation=%s", x, y, scaleX, scaleY, rotation));

      this.x = x;
      this.y = y;
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.rotation = rotation;
    }

    public void setBackButtonListener(BackButtonListener backButtonListener) {
        this.mBackButtonListener = backButtonListener;
    }

    public void setShootButtonListener(ShootButtonListener shootButtonListener) {
        this.mShootButtonListener = shootButtonListener;
    }

    public Uri getLastCaptureImageURI() {
      return this.mLastCaptureUri;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        mBackButtonListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
      Log.d(TAG, "onCreateView");
      // layoutからView取得
      appResourcesPackage = getActivity().getPackageName();
      mView = inflater.inflate(getResources().getIdentifier("camera_fragment", "layout", appResourcesPackage), container, false);
      mPreviewView = (SurfaceView) mView.findViewById(getResources().getIdentifier("surface_view", "id", appResourcesPackage));
      mSanadaView = (SanadaImageView) mView.findViewById(getResources().getIdentifier("sanada_view", "id", appResourcesPackage));
      mShootButton = (Button) mView.findViewById(getResources().getIdentifier("shoot_button", "id", appResourcesPackage));
      mBackButton = (Button) mView.findViewById(getResources().getIdentifier("back_button", "id", appResourcesPackage));

      SurfaceHolder holder = mPreviewView.getHolder();
      holder.addCallback(surfaceCallback);
      holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
      mView.setOnTouchListener(touchListener);
      mShootButton.setOnClickListener(shootOnClickListener);
      mBackButton.setOnClickListener(backOnClickListener);

      return mView;
    }


    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
          Log.d(TAG, "surfaceCallback#surfaceCreated");
          try {
                Log.d(TAG, "surfaceCallback#camera open");
                int cameraId = 0;
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90); // ポートレート用に90度回転
                mCamera.setPreviewDisplay(holder);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {
          Log.d(TAG, "surfaceCallback#surfaceChanged");
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
                Camera.Size previewSize = previewSizes.get(0);
                Log.d(TAG, "previewSize width=" + String.valueOf(previewSize.width) + ", height=" + String.valueOf(previewSize.height));
                parameters.setPreviewSize(previewSize.width, previewSize.height);

                mCamera.setParameters(parameters);
                mCamera.setPreviewCallback(previewCallback);
                mCamera.startPreview();
            } catch (Exception e) {
              Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCallback#surfaceDestroyed");
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    };

    private PreviewCallback previewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
          // 読み込む範囲
          int previewWidth = camera.getParameters().getPreviewSize().width;
          int previewHeight = camera.getParameters().getPreviewSize().height;

          // プレビューデータから Bitmap を生成
          mPreviewBitmap = getBitmapImageFromYUV(data, previewWidth, previewHeight);
        }

        public Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {
            try {
                // プレビューデータからBitmap生成
                YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
                byte[] jdata = baos.toByteArray();
                BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
                bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap orgBmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
                // ポートレート用に90度回転
                Matrix rotMat = new Matrix();
                rotMat.postRotate(90);
                Bitmap bmp = Bitmap.createBitmap(orgBmp, 0, 0, width, height, rotMat, true);
                return bmp;
            } catch (Exception e) {
              Log.e(TAG, e.getLocalizedMessage(), e);
            }
            return null;
        }
    };


    // シャッターが押されたときに呼ばれるコールバック
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
          Log.d(TAG, "shutterCallback#onShutter");
        }
    };

    // 写真撮影後に呼ばれるコールバック
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
          Log.d(TAG, "pictureCallback#onPictureTaken");
            if (data != null) {
                // プレビュー再開
                camera.startPreview();
            }
        }
    };

    // AF完了時のコールバック
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.d(TAG, "autoFocusCallback#onAutoFocus");
            camera.autoFocus(null);
        }
    };

    // 画面タッチイベントリスナー
    OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "touchListener#onTouch");
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mCamera != null) {
                  mCamera.autoFocus(autoFocusCallback);
                }
            }
            return false;
        }
    };

    // 戻るボタンクリックイベントリスナー
    OnClickListener backOnClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        // 2度押し防止
          mBackButton.setOnClickListener(null);
          mBackButtonListener.onClick(v);
      }
    };

    // 撮影ボタンクリックイベントリスナー
    OnClickListener shootOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // 2度押し防止
            mShootButton.setOnClickListener(null);
            // 画面イメージキャプチャ
            captureImage();
            // シャッター音鳴らすだけ
            mCamera.takePicture(shutterCallback, null, pictureCallback);

            mShootButtonListener.onClick(v);
        }
    };


    /**
    * 合成イメージ出力
    */
    private void captureImage() {
      Log.d(TAG, "captureImage");
      Bitmap newBitmap = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(), Bitmap.Config.ARGB_8888);
      try {
          Canvas drawCanvas = new Canvas(newBitmap);

          // Preview View の bitmapを取得
          Log.d(TAG, "captureImage get mPreviewView bitmap: " + String.valueOf(mPreviewBitmap.getByteCount()));

          // Sanada View の bitmapを取得
          mSanadaView.setDrawingCacheEnabled(true);
          Bitmap sanadaBitmapCache = mSanadaView.getDrawingCache();
          Bitmap sanadaBitmap = Bitmap.createBitmap(sanadaBitmapCache);
          mSanadaView.setDrawingCacheEnabled(false);
          Log.d(TAG, "captureImage get mSanadaView bitmap: " + String.valueOf(sanadaBitmap.getByteCount()));

          // Viewを合成
          drawCanvas.drawBitmap(mPreviewBitmap, mPreviewView.getMatrix(), null);
          drawCanvas.drawBitmap(sanadaBitmap, mSanadaView.getMatrix(), null);
      } catch (Exception e) {
          Log.e(TAG, e.getLocalizedMessage(), e);
      }

      // ディレクトリ作成
      String dirPath = Environment.getExternalStorageDirectory().getPath() + "/sanadaimage"; //外部SDに保存
      File dir = new File(dirPath);
      if (!dir.exists()) {
        dir.mkdirs();
      }
      // ファイル出力
      String filePath = "sanada" + DateFormat.format("yyyy-MM-dd_kkmmss", System.currentTimeMillis()).toString() + ".jpg";
      File file = new File(dirPath, filePath);
      Log.d(TAG, "captureImage output file:" + file.getAbsolutePath());
      this.mLastCaptureUri = Uri.fromFile(file);

      OutputStream outputStream = null;
      try {
          outputStream = new FileOutputStream(file);
          newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
      } catch (Exception e) {
        Log.e(TAG, e.getLocalizedMessage(), e);
      } finally {
          if (outputStream != null) {
              try {
                  outputStream.close();
              } catch (IOException e) {
                  Log.e(TAG, e.getLocalizedMessage(), e);
              }
          }
      }
    }


}
