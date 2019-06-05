package com.tangpj.repository.ui.repositories

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.tangpj.paging.Listing
import com.tangpj.paging.PageLoadState
import com.tangpj.repository.repository.RepoRepository
import com.tangpj.repository.vo.Repo
import javax.inject.Inject

class RepositoryViewModel @Inject constructor(private val repoRepository: RepoRepository): ViewModel(){

    private val _login = MutableLiveData<String>()


    private val repoListing: LiveData<Listing<Repo>> = Transformations.map(_login){
        repoRepository.loadStarRepos(it)
    }


    val pagedList: LiveData<PagedList<Repo>> = Transformations.switchMap(repoListing){
        it.pagedList
    }

    val repoRetry: LiveData<() -> Unit>  = Transformations.map(repoListing){
        it.retry
    }

    var refresh: LiveData<() -> Unit> = Transformations.map(repoListing){
        it.refresh
    }
    val pageLoadState: LiveData<PageLoadState> = Transformations.switchMap(repoListing) {
        it?.pageLoadState
    }

    fun setRepoOwner(login: String){
        _login.value = login
    }


}