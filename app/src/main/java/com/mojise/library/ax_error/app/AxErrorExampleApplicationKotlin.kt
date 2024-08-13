package com.mojise.library.ax_error.app

import android.app.Application
import com.mojise.library.ax_error.AxError
import com.mojise.library.ax_error.BuildConfig

class AxErrorExampleApplicationKotlin : Application() {

    override fun onCreate() {
        super.onCreate()

        // KakaoSdk.init(this, FakeBuildConfig.KAKAO_SDK_APP_KEY)

        AxError.Executor(this)
            .registerGlobalExceptionHandler()
            .execute()

//        AxError.FullScreenBuilder(this)
//            .errorTitle(R.string.error_title)
//            .errorSubTitle(R.string.error_sub_title)
//            .throwable(throwable)
//            .start()
    }
}

object FakeBuildConfig {
    const val KAKAO_SDK_APP_KEY = "kakao_sdk_app_key"
    const val KAKAO_CHANNEL_PUBLIC_ID = "kakao_channel_public_id"
}