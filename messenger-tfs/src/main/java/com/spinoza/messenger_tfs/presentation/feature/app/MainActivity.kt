package com.spinoza.messenger_tfs.presentation.feature.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding
import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolder
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.util.getAppComponent
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var navigatorHolder: AppNavigatorHolder

    @Inject
    lateinit var router: AppRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getAppComponent().inject(this)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            router.replaceScreen(Screens.Login())
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        val navigator = AppNavigator(this, R.id.mainFragmentContainer)
        navigatorHolder.getHolder().setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigatorHolder.getHolder().removeNavigator()
    }
}