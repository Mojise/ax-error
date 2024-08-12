package com.mojise.library.ax_error

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.TaskStackBuilder
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable

private data class AxErrorActivityViewBinding(
    val scrollViewErrorLogText: NestedScrollView,
    val llCopyButtonAndGuideMessage: LinearLayout,
    val tvErrorTitle: TextView,
    val tvErrorSubTitle: TextView,
    val tvErrorLogText: EditText,
    val tvCopyButtonGuideMessage: TextView,
    val btnRefresh: ConstraintLayout,
    val btnCopyErrorLog: TextView,
    val textHelpLeft: TextView,
    val btnHelp: TextView,
)

internal data class AxErrorActivityUiData(
    val errorTitle: String,
    val errorSubTitle: String,
    val throwable: Throwable?,
) : Serializable {

    companion object {
        fun getDefault(context: Context) = AxErrorActivityUiData(
            errorTitle = context.getString(R.string.ax_error_library_error_title),
            errorSubTitle = context.getString(R.string.ax_error_library_error_sub_title),
            throwable = null,
        )
    }
}

class AxErrorActivity : AppCompatActivity() {

    private lateinit var binding: AxErrorActivityViewBinding

    private val activityIntents: List<Intent>? by lazy {
        IntentCompat
            .getParcelableArrayExtra(intent, EXTRA_INTENT_ARRAY, Intent::class.java)
            ?.filterIsInstance<Intent>()
    }

    private val uiData: AxErrorActivityUiData by lazy {
        IntentCompat.getSerializableExtra(intent, EXTRA_ERROR_DATA, AxErrorActivityUiData::class.java)
            ?: AxErrorActivityUiData.getDefault(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ax_error_in_library)

        // Log.e(TAG, "onCreate: ProcessId=${Process.myPid()}")
        // activityIntents?.forEach { Log.e(TAG, "onCreate :: activityIntents=$it") }

        initViewBinding()
        initViewAndListeners()
    }

    private fun initViewBinding() {
        binding = AxErrorActivityViewBinding(
            scrollViewErrorLogText = findViewById(R.id.scroll_view_error_log_text),
            llCopyButtonAndGuideMessage = findViewById(R.id.ll_copy_button_and_guide_message),
            tvErrorTitle = findViewById(R.id.error_title),
            tvErrorSubTitle = findViewById(R.id.error_sub_title),
            tvErrorLogText = findViewById(R.id.error_log_text),
            tvCopyButtonGuideMessage = findViewById(R.id.copy_button_guide_message),
            btnRefresh = findViewById(R.id.btn_refresh),
            btnCopyErrorLog = findViewById(R.id.btn_copy_error_log),
            textHelpLeft = findViewById(R.id.text_help_left),
            btnHelp = findViewById(R.id.btn_help),
        )
    }

    private fun initViewAndListeners() {
        with (binding) {
            scrollViewErrorLogText.isVisible = BuildConfig.DEBUG
            llCopyButtonAndGuideMessage.isVisible = BuildConfig.DEBUG

            tvErrorTitle.text = uiData.errorTitle.toStringFromHtml()
            tvErrorSubTitle.text = uiData.errorSubTitle.toStringFromHtml()
            tvErrorLogText.setText(uiData.throwable.toErrorLogText())

            tvCopyButtonGuideMessage.text =
                R.string.ax_error_library_copy_button_guide_message.toStringFromHtml(this@AxErrorActivity)

            btnCopyErrorLog.setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("errorLog", uiData.throwable.toErrorLogText())
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this@AxErrorActivity, R.string.ax_error_library_toast_copied, Toast.LENGTH_SHORT).show()
            }

            btnRefresh.setOnClickListener {
                finishAndStartActivitiesInStackSequentially()
            }

            textHelpLeft.isVisible = AxError.isHelpButtonVisible
            btnHelp.isVisible = AxError.isHelpButtonVisible
            btnHelp.setOnClickListener {
                try {
                    AxError.onHelpButtonClickedListener?.onHelpButtonClicked(this@AxErrorActivity)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@AxErrorActivity, R.string.ax_error_library_toast_message_help_button_error, Toast.LENGTH_SHORT).show()
                    finishAndStartActivitiesInStackSequentially()
                }
            }
        }

        onBackPressedDispatcher.addCallback(owner = this, enabled = true) {
            finishAndStartActivitiesInStackSequentially()
        }
    }

    /**
     * - 현재 [AxErrorActivity]를 종료하고, [activityIntents]에 저장된 [Intent]를 순차적으로 재실행한다.
     * - TaskStackBuilder 활용.
     * - 액티비티를 한 번에 전부 재실행되지 않고, 스택의 맨 위부터 하나씩 실행함.
     *
     * @see TaskStackBuilder
     */
    private fun finishAndStartActivitiesInStackSequentially() {
        activityIntents
            ?.toTaskStackBuilder(this)
            ?.startActivities()

        finish()
    }

    /**
     * - 현재 [AxErrorActivity]를 종료하고, [activityIntents]에 저장된 [Intent]를 순차적으로 재실행한다.
     * - 스택에 있는 액티비티를 모두 순차적으로 재실행함.
     */
    private fun finishAndStartActivityInStackSequentially2() {
        lifecycleScope.launch {
            activityIntents?.forEach {
                startActivity(it)
                delay(1)
            }
            finish()
        }
    }

    companion object {
        internal const val TAG = AxError.TAG
        internal const val EXTRA_INTENT_ARRAY = "EXTRA_INTENT_ARRAY"
        internal const val EXTRA_ERROR_DATA = "EXTRA_ERROR_DATA"
    }
}