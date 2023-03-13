package com.spinoza.messenger_tfs.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}