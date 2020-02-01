package com.webprague.app.testapp;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.webprague.lib.imu_jwxt_captcha.IMUJwxtCaptchaCracker;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivCaptcha;
    private Button btnLoad;
    private EditText etPredict;
    private Button btnPredict;
    private Bitmap bitmap = null;
    private IMUJwxtCaptchaCracker captchaCracker;

    private void initPermissions(){
        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(
                                Manifest.permission.INTERNET)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Toast.makeText(MainActivity.this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                        btnLoad.setEnabled(false);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivCaptcha = findViewById(R.id.iv_captcha);
        btnLoad = findViewById(R.id.btn_load);
        etPredict = findViewById(R.id.et_predict);
        btnPredict = findViewById(R.id.btn_predict);
        initPermissions();
        captchaCracker = new IMUJwxtCaptchaCracker(this);
        btnLoad.setOnClickListener(this);
        btnPredict.setOnClickListener(this);
    }
    private OkHttpClient client = new OkHttpClient();

    private void loadCaptcha(){
        btnLoad.setEnabled(false);
        btnLoad.setText("加载中...");
        Request request = new Request.Builder().url("http://jwxt.imu.edu.cn/img/captcha.jpg").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnLoad.setEnabled(true);
                        btnLoad.setText("加载图片");
                        Toast.makeText(MainActivity.this, "网络请求失败，请检查", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();//得到图片的流
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                MainActivity.this.bitmap = bitmap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnLoad.setEnabled(true);
                        btnLoad.setText("加载图片");
                        ivCaptcha.setImageBitmap(bitmap);
                    }
                });
            }
        });

    }

    private void predict(){
        if (bitmap == null){
            Toast.makeText(this, "请先加载图片", Toast.LENGTH_SHORT).show();
        }else {
            String ans = captchaCracker.crack(bitmap);
            etPredict.setText(ans);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_load:
                loadCaptcha();
                break;
            case R.id.btn_predict:
                predict();
                break;
        }
    }
}
