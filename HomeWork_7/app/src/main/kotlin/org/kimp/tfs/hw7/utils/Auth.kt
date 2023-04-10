package org.kimp.tfs.hw7.utils

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import org.kimp.tfs.hw7.BuildConfig
import retrofit2.Invocation

@Target(AnnotationTarget.FUNCTION)
annotation class Authenticated

class ZulipAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val invocation = chain.request().tag(Invocation::class.java)
            ?: return chain.proceed(chain.request())

        val shouldAttachAuthHeader = invocation
            .method()
            .annotations
            .any { it.annotationClass == Authenticated::class }

        return if (shouldAttachAuthHeader) {
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .addHeader(
                        "Authorization",
                        Credentials.basic(BuildConfig.ZULIP_USERNAME, BuildConfig.ZULIP_API_KEY)
                    )
                    .build()
            )
        } else chain.proceed(chain.request())
    }
}
