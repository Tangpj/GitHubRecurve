package com.tangpj.repository.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.ItemKeyedDataSource
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.recurve.apollo.LiveDataApollo
import com.recurve.core.resource.ApiResponse
import com.recurve.core.util.RateLimiter
import com.recurve.paging.ItemKeyedBoundResource
import com.tangpj.github.di.PagingConfig
import com.tangpj.repository.ApolloCommitsQuery
import com.tangpj.repository.db.RepositoryDb
import com.tangpj.repository.mapper.*
import com.tangpj.repository.valueObject.query.CommitsQuery
import com.tangpj.repository.valueObject.query.after
import com.tangpj.repository.valueObject.query.getApolloCommitsQuery
import com.tangpj.repository.valueObject.query.startFirst
import com.tangpj.repository.valueObject.result.CommitsResult
import com.tangpj.repository.vo.CommitVo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CommitRepository @Inject constructor(
        private val apolloClient: ApolloClient,
        private val repoDb: RepositoryDb,
        private val pagingConfig: PagingConfig){


    private val commitRateLimiter = RateLimiter<ApolloCommitsQuery>(1, TimeUnit.MINUTES)

    fun loadCommits(commitsQuery: CommitsQuery) =
            object : ItemKeyedBoundResource<String, CommitVo, ApolloCommitsQuery.Data>(){

                private var commitsResult: CommitsResult? = null
                private var query: ApolloCommitsQuery? = null

                override fun createInitialCall(params: ItemKeyedDataSource.LoadInitialParams<String>): LiveData<ApiResponse<ApolloCommitsQuery.Data>> {
                    val initialQuery =
                            commitsQuery.getApolloCommitsQuery(startFirst = params.requestedLoadSize)
                    query = initialQuery
                    Timber.d("""createInitialCall, start first = ${params.requestedLoadSize},
                        |hasNextPage = ${commitsResult?.pageInfo?.hasNextPage}""".trimMargin())
                    val commitCall = apolloClient.query(initialQuery)
                            .responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                    return LiveDataApollo.from(commitCall)

                }

                override fun createAfterCall(params: ItemKeyedDataSource.LoadParams<String>): LiveData<ApiResponse<ApolloCommitsQuery.Data>>? {

                    val afterQuery = commitsQuery.getApolloCommitsQuery(
                            startFirst = params.requestedLoadSize,
                            after = commitsResult?.after)
                   query = afterQuery
                    val commitCall = apolloClient
                            .query(afterQuery)
                            .responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                    return LiveDataApollo.from(commitCall)
                }

                override fun getKey(item: CommitVo): String = item.commit.id


                override fun saveCallResult(item: ApolloCommitsQuery.Data) {
                    query?.let {
                        commitsResult = saveCommits(it, item)
                    }
                }

                override fun shouldFetch(data: List<CommitVo>?): Boolean =
                        (data == null || data.isEmpty() || commitRateLimiter.shouldFetch(query))

                override fun loadFromDb(): LiveData<List<CommitVo>> {
                    val commitResultLiveData = repoDb.commitDao().loadCommitResult(
                            login = commitsQuery.gitObjectQuery.repoDetailQuery.login,
                            repoName = commitsQuery.gitObjectQuery.repoDetailQuery.name,
                            startFirst = query?.startFirst() ?: pagingConfig.initialLoadSizeHint,
                            after = query?.after() ?: "",
                            authorId = commitsQuery.author?.id)

                    return Transformations.switchMap(commitResultLiveData) {
                        repoDb.commitDao().loadCommitVoListOrderById(it?.commitIds ?: emptyList())
                    }

                }

                override fun hasNextPage(): Boolean {
                    return commitsResult?.pageInfo?.hasNextPage ?: false
                }

            }.asListing(pagingConfig.getConfig())


    private fun saveCommits(
            query: ApolloCommitsQuery,
            data: ApolloCommitsQuery.Data
    ): CommitsResult?{
        val history = data.getCommitHistory()
        history ?: return null
        val pageInfo = history.getLocalPageInfo()
        val commitVos = history.getCommitVos()

        val commitIds = commitVos.map { it.commit.id}
        val result = CommitsResult(
                login = query.variables().login(),
                repoName = query.variables().repoName(),
                authorId = query.variables().author().value?.id() ?: "",
                commitIds = commitIds,
                startFirst = query.variables().startFirst().value ?: pagingConfig.initialLoadSizeHint,
                after = query.variables().after().value ?: "",
                pageInfo = pageInfo
        )
        val commits = commitVos.map { it.commit }
        val committees = commitVos.map { it.committer }
        repoDb.runInTransaction {
            repoDb.commitDao().insertCommittees(committees)
            repoDb.commitDao().insertCommits(commits)
            repoDb.commitDao().insertCommitsResult(result)
        }
        return result
    }
}