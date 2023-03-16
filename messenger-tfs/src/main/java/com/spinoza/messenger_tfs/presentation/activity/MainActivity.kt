package com.spinoza.messenger_tfs.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.spinoza.messenger_tfs.MessengerApp
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val navigator = AppNavigator(this, R.id.fragmentContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        MessengerApp.navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        MessengerApp.navigatorHolder.removeNavigator()
        super.onPause()
    }
}