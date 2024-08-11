package com.mojise.library.ax_error.app.activity

import android.os.Bundle
import com.mojise.library.ax_error.app.base.BaseErrorTestActivity

class ActivityAAA : BaseErrorTestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.textView.text = testTextFromIntent
        binding.btn1.onClickedStartTo(MainActivity::class.java)
    }
}