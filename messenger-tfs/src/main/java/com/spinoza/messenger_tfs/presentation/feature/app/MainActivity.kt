package com.spinoza.messenger_tfs.presentation.feature.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding
import com.spinoza.messenger_tfs.di.GlobalDI
import com.spinoza.messenger_tfs.presentation.navigation.Screens

class MainActivity : AppCompatActivity() {

    private val navigatorHolder = GlobalDI.INSTANCE.globalNavigatorHolder
    private val router = GlobalDI.INSTANCE.globalRouter
    private val navigator = AppNavigator(this, R.id.mainFragmentContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            router.replaceScreen(Screens.Login())
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
    }
}