package com.example.signatureprotect;

import android.app.Application;
import android.util.Log;

/**
 * Created by chang on 2019/1/16.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("signatureprotect", "source apk oncreate");
    }
}
