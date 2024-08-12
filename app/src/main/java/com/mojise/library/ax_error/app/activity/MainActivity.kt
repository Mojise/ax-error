package com.mojise.library.ax_error.app.activity

import android.os.Bundle
import com.mojise.library.ax_error.app.base.BaseErrorTestActivity

internal class MainActivity : BaseErrorTestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with (binding) {
            btn1.onClickedStartTo(ActivityA::class.java) { putExtraTest(TAG) }
            btn2.onClickedStartTo(ActivityB::class.java)
            btn3.onClickedStartTo(ActivityC::class.java)
        }
    }
}