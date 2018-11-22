package com.example.githubclient

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel;
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class RepoListViewModel : ViewModel() {

    private var _repos = MutableLiveData<List<Repo>>()
    val repos: LiveData<List<Repo>>
        get() = _repos

    fun loadRepos() {
        GlobalScope.launch(Dispatchers.Main) {
            _loadRepos().let {
                _repos.value = it
            }
        }
    }

    private suspend fun _loadRepos(): List<Repo> {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

        val service = retrofit.create(GitHubService::class.java)
        return service.listRepos("octocat").await()
    }
}

interface GitHubService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): Deferred<List<Repo>>
}

data class Repo(
    val id: String,
    val name: String,
    val url: String
)

