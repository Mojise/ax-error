package com.mojise.library.ax_error

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Process
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import java.util.ArrayDeque
import java.util.Deque

/**
 * - [AxExceptionHandler]를 [Thread]의 기본 [Thread.UncaughtExceptionHandler]로 등록
 * - [Application.onCreate]에서 `registerAxExceptionHandler()` 함수를 호출하여 앱 전역에서 발생하는 Exception을 처리함
 * - 앱 실행 중 Exception이 발생 시, 앱을 종료하지 않고 에러 화면([AxErrorActivity])을 보여줌
 *
 * case 1. 문의하기 버튼을 숨길 경우
 *  ```kotlin
 *  class MyApplication : Application {
 *      fun onCreate() {
 *          registerAxExceptionHandler()
 *      }
 *  }
 *  ```
 *
 * case 2. 문의하기 버튼을 표시할 경우
 *  ```kotlin
 *  class MyApplication : Application {
 *      fun onCreate() {
 *          KakaoSdk.init(this, "${NATIVE_APP_KEY}")
 *          registerAxExceptionHandler("${KAKAO_PUBLIC_CHANNEL_ID}")
 *      }
 *  }
 *  ```
 * @param kakaoPublicChannelId 카카오톡 플러스 친구 채널 아이디. (null이면 [AxErrorActivity] 화면 내의 "문의하기" 버튼을 표시하지 않음)
 *
 */
fun Application.registerAxExceptionHandler(
    kakaoPublicChannelId: String? = null,
) {
    if (kakaoPublicChannelId != null) {
        try {
            KakaoSdk.appKey.isBlank()
        } catch (e: UninitializedPropertyAccessException) {
            throw IllegalStateException("KakaoSdk.init() 을 먼저 호출해야 합니다!!")
        }
    }

    AxErrorActivity.GlobalOption.kakaoPublicChannelIdForHelpButton = kakaoPublicChannelId

    val crashlyticsExceptionHandler = Thread.getDefaultUncaughtExceptionHandler() ?: return
    val axExceptionHandler = AxExceptionHandler(this, crashlyticsExceptionHandler)

    Thread.setDefaultUncaughtExceptionHandler(axExceptionHandler)
}

/**
 * UnCaught Thread Exception 핸들러 (Thread에서 캐치되지 않은 Exception 핸들러)
 *
 * @see <a href="https://medium.com/prnd/%EC%95%84%EB%A6%84%EB%8B%B5%EA%B2%8C-%EC%95%B1-%EC%98%A4%EB%A5%98-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0-8bf9a46df515">아름답게 안드로이드 앱 오류 처리하기</a>
 */
internal class AxExceptionHandler(
    private val application: Application,
    private val crashlyticsExceptionHandler: Thread.UncaughtExceptionHandler,
) : Thread.UncaughtExceptionHandler, Application.ActivityLifecycleCallbacks {

    private val activityStack: Deque<Activity> = ArrayDeque()

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isSkipActivity(activity).not()) {
            activityStack.push(activity)
        }
        logLifecycle(activity, "Created")
    }

    override fun onActivityStarted(activity: Activity) {
        logLifecycle(activity, "Started")
    }

    override fun onActivityResumed(activity: Activity) {
        logLifecycle(activity, "Resumed")
    }

    override fun onActivityPaused(activity: Activity) {
        logLifecycle(activity, "Paused")
    }

    override fun onActivityStopped(activity: Activity) {
        logLifecycle(activity, "Stopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        logLifecycle(activity, "SaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (isSkipActivity(activity).not()) {
            activityStack.remove(activity)
        }
        logLifecycle(activity, "Destroyed")
    }

    private fun isSkipActivity(activity: Activity) = activity is AxErrorActivity

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e(TAG, "uncaughtException: ProcessId=${Process.myPid()}")

        startErrorActivity(throwable)

        crashlyticsExceptionHandler.uncaughtException(thread, throwable)
        // Process.killProcess(Process.myPid())
        // System.exit(-1)
    }

    private fun startErrorActivity(throwable: Throwable) {
        AxErrorActivity.Builder(application)
            .startFromExceptionHandler(activityStack, throwable)
    }

    private fun logLifecycle(activity: Activity, state: String) {
        if (BuildConfig.DEBUG.not()) {
            return
        }

        val logMessage = buildString {
            val spacing = " ".repeat("SaveInstanceState".length - state.length)

            appendLine("[pid=${Process.myPid()} | onActivity ($state)]$spacing:: $activity")

            if (state == "Created" || state == "Destroyed") {
                activityStack.forEachIndexed { index, activity ->
                    appendLine("    → Stack[${activityStack.size - (index + 1)}]=$activity")
                }
            }
        }

        if (isSkipActivity(activity)) {
            Log.e(TAG, logMessage)
        } else {
            Log.i(TAG, logMessage)
        }
    }

    companion object {
        private const val TAG = AxErrorConstants.TAG
    }
}