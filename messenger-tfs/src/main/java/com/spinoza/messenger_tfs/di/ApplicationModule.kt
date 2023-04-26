package com.spinoza.messenger_tfs.di

import com.bumptech.glide.load.model.LazyHeaders
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.domain.model.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.webutil.WebUtil
import dagger.Module
import dagger.Provides

@Module
object ApplicationModule {

    @ApplicationScope
    @Provides
    fun provideCicerone(): Cicerone<Router> = Cicerone.create()


    @ApplicationScope
    @Provides
    fun provideGlobalRouter(cicerone: Cicerone<Router>): Router = cicerone.router

    @ApplicationScope
    @Provides
    fun provideGlobalNavigatorHolder(cicerone: Cicerone<Router>): NavigatorHolder =
        cicerone.getNavigatorHolder()

    @ApplicationScope
    @Provides
    fun provideAppAuthKeeper(): AppAuthKeeper = AppAuthKeeper(EMPTY_STRING)

    @ApplicationScope
    @Provides
    fun provideUrlUtil(authKeeper: AppAuthKeeper): WebUtil = object : WebUtil {

        override fun getFullUrl(url: String): String =
            if (url.startsWith(URL_HTTP_SECURED_PREFIX, ignoreCase = true) ||
                url.startsWith(URL_HTTP_BASIC_PREFIX, ignoreCase = true)
            ) {
                url
            } else {
                val modifiedUrl = if (!url.startsWith(URL_SLASH)) {
                    "/$url"
                } else {
                    url
                }
                "${BASE_URL}$modifiedUrl"
            }


        override fun isUserUploadsUrl(url: String): Boolean =
            url.startsWith(URL_USER_UPLOADS_PREFIX, ignoreCase = true)

        override fun getLazyHeaders(): LazyHeaders =
            LazyHeaders.Builder().addHeader(HEADER_AUTHORIZATION_TITLE, authKeeper.data).build()
    }

    private const val EMPTY_STRING = ""
    private const val BASE_URL = BuildConfig.ZULIP_SERVER_URL
    private const val URL_HTTP_SECURED_PREFIX = "https://"
    private const val URL_HTTP_BASIC_PREFIX = "http://"
    private const val URL_SLASH = "/"
    private const val URL_USER_UPLOADS_PREFIX = "$BASE_URL/user_uploads"
    private const val HEADER_AUTHORIZATION_TITLE = "Authorization"
}