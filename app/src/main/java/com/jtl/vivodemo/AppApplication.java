package com.jtl.vivodemo;

import android.app.Application;
import android.content.Context;

public class AppApplication extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
