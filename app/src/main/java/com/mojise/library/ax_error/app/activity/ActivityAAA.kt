package com.mojise.library.ax_error.app.activity

import android.content.Intent
import android.os.Bundle
import com.mojise.library.ax_error.app.base.BaseErrorTestActivity

internal class ActivityAAA : BaseErrorTestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.textView.text = testTextFromIntent
        binding.btn1.onClickedStartTo(MainActivity::class.java) {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
}