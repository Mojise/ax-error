package com.mojise.library.ax_error.app;

import android.app.Application;

import com.mojise.library.ax_error.AxError;

public class AxErrorExampleApplicationJava extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // KakaoSdk.init(this, FakeBuildConfig.KAKAO_SDK_APP_KEY);

        // 앱 전역에 에러 핸들러를 등록
        AxError.registerAxGlobalExceptionHandler(this);
        // 에러 화면 내의 "문의하기" 버튼 표시하기 및 버튼 클릭 리스너 설정.
        AxError.setHelpButtonShowingAndOnClickedListener((context) -> {
            // ex) KakaoUtil.openKakaoChannel(context, BuildConfig.KAKAO_CHANNEL_PUBLIC_ID);
        });
    }
}