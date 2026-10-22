package com.example.nfctag

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "NFC-метка активна.\nПоднеси телефон к терминалу."
            textSize = 20f
            setPadding(60, 200, 60, 60)
        })
    }
}
