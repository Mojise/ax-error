package com.mojise.library.ax_error.app.base

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mojise.library.ax_error.app.R
import java.io.IOException

internal data class BaseErrorTestActivityViewBinding(
    val btnBack: ImageView,
    val title: TextView,
    val textView: TextView,
    val btn1: Button,
    val btn2: Button,
    val btn3: Button,
    val btn4: Button,
    val btn5: Button,
    val progressIndicatorContainer: FrameLayout,
    val progressIndicator: CircularProgressIndicator,
) {
    fun showProgressIndicator() {
        progressIndicatorContainer.isVisible = true
        progressIndicator.show()
    }
    fun hideProgressIndicator() {
        progressIndicatorContainer.isVisible = false
        progressIndicator.hide()
    }
}

internal abstract class BaseErrorTestActivity : AppCompatActivity() {

    protected val TAG: String
        get() = this.javaClass.simpleName

    protected lateinit var binding: BaseErrorTestActivityViewBinding
        private set

    protected val testTextFromIntent: String
        get() = intent.getStringExtra(EXTRA_TEST) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_test_in_ax_error_library)

        binding = BaseErrorTestActivityViewBinding(
            btnBack = findViewById(R.id.btn_back),
            title = findViewById(R.id.title),
            textView = findViewById(R.id.text_view),
            btn1 = findViewById(R.id.btn_1),
            btn2 = findViewById(R.id.btn_2),
            btn3 = findViewById(R.id.btn_3),
            btn4 = findViewById(R.id.btn_4),
            btn5 = findViewById(R.id.btn_5),
            progressIndicatorContainer = findViewById(R.id.progress_indicator_container),
            progressIndicator = findViewById(R.id.progress_indicator),
        )

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.title.text = this.javaClass.simpleName

        with (binding) {
            btn1.setOnClickListener {
                throw NullPointerException("Test Error Case 1.")
            }
            btn2.setOnClickListener {
                throw IllegalArgumentException("Test Error Case 2.")
            }
            btn3.setOnClickListener {
                throw ArithmeticException("Test Error Case 3.")
            }
            btn4.setOnClickListener {
                throw IOException("Test Error Case 4.")
            }
            btn5.setOnClickListener {
                throw RuntimeException("Test Error Case 5.")
            }
        }
    }

    protected fun Button.onClickedStartTo(
        clazz: Class<*>,
        intentBuilder: Intent.() -> Unit = { },
    ) {
        text = "start ${clazz.simpleName}"
        setOnClickListener {
            val prevText = this@BaseErrorTestActivity.intent.getStringExtra(EXTRA_TEST) ?: ""
            val intent = Intent(this@BaseErrorTestActivity, clazz)

            intent.intentBuilder()
            intent.getStringExtra(EXTRA_TEST)?.let { currText -> intent.putExtra(EXTRA_TEST, "$prevText -> $currText") }
            startActivity(intent)
        }
    }

    protected fun Intent.putExtraTest(text: String) {
        putExtra(EXTRA_TEST, text)
    }

    companion object {
        const val EXTRA_TEST = "test"
    }
}