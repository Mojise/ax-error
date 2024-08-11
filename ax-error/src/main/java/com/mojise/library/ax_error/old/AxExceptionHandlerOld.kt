package com.mojise.library.ax_error.old

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Process

/**
 * @see <a href="https://medium.com/prnd/%EC%95%84%EB%A6%84%EB%8B%B5%EA%B2%8C-%EC%95%B1-%EC%98%A4%EB%A5%98-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0-8bf9a46df515">아름답게 안드로이드 앱 오류 처리하기</a>
 */
class AxExceptionHandlerOld(
    application: Application,
    private val crashlyticsExceptionHandler: Thread.UncaughtExceptionHandler,
) : Thread.UncaughtExceptionHandler {

    private var lastActivity: Activity? = null
    private var activityCount = 0

    init {
        application.registerActivityLifecycleCallbacks(
            object : SimpleActivityLifecycleCallbacksOld() {

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    if (isSkipActivity(activity)) {
                        return
                    }
                    lastActivity = activity
                }

                override fun onActivityStarted(activity: Activity) {
                    if (isSkipActivity(activity)) {
                        return
                    }
                    activityCount++
                    lastActivity = activity
                }

                override fun onActivityStopped(activity: Activity) {
                    if (isSkipActivity(activity)) {
                        return
                    }
                    activityCount--
                    if (activityCount < 0) {
                        lastActivity = null
                    }
                }
            })
    }

    private fun isSkipActivity(activity: Activity) = activity is AxErrorActivityOld

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        lastActivity?.let {
            startErrorActivity(it, throwable)
        }

        crashlyticsExceptionHandler.uncaughtException(thread, throwable)
        Process.killProcess(Process.myPid())
        System.exit(-1)
    }

    private fun startErrorActivity(activity: Activity, throwable: Throwable) {
        AxErrorActivityOld.from(activity)
            .throwable(throwable)
            .startFromExceptionHandler()

        activity.finish()
    }
}

fun Application.registerAxExceptionHandlerOld() {
    val crashlyticsExceptionHandler = Thread.getDefaultUncaughtExceptionHandler() ?: return
    Thread.setDefaultUncaughtExceptionHandler(
        AxExceptionHandlerOld(this, crashlyticsExceptionHandler)
    )
}