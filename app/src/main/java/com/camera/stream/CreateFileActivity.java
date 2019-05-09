package com.camera.stream;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class CreateFileActivity extends AppCompatActivity {

    private final int REQUESTCODE = 101;
    private String fileName = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_file);

        findViewById(R.id.btn_main_createFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create(fileName);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTCODE) {
            //询问用户权限
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                //用户同意
            } else {
                //用户不同意
            }
        }
    }
    public void create(String fileName){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(checkSelfPermission == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUESTCODE);
            }
        }
        //Environment.getExternalStorageDirectory().getAbsolutePath():SD卡根目录
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+fileName);
        if (!file.exists()){
            boolean isSuccess = file.mkdirs();
            if(isSuccess){
                Toast.makeText(CreateFileActivity.this,"文件夹创建成功",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(CreateFileActivity.this,"文件夹创建失败",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(CreateFileActivity.this,"文件夹已存在",Toast.LENGTH_LONG).show();
        }
    }

}
