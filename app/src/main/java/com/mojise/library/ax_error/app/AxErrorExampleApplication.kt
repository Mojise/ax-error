package com.mojise.library.ax_error.app

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.mojise.library.ax_error.registerAxExceptionHandler

class AxErrorExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_SDK_APP_KEY)

        // 앱 전역에 에러 핸들러를 등록
        registerAxExceptionHandler(
            kakaoPublicChannelId = BuildConfig.KAKAO_CHANNEL_PUBLIC_ID
        )
    }
}