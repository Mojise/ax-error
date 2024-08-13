package com.mojise.library.ax_error

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.mojise.library.ax_error.AxErrorActivity.Companion.EXTRA_ERROR_DATA
import com.mojise.library.ax_error.AxErrorActivity.Companion.EXTRA_INTENT_ARRAY
import java.lang.Thread.UncaughtExceptionHandler
import java.util.Deque

/**
 * AxError 라이브러리 사용을 위한 object 클래스.
 *
 * @see AxError.Executor
 * @see AxError.FullScreenBuilder
 */
object AxError {

    internal const val TAG = "AxError"

    internal object GlobalOption {
        internal var isHelpButtonVisible: Boolean = false
        internal var isErrorLogMessageVisible: Boolean = false
        internal var onHelpButtonClickedListener: OnHelpButtonClickedListener? = null
    }

    /**
     * - AxError 라이브러리의 Global Exception Handler 등록 및 설정을 위한 Executor.
     * - 앱 전역에서 발생하는 Exception을 처리하고, 안드로이드 시스템 앱 중지 팝업 대신 에러 화면([AxErrorActivity])을 보여주는 기능을 제공함.
     * - [Application.onCreate]에서 생성해서 실행하기.
     *
     * **Kotlin**
     * ```kotlin
     * class KotlinApplication : Application() {
     *
     *     override fun onCreate() {
     *         super.onCreate()
     *
     *         // KakaoSdk.init(this, FakeBuildConfig.KAKAO_SDK_APP_KEY)
     *
     *         AxError.Executor(this)
     *             .isHelpButtonVisible(false)
     *             .isErrorLogMessageVisible(BuildConfig.DEBUG)
     *             .setOnHelpButtonClickedListener { context ->
     *                 // ex) KakaoUtil.openKakaoChannel(context, BuildConfig.KAKAO_CHANNEL_PUBLIC_ID)
     *             }
     *             .registerGlobalExceptionHandler()
     *             .execute()
     *     }
     * }
     * ```
     *
     * **Java**
     * ```java
     * public class JavaApplication extends Application {
     *
     *     @Override
     *     public void onCreate() {
     *         super.onCreate();
     *
     *         // KakaoSdk.init(this, FakeBuildConfig.KAKAO_SDK_APP_KEY);
     *
     *         new AxError.Executor(this)
     *                 .isHelpButtonVisible(true)
     *                 .isErrorLogMessageVisible(BuildConfig.DEBUG)
     *                 .setOnHelpButtonClickedListener((context) -> {
     *                     // ex) KakaoUtil.openKakaoChannel(context, BuildConfig.KAKAO_CHANNEL_PUBLIC_ID);
     *                 })
     *                 .registerGlobalExceptionHandler()
     *                 .execute();
     *     }
     * }
     * ```
     *
     * @see AxGlobalExceptionHandler
     * @see AxErrorActivity
     */
    class Executor(private val application: Application) {

        private var exceptionHandler: UncaughtExceptionHandler? = null

        /** 에러 화면([AxErrorActivity]) 내의 "문의하기" 버튼 표시 여부 (기본값: false) */
        fun isHelpButtonVisible(isVisible: Boolean) = apply {
            GlobalOption.isHelpButtonVisible = isVisible
        }

        /** 에러 화면([AxErrorActivity]) 내의 에러 로그 메시지 표시 여부 (기본값: false) */
        fun isErrorLogMessageVisible(isVisible: Boolean) = apply {
            GlobalOption.isErrorLogMessageVisible = isVisible
        }

        /** 에러 화면([AxErrorActivity]) 내의 "문의하기" 버튼 클릭 리스너 설정 */
        fun setOnHelpButtonClickedListener(listener: OnHelpButtonClickedListener) = apply {
            GlobalOption.onHelpButtonClickedListener = listener
        }

        /** 앱 전역에서 발생하는 Exception 핸들러 등록 */
        fun registerGlobalExceptionHandler() = apply {
            exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()?.let { crashlyticsExceptionHandler ->
                AxGlobalExceptionHandler(application, crashlyticsExceptionHandler)
            }
        }

        /** [AxGlobalExceptionHandler] 실행 */
        fun execute() {
            exceptionHandler?.let(Thread::setDefaultUncaughtExceptionHandler)
        }
    }

    /**
     * Full Screen 에러 화면 ([AxErrorActivity])을 띄우기 위한 빌더 클래스
     *
     * **Kotlin**
     * ```kotlin
     * AxError.FullScreenBuilder(context)
     *    .errorTitle(R.string.error_title)
     *    .errorSubTitle(R.string.error_sub_title)
     *    .throwable(throwable)
     *    .start()
     * ```
     * **Java**
     * ```java
     * new AxError.FullScreenBuilder(context)
     *         .errorTitle(R.string.error_title)
     *         .errorSubTitle(R.string.error_sub_title)
     *         .throwable(e)
     *         .start();
     * ```
     */
    class FullScreenBuilder(private val context: Context) {

        private var uiData: AxErrorActivityUiData = AxErrorActivityUiData.getDefault(context)

        /** 에러 화면 제목 */
        fun errorTitle(errorTitle: String) = apply { uiData = uiData.copy(errorTitle = errorTitle) }

        /** 에러 화면 제목 */
        fun errorTitle(@StringRes resId: Int) = apply { uiData = uiData.copy(errorTitle = context.getString(resId)) }

        /** 에러 화면 부제목 */
        fun errorSubTitle(errorSubTitle: String) = apply { uiData = uiData.copy(errorSubTitle = errorSubTitle) }

        /** 에러 화면 부제목 */
        fun errorSubTitle(@StringRes resId: Int) = apply { uiData = uiData.copy(errorSubTitle = context.getString(resId)) }

        /** 에러 화면 로그 텍스트의 [Throwable] */
        fun throwable(throwable: Throwable?) = apply { uiData = uiData.copy(throwable = throwable) }

        /** 에러 액티비티 실행 */
        fun start() {
            val intent = Intent(context, AxErrorActivity::class.java).apply {
                putExtra(EXTRA_ERROR_DATA, uiData)
            }
            context.startActivity(intent)
        }

        internal fun startFromExceptionHandler(
            activityStack: Deque<Activity>,
            throwable: Throwable,
        ) {
            uiData = uiData.copy(throwable = throwable)

            val activityStackIntentArray = activityStack
                .map(Activity::getIntent)
                .reversed()
                .toTypedArray()

            val intent = Intent(context, AxErrorActivity::class.java).apply {
                putExtra(EXTRA_INTENT_ARRAY, activityStackIntentArray)
                putExtra(EXTRA_ERROR_DATA, uiData)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
    }
}