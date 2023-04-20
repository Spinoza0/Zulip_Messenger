package org.kimp.tfs.hw7.di

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.kimp.tfs.hw7.R
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FontModule {
    @Provides
    @Singleton
    fun provideAppTypeface(
        @ApplicationContext context: Context
    ) = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans)!!
}
