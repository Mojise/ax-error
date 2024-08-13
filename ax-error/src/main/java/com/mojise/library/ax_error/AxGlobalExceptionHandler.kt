package com.mojise.library.ax_error

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Process
import android.util.Log
import java.util.ArrayDeque
import java.util.Deque

/**
 * - UnCaught Thread Exception 핸들러 (Thread 에서 캐치되지 않은 Exception을 처리하는 핸들러)
 * - 앱 전역에서 발생하는 Exception을 처리하고, 안드로이드 시스템의 "앱 중지 팝업" 대신 "커스텀 에러 화면([AxErrorActivity])"을 보여주는 기능을 제공함.
 *
 * @see <a href="https://medium.com/prnd/%EC%95%84%EB%A6%84%EB%8B%B5%EA%B2%8C-%EC%95%B1-%EC%98%A4%EB%A5%98-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0-8bf9a46df515">(참고) 아름답게 안드로이드 앱 오류 처리하기</a>
 * @see Thread.UncaughtExceptionHandler
 * @see Application.ActivityLifecycleCallbacks
 */
internal class AxGlobalExceptionHandler(
    private val application: Application,
    private val crashlyticsExceptionHandler: Thread.UncaughtExceptionHandler,
) : Thread.UncaughtExceptionHandler, Application.ActivityLifecycleCallbacks {

    private val activityStack: Deque<Activity> = ArrayDeque()

    private val Activity.isErrorActivity: Boolean
        get() = this is AxErrorActivity

    /**
     * - 현재 활성화 된 AxErrorActivity 카운트
     * - AxErrorActivity 내에서 Exception이 발생할 경우, 무한 루프를 방지하기 위해 사용
     */
    private var errorActivityActivationCount = 0

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.isErrorActivity) {
            errorActivityActivationCount++
        } else {
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
        if (activity.isErrorActivity) {
            errorActivityActivationCount--
        } else {
            activityStack.remove(activity)
        }
        logLifecycle(activity, "Destroyed")
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e(TAG, "uncaughtException :: ProcessId=${Process.myPid()} | Thread=$thread")
        Log.e(TAG, "uncaughtException :: errorActivityCount=$errorActivityActivationCount")

        if (errorActivityActivationCount > 0) {
            Log.e(TAG, "uncaughtException :: Already showing error activity.")
        } else {
            startErrorActivity(throwable)
        }

        crashlyticsExceptionHandler.uncaughtException(thread, throwable)
        // Process.killProcess(Process.myPid())
        // System.exit(-1)
    }

    private fun startErrorActivity(throwable: Throwable) {
        AxError.FullScreenBuilder(application)
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

        if (activity.isErrorActivity) {
            Log.e(TAG, logMessage)
        } else {
            Log.i(TAG, logMessage)
        }
    }

    companion object {
        private const val TAG = AxError.TAG
    }
}