package com.tangpj.github.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module

@Module
abstract class GitHubAppModules{
    @Binds
    abstract fun bindContext(application: Application): Context
}