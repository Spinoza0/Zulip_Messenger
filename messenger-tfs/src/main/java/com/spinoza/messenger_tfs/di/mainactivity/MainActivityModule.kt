package com.spinoza.messenger_tfs.di.mainactivity

import com.github.terrakok.cicerone.androidx.AppNavigator
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {

    @Provides
    fun provideAppNavigator(mainActivity: MainActivity): AppNavigator =
        AppNavigator(mainActivity, R.id.mainFragmentContainer)
}