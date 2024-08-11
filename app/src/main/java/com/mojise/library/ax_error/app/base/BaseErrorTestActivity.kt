package com.mojise.library.ax_error.app.base

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mojise.library.ax_error.app.databinding.ActivityErrorTestBinding
import java.io.IOException

abstract class BaseErrorTestActivity : AppCompatActivity() {

    protected val TAG: String
        get() = this.javaClass.simpleName

    protected lateinit var binding: ActivityErrorTestBinding
        private set

    protected val testTextFromIntent: String
        get() = intent.getStringExtra(EXTRA_TEST) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityErrorTestBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)

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