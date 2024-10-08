package com.mojise.library.ax_error.app.activity

import android.os.Bundle
import com.mojise.library.ax_error.AxError
import com.mojise.library.ax_error.app.base.BaseErrorTestActivity

internal class ActivityCA : BaseErrorTestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btn1.text = "Start Error Activity"
        binding.btn1.setOnClickListener {
            AxError.FullScreenBuilder(this@ActivityCA)
                .errorTitle("Error Title")
                .errorSubTitle("Error Sub Title")
                .throwable(NullPointerException("Test Exception"))
                .start()
        }
    }
}