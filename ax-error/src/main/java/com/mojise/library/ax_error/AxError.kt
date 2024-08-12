package com.mojise.library.ax_error

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.mojise.library.ax_error.AxErrorActivity.Companion.EXTRA_ERROR_DATA
import com.mojise.library.ax_error.AxErrorActivity.Companion.EXTRA_INTENT_ARRAY
import java.util.Deque

/**
 * - AxError 라이브러리의 Global Exception Handler 등록 및 설정을 위한 객체
 * - 앱 전역에서 발생하는 Exception을 처리하고, 에러 화면을 보여주는 기능을 제공함
 *
 * **Kotlin**
 * ```kotlin
 * class KotlinApplication : Application() {
 *
 *     override fun onCreate() {
 *         super.onCreate()
 *
 *         // KakaoSdk.init(this, BuildConfig.KAKAO_SDK_APP_KEY)
 *
 *         // 앱 전역에 에러 핸들러를 등록
 *         AxError.registerAxGlobalExceptionHandler(this)
 *         // 에러 화면 내의 "문의하기" 버튼 표시하기 및 버튼 클릭 리스너 설정.
 *         AxError.setHelpButtonShowingAndOnClickedListener { context ->
 *             // ex) KakaoUtil.openKakaoChannel(context, BuildConfig.KAKAO_CHANNEL_PUBLIC_ID)
 *         }
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
 *         // 앱 전역에 에러 핸들러를 등록
 *         AxError.registerAxGlobalExceptionHandler(this);
 *         // 에러 화면 내의 "문의하기" 버튼 표시하기 및 버튼 클릭 리스너 설정.
 *         AxError.setHelpButtonShowingAndOnClickedListener((context) -> {
 *             // ex) KakaoUtil.openKakaoChannel(context, BuildConfig.KAKAO_CHANNEL_PUBLIC_ID);
 *         });
 *     }
 * }
 * ```
 */
object AxError {

    internal const val TAG = "AxError"

    internal var onHelpButtonClickedListener: OnHelpButtonClickedListener? = null

    internal val isHelpButtonVisible: Boolean
        get() = onHelpButtonClickedListener != null

    /**
     * - [AxGlobalExceptionHandler]를 [Thread]의 기본 [Thread.UncaughtExceptionHandler]로 등록
     * - [Application.onCreate]에서 `registerAxExceptionHandler()` 함수를 호출하여 앱 전역에서 발생하는 Exception을 처리함
     * - 앱 실행 중 Exception이 발생 시, 앱을 종료하지 않고 에러 화면([AxErrorActivity])을 보여줌
     */
    @JvmStatic
    fun registerAxGlobalExceptionHandler(application: Application) {
        val crashlyticsExceptionHandler = Thread.getDefaultUncaughtExceptionHandler() ?: return
        val exceptionHandler = AxGlobalExceptionHandler(application, crashlyticsExceptionHandler)

        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler)
    }

    /**
     * - 에러 화면 내의 "문의하기" 버튼 클릭 리스너 설정.
     * - "문의하기" 버튼을 표시하려면 [setHelpButtonShowingAndOnClickedListener] 함수를 호출하여 리스너를 설정해야 함.
     */
    @JvmStatic
    fun setHelpButtonShowingAndOnClickedListener(onHelpButtonClickedListener: OnHelpButtonClickedListener) {
        this.onHelpButtonClickedListener = onHelpButtonClickedListener
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