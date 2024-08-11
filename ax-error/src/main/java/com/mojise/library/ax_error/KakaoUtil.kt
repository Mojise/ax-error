package com.mojise.library.ax_error

import android.content.ActivityNotFoundException
import android.content.Context
import android.widget.Toast
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.talk.TalkApiClient

internal object KakaoUtil {

    fun initKakaoSdk(context: Context, appKey: String) {
        KakaoSdk.init(context, appKey)
    }

    fun openKakaoChannel(context: Context, publicChannelId: String) {
        val url = TalkApiClient.instance.channelChatUrl(publicChannelId)

        try {
            // CustomTabs 로 열기
            KakaoCustomTabsClient.openWithDefault(context, url)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "디바이스에 설치된 인터넷 브라우저가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
}