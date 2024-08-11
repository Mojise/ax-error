package com.mojise.library.ax_error

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.core.content.IntentCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.mojise.library.ax_error.databinding.ActivityAxErrorBinding
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter
import java.util.Deque

class AxErrorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAxErrorBinding

    private val activityIntents: List<Intent>? by lazy {
        IntentCompat
            .getParcelableArrayExtra(intent, EXTRA_INTENT_ARRAY, Intent::class.java)
            ?.filterIsInstance<Intent>()
    }

    private val uiData: UiData by lazy {
        IntentCompat.getSerializableExtra(intent, EXTRA_ERROR_DATA, UiData::class.java)
            ?: UiData.getDefault(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log.e(TAG, "onCreate: ProcessId=${Process.myPid()}")
        // activityIntents?.forEach { Log.e(TAG, "onCreate :: activityIntents=$it") }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ax_error)
        binding.lifecycleOwner = this

        initViewAndListeners()
    }

    private fun initViewAndListeners() {
        with (binding) {
            scrollViewErrorLogText.isVisible = BuildConfig.DEBUG
            llCopyButtonAndGuideMessage.isVisible = BuildConfig.DEBUG

            errorTitle.text = uiData.errorTitle.toStringFromHtml()
            errorSubTitle.text = uiData.errorSubTitle.toStringFromHtml()
            errorLogText.setText(uiData.throwable.toErrorLogText())

            copyButtonGuideMessage.text =
                R.string.copy_button_guide_message.toStringFromHtml()

            btnCopyErrorLog.setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("errorLog", uiData.throwable.toErrorLogText())
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this@AxErrorActivity, R.string.toast_copied, Toast.LENGTH_SHORT).show()
            }
            btnRefresh.setOnClickListener {
                finishAndGoBackToPreviousActivity()
            }

            textHelpLeft.isVisible = GlobalOption.isHelpButtonVisible
            btnHelp.isVisible = GlobalOption.isHelpButtonVisible
            val kakaoPublicChannelId = GlobalOption.kakaoPublicChannelIdForHelpButton
            if (kakaoPublicChannelId != null) {
                btnHelp.setOnClickListener {
                    KakaoUtil.openKakaoChannel(this@AxErrorActivity, kakaoPublicChannelId)
                }
            }
        }

        onBackPressedDispatcher.addCallback(owner = this, enabled = true) {
            finishAndGoBackToPreviousActivity()
        }
    }

    private fun finishAndGoBackToPreviousActivity() {
        activityIntents?.let {
            val stackBuilder = TaskStackBuilder.create(this).apply {
                it.forEach { intent ->
                    addNextIntent(intent)
                }
            }
            stackBuilder.startActivities()
        }
        finish()
    }

    private fun finishAndGoBackToPreviousActivity2() {
//        lifecycleScope.launch {
//            activityIntents?.forEach {
//                startActivity(it)
//                delay(1L)
//            }
//            finish()
//        }
    }

    private fun Int.toStringFromHtml(): Spanned {
        return HtmlCompat.fromHtml(getString(this), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun String.toStringFromHtml(): Spanned {
        return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun Throwable?.toErrorLogText(): String {
        if (this == null) return ""
        val stringWriter = StringWriter()
        this.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()
    }

    private data class UiData(
        val errorTitle: String,
        val errorSubTitle: String,
        val throwable: Throwable?,
    ) : Serializable {

        companion object {
            fun getDefault(context: Context) = UiData(
                errorTitle = context.getString(R.string.error_title),
                errorSubTitle = context.getString(R.string.error_sub_title),
                throwable = null,
            )
        }
    }

    class Builder(private val context: Context) {

        private var uiData: UiData = UiData.getDefault(context)

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

    internal object GlobalOption {
        /**
         * - [AxErrorActivity] 화면 내의 "문의하기" 버튼
         * - 리스너 설정 여부에 따라 "문의하기" 버튼의 표시 여부가 결정됨.
         */
        internal var kakaoPublicChannelIdForHelpButton: String? = null

        internal val isHelpButtonVisible: Boolean
            get() = kakaoPublicChannelIdForHelpButton != null
    }

    companion object {
        private const val TAG = AxErrorConstants.TAG
        private const val EXTRA_INTENT_ARRAY = "EXTRA_INTENT_ARRAY"
        private const val EXTRA_ERROR_DATA = "EXTRA_ERROR_DATA"
    }
}