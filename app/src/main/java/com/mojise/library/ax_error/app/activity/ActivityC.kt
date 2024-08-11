package com.mojise.library.ax_error.app.activity

import android.os.Bundle
import com.mojise.library.ax_error.app.base.BaseErrorTestActivity

class ActivityC : BaseErrorTestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btn1.onClickedStartTo(ActivityCA::class.java)
    }
}