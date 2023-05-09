package com.spinoza.messenger_tfs.di

import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ChannelsScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PeopleScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ProfileScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MessagesScope