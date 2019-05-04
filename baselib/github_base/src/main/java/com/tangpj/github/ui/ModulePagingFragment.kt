package com.tangpj.github.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.tangpj.github.databinding.FragmentBaseRecyclerViewBinding
import com.tangpj.recurve.dagger2.RecurveDaggerListFragment

/**
 *
 * @className: 模块化Paging
 * @author: tangpengjian113
 * @createTime: 2019-04-15 15:45
 */
abstract class ModulePagingFragment: RecurveDaggerListFragment(){

    private lateinit var binding: FragmentBaseRecyclerViewBinding

    abstract fun onBindingInit(binding: ViewDataBinding)

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?,
                                 savedInstanceState: Bundle?): ViewDataBinding? {
        binding = FragmentBaseRecyclerViewBinding.inflate(inflater, container, false)

        binding.setLifecycleOwner(this)
        onBindingInit(binding)
        initRecyclerView(binding.rvContent)
        return binding

    }

    fun <T> loading(pageLoadingInvoke: PageLoading<T>.() -> Unit){
        val loading = PageLoading<T>()
        loading.pageLoadingInvoke()
        binding.resource = loading.resource
        binding.networkState = loading.networkState
        binding.retryCallback = loading.retry
    }



}