package com.example.githubclient

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.repo_list_fragment.*


class RepoListFragment : Fragment() {

    private lateinit var viewModel: RepoListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.repo_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RepoListViewModel::class.java)

        rcl_repo.setHasFixedSize(true)
        rcl_repo.layoutManager = LinearLayoutManager(context)

        viewModel.repos.observe(this, Observer {
            rcl_repo.adapter = Adapter(it ?: arrayListOf())
        })
        viewModel.loadRepos()
    }

}
