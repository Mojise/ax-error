package com.mojise.library.ax_error.app.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.mojise.library.ax_error.app.base.BaseErrorTestActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class ActivityA : BaseErrorTestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            binding.showProgressIndicator()
            delay(2000)
            binding.hideProgressIndicator()
        }

        binding.textView.text = testTextFromIntent
        binding.btn1.onClickedStartTo(ActivityAA::class.java) { putExtraTest(TAG) }


//        binding.btn1.text = "start ActivityAA (with delay)"
//        binding.btn1.setOnClickListener {
//            lifecycleScope.launch {
//                binding.showProgressIndicator()
//                delay(3000)
//                binding.hideProgressIndicator()
//
//                val prevText = intent.getStringExtra(EXTRA_TEST) ?: ""
//
//                val newIntent = Intent(this@ActivityA, ActivityAA::class.java)
//                newIntent.putExtraTest("$prevText -> $TAG")
//
//                startActivity(newIntent)
//            }
//        }
    }
}