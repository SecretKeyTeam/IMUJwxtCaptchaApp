package com.webprague.lib.imu_jwxt_captcha;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 教务系统验证码破解类
 * 很久不写Java了。。。
 * 以下是核心类
 * 可能存在内存泄露
 * @version 0.2
 * @author hupeng
 * */
public class IMUJwxtCaptchaCracker {
    private Context context;
    private Module module = null;

    public IMUJwxtCaptchaCracker(Context context){
        this.context = context;
        try {
            module = Module.load(assetFilePath(this.context, "model.pt"));
        } catch (IOException e) {
            Log.e("IMUJwxtCaptchaCracker", "Error reading assets", e);
        }
    }

    public String crack(Bitmap bitmap){
        String ans = "";
        try {
            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                 new float[]{0.0f, 0.0f, 0.0f}, new float[]{1.0f, 1.0f, 1.0f});
            Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
            float farr[] = outputTensor.getDataAsFloatArray();
            Log.i("vvv", farr.length+"");
            for(int i=0; i<4; i++){
                float maxVal = farr[i*36];
                int maxIdx=0;
                for (int j=0; j<36; j++){
                    if (farr[i*36+j] > maxVal){
                        maxIdx = j;
                        maxVal = farr[i*36+j];
                    }
                }
                if (maxIdx < 10){
                    ans += (char)(maxIdx+'0');
                }else {
                    ans += (char)(maxIdx-10+'a');
                }
            }
        }catch (Exception e){
            Log.e("IMUJwxtCaptchaCracker", "发生错误，请自检代码，依然无法解决的话，提issue到GitHub仓库", e);
        }
        return ans;
    }

    private static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
//        if (file.exists() && file.length() > 0) {
//            return file.getAbsolutePath();
//        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }
        return file.getAbsolutePath();
    }
  }


}
