package com.tangpj.repository.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.apollographql.apollo.ApolloClient
import com.tangpj.github.fragment.RepoField
import com.tangpj.github.vo.Repo

import com.tangpj.recurve.resource.ApiResponse
import com.tangpj.recurve.resource.NetworkBoundResource
import com.tangpj.recurve.util.RateLimiter
import com.tangpj.repository.db.RepoDao
import java.util.concurrent.TimeUnit

class RepoRepository  constructor(
         val apolloClient: ApolloClient,
         val repoDao: RepoDao){

    private val repoRateLimiter = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun loadRepos(owner: String) =
            object : NetworkBoundResource<List<Repo>, List<RepoField>>(){
                override fun saveCallResult(item: List<RepoField>) {
                }

                override fun shouldFetch(data: List<com.tangpj.github.vo.Repo>?): Boolean =
                        data == null || data.isEmpty() || repoRateLimiter.shouldFetch(owner)

                override fun loadFromDb(): LiveData<List<com.tangpj.github.vo.Repo>>  =
                    repoDao.loadRepositories(owner)


                override fun createCall(): LiveData<ApiResponse<List<RepoField>>> {
                    return MediatorLiveData()
                }

            }.asLiveData()

}