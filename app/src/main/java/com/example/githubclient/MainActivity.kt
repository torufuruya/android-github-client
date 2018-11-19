package com.example.githubclient

import android.arch.lifecycle.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: RepoListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rcl_repo.setHasFixedSize(true)
        rcl_repo.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProviders.of(this).get(RepoListViewModel::class.java)
        viewModel.repos.observe(this, Observer {
            rcl_repo.adapter = Adapter(it ?: arrayListOf())
        })
        viewModel.loadRepos()
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

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val nameText: TextView = itemView.findViewById(R.id.name_text)
    val urlText: TextView = itemView.findViewById(R.id.url_text)
}

class Adapter(private val items: List<Repo>) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("Adapter", "onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("Adapter", "getItemCount")
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("Adapter", "onBindViewHolder")
        val repo = items[position]
        holder.nameText.text = repo.name
        holder.urlText.text = repo.url
    }
}

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