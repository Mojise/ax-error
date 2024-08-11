package com.mojise.library.ax_error.old

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.mojise.library.ax_error.BuildConfig
import com.mojise.library.ax_error.R
import com.mojise.library.ax_error.databinding.ActivityAxErrorBinding
import java.io.PrintWriter
import java.io.StringWriter


class AxErrorActivityOld : AppCompatActivity() {

    private lateinit var binding: ActivityAxErrorBinding

    private val lastActivityIntent: Intent? by lazy { IntentCompat.getParcelableExtra(intent, EXTRA_INTENT, Intent::class.java) }
    private val errorTitleString: String? by lazy { intent.getStringExtra(EXTRA_ERROR_TITLE) }
    private val errorSubTitleString: String? by lazy { intent.getStringExtra(EXTRA_ERROR_SUB_TITLE) }
    private val errorLogText: String? by lazy { intent.getStringExtra(EXTRA_ERROR_LOG_TEXT) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ax_error)
        binding.lifecycleOwner = this

        initViewAndListeners()
    }

    private fun initViewAndListeners() {
        with (binding) {
            errorTitle.text = errorTitleString?.toStringFromHtml()
                ?: R.string.error_title.toStringFromHtml()
            errorSubTitle.text = errorSubTitleString?.toStringFromHtml()
                ?: R.string.error_sub_title.toStringFromHtml()

            scrollViewErrorLogText.isVisible = BuildConfig.DEBUG
            llCopyButtonAndGuideMessage.isVisible = BuildConfig.DEBUG

            errorLogText.setText(this@AxErrorActivityOld.errorLogText)

            copyButtonGuideMessage.text = R.string.copy_button_guide_message.toStringFromHtml()

            btnCopyErrorLog.setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("errorLog", this@AxErrorActivityOld.errorLogText)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this@AxErrorActivityOld, R.string.toast_copied, Toast.LENGTH_SHORT).show()
            }
            btnRefresh.setOnClickListener {
                finishAndGoBackToPreviousActivity()
            }
            btnHelp.setOnClickListener {

            }
        }

        onBackPressedDispatcher.addCallback(owner = this, enabled = true) {
            finishAndGoBackToPreviousActivity()
        }
    }

    private fun finishAndGoBackToPreviousActivity() {
        Log.e(TAG, "finishAndGoBackToPreviousActivity :: lastActivityIntent=$lastActivityIntent$")

        if (lastActivityIntent != null) {
            startActivity(lastActivityIntent)
        }
        finish()
    }

    private fun Int.toStringFromHtml(): Spanned {
        return HtmlCompat.fromHtml(getString(this), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun String.toStringFromHtml(): Spanned {
        return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    class RequestManager internal constructor(private val activity: Activity) {

        private var errorTitle: String? = null
        private var errorSubTitle: String? = null
        private var throwable: Throwable? = null

        /** 에러 화면 제목 */
        fun errorTitle(errorTitle: String) = apply { this.errorTitle = errorTitle }

        /** 에러 화면 부제목 */
        fun errorSubTitle(errorSubTitle: String) = apply { this.errorSubTitle = errorSubTitle }

        /** 에러 화면 로그 텍스트의 [Throwable] */
        fun throwable(throwable: Throwable) = apply { this.throwable = throwable }

        /**
         * - 에러 화면 액티비티 실행.
         * - 일반적인 에러나 예외처리 할 때, 에러 내용을 사용자에게 UI로 보여줄 때 사용.
         */
        fun start() {
            val intent = Intent(activity, AxErrorActivityOld::class.java).apply {
                errorTitle?.let { putExtra(EXTRA_ERROR_TITLE, it) }
                errorSubTitle?.let { putExtra(EXTRA_ERROR_SUB_TITLE, it) }
                throwable?.let { putExtra(EXTRA_ERROR_LOG_TEXT, it.toErrorLogText()) }
            }
            activity.startActivity(intent)
        }

        /**
         * - 에러 화면 액티비티 실행.
         * - 앱 사용 도중 예상치 못한 에러가 발생했을 때, [Thread.UncaughtExceptionHandler]에 의해 자동으로 에러 화면을 보여줄 때 사용.
         */
        fun startFromExceptionHandler() {
            val intent = Intent(activity, AxErrorActivityOld::class.java).apply {
                putExtra(EXTRA_INTENT, activity.intent)
                putExtra(EXTRA_ERROR_LOG_TEXT, throwable?.toErrorLogText())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            }
            activity.startActivity(intent)
        }

        private fun Throwable.toErrorLogText(): String {
            val stringWriter = StringWriter()
            this.printStackTrace(PrintWriter(stringWriter))
            return stringWriter.toString()
        }
    }

    companion object {
        private const val TAG = "AxErrorActivity"

        private const val EXTRA_INTENT = "EXTRA_INTENT"
        private const val EXTRA_ERROR_TITLE = "EXTRA_ERROR_TITLE"
        private const val EXTRA_ERROR_SUB_TITLE = "EXTRA_ERROR_SUB_TITLE"
        private const val EXTRA_ERROR_LOG_TEXT = "EXTRA_ERROR_LOG_TEXT"

        fun from(activity: Activity) = RequestManager(activity)
    }
}