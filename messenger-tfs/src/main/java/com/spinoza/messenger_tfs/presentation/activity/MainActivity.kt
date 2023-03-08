package com.spinoza.messenger_tfs.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}