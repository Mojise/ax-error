package com.mojise.library.ax_error.app;

import android.app.Application;

import com.mojise.library.ax_error.AxError;

public class AxErrorExampleApplicationJava extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // KakaoSdk.init(this, FakeBuildConfig.KAKAO_SDK_APP_KEY);

        new AxError.Executor(this)
                .execute();


    }
}