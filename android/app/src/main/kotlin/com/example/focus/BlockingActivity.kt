package com.example.focus

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class BlockingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "📚 Access to this app is blocked."
            textSize = 22f
            setBackgroundColor(0xAA000000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            gravity = android.view.Gravity.CENTER
        }

        setContentView(textView)
    }
}