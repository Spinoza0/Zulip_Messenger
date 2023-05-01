package com.spinoza.messenger_tfs.di

import javax.inject.Qualifier

@Qualifier
annotation class ChannelIsSubscribed

@Qualifier
annotation class DispatcherDefault

@Qualifier
annotation class DispatcherIO