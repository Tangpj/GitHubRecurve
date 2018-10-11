package com.tangpj.oauth2.di

import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.tangpj.github.utils.GithubNextPageStrategy
import com.tangpj.oauth.api.OAuthService
import com.tangpj.recurve.retrofit2.LiveDataCallAdapterFactory
import dagger.Module
import javax.inject.Singleton

@Module(includes = [Oauth2ViewModelModule::class])
class OauthAppModule{

    @Singleton
    @Provides
    fun providerOauthService(): OAuthService{
        return Retrofit.Builder()
                .baseUrl("https://github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory(GithubNextPageStrategy()))
                .build()
                .create(OAuthService::class.java)
    }
}