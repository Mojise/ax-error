package com.mojise.library.ax_error.app.activity

import android.os.Bundle
import com.mojise.library.ax_error.app.base.BaseErrorTestActivity

class ActivityA : BaseErrorTestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.textView.text = testTextFromIntent
        binding.btn1.onClickedStartTo(ActivityAA::class.java) { putExtraTest(TAG) }
    }
}