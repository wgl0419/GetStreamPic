package com.camera.stream;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    SurfaceView mPreview;
    SurfaceHolder mHolder;
    Camera mcamera;
    int screenWidth, screenHeight;
    boolean isPreview = false; // 是否在浏览中
    int pic_name = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = findViewById(R.id.surface_view);
        screenHeight = 480;
        screenWidth = 640;
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        takePhone();
    }


    public void takePhone() {

        // check Android 6 permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (mcamera == null) {
                mcamera = getMcamera();
                if (mHolder != null) {
                    setStartPreview(mcamera, mHolder);
                }
            }
        }  else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    //在这里面添加了一些内容
    private Camera getMcamera() {
        Camera camera;
        try {
            camera = Camera.open();
            if (camera != null && !isPreview) {
                try {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewSize(screenWidth, screenHeight); // 设置预览照片的大小
                    parameters.setPreviewFpsRange(20, 30); // 每秒显示20~30帧
                    parameters.setPictureFormat(ImageFormat.NV21); // 设置图片格式
                    parameters.setPictureSize(screenWidth, screenHeight); // 设置照片的大小
                    parameters.setPreviewFrameRate(3);// 每秒3帧 每秒从摄像头里面获得3个画面,
                    // camera.setParameters(parameters); // android2.3.3以后不需要此行代码
//                    camera.setPreviewDisplay(mHolder); // 通过SurfaceView显示取景画面
                    camera.setPreviewCallback(new StreamIt()); // 设置回调的类
                    System.out.println("asasasasasasasasssssssssssssssssssssssssssss");
                    camera.startPreview(); // 开始预览
                    camera.autoFocus(null); // 自动对焦
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isPreview = true;
            }
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
            Toast.makeText(this, "无法获取前置摄像头", Toast.LENGTH_LONG);
        }
        return camera;
    }

    //我新添加的关键类   保存图片名称为 pic_name +".jpg";
    class StreamIt implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            try {
                // 调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
                YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                if (image != null) {

                    ByteArrayOutputStream outstream = new ByteArrayOutputStream(data.length);
                    image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, outstream);

                    byte[] tmp = outstream.toByteArray();
                    Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
                    String picture_name = pic_name + ".jpg";
                    System.out.println(picture_name);

                    saveBitmap(bmp, picture_name);

                    pic_name = pic_name + 1;
                    System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaassssssssssssssssssssssssssss");
                    outstream.flush();
                }
            } catch (Exception ex) {
                Log.e("Sys", "Error:" + ex.getMessage());
            }
        }
    }

    //新添加的保存到手机的方法
    @SuppressLint("SdCardPath")
    private void saveBitmap(Bitmap bitmap, String bitName) throws IOException {
        File appDir = new File(Environment.getExternalStorageDirectory(), "Images");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, bitName);     // 创建文件
        try {                                       // 写入图片
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
       开始预览相机内容
        */
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    释放相机资源
     */
    private void releaseCamera() {
        if (mcamera != null) {
            mcamera.setPreviewCallback(null);
            mcamera.stopPreview();
            mcamera.release();
            mcamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mcamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setStartPreview(mcamera, mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
