package com.spinoza.messenger_tfs.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding
import com.spinoza.messenger_tfs.presentation.navigation.Screens

class MainActivity : AppCompatActivity() {

    private val globalNavigatorHolder = App.navigatorHolder
    private val globalRouter = App.router

    private val globalNavigator = AppNavigator(this, R.id.mainFragmentContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            globalRouter.replaceScreen(Screens.Login())
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        globalNavigatorHolder.setNavigator(globalNavigator)
    }

    override fun onPause() {
        super.onPause()
        globalNavigatorHolder.removeNavigator()
    }
}