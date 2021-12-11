package com.example.spire.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.example.spire.databinding.FragmentSearchBinding
import retrofit2.Call
import retrofit2.Retrofit
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.spire.LoginActivity
import com.example.spire.MainActivity
import com.google.gson.GsonBuilder

import org.json.JSONException;
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory


class SearchFragment : Fragment() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var gameAdapter: RecyclerView.Adapter<GameAdapter.GameViewHolder>? = null
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var mQueue: RequestQueue? = null
    public var mIsLoading = false
    private var DELAY_TIME_TEXTCHANGED = 2500;
    private var mIsLastPage = false
    private var mCurrentPage = 0
    private val pageSize = 10
    private lateinit var search : String

    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialise loading state
        mIsLoading = false;
        mIsLastPage = false;
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        search = binding.searchText.text.toString()

        return binding.root

    }
    private fun adapterOnClick(game: Game) {
        (activity as MainActivity).adapterOnClick(game)
    }
    private fun showAllGames(games : List<Game>){
        mIsLoading = true
        gameAdapter = GameAdapter(games) { game -> adapterOnClick(game) }
        (gameAdapter as GameAdapter).setList(games)
        binding.recyclerSearchGame.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = gameAdapter
        }
        mIsLoading = false
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://rawg.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.fetchAllGames().enqueue(object : Callback<AllGameQuery>{
            override fun onResponse(
                call: Call<AllGameQuery>,
                response: retrofit2.Response<AllGameQuery>
            ) {
                if(!mIsLoading)
                    showAllGames(response.body()!!.results)

                val layoutManager = binding.recyclerSearchGame.layoutManager as LinearLayoutManager
                scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                        // Est trigger quand il faut ajouter des données à la liste (bas de la liste atteint)
                        loadNextDataFromApi(page)
                    }
                }
                binding.recyclerSearchGame.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListener);
                binding.searchText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun afterTextChanged(p0: Editable?) {
                        Handler().postDelayed({
                            if(binding.searchText.text.trim().toString() != "")
                                api.SearchGames(binding.searchText.text.trim().toString()).enqueue(object : Callback<AllGameQuery>{
                                    override fun onResponse(
                                        call: Call<AllGameQuery>,
                                        response: Response<AllGameQuery>
                                    ) {
                                        if(!mIsLoading)
                                            showAllGames(response.body()!!.results)
                                    }

                                    override fun onFailure(call: Call<AllGameQuery>, t: Throwable) {
                                    }
                                })
                            else {

                                api.fetchAllGames().enqueue(object : Callback<AllGameQuery> {
                                    override fun onResponse(
                                        call: Call<AllGameQuery>,
                                        response: retrofit2.Response<AllGameQuery>
                                    ) {
                                        if (!mIsLoading)
                                            showAllGames(response.body()!!.results)

                                        scrollListener =
                                            object :
                                                EndlessRecyclerViewScrollListener(layoutManager) {
                                                override fun onLoadMore(
                                                    page: Int,
                                                    totalItemsCount: Int,
                                                    view: RecyclerView
                                                ) {
                                                    // Est trigger quand il faut ajouter des données à la liste (bas de la liste atteint)
                                                    loadNextDataFromApi(page)
                                                }
                                            }
                                        binding.recyclerSearchGame.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListener);

                                    }

                                    override fun onFailure(call: Call<AllGameQuery>, t: Throwable) {

                                    }
                                })
                            }
                        }, DELAY_TIME_TEXTCHANGED.toLong());
                        }
                    })
            }
            override fun onFailure(call: Call<AllGameQuery>, t: Throwable) {
            }

        })


    }


    private fun loadNextDataFromApi(page: Int) {
        mIsLoading = true
        Log.d("NUMBER", page.toString())
        // update recycler adapter avec la prochaine page
        val retrofit = Retrofit.Builder()
            .baseUrl("https://rawg.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.GetPage(page).enqueue(object : Callback<AllGameQuery>{
            override fun onResponse(call: Call<AllGameQuery>, response: Response<AllGameQuery>) {
                val result = response.body()
                if(result == null)
                    return
                else
                    (gameAdapter as GameAdapter).addAll(result.results)

                mIsLoading = false
                if(result.next == null){
                    mIsLastPage = true
                }
                else{
                    mIsLastPage = false
                }
            }
            override fun onFailure(call: Call<AllGameQuery>, t: Throwable) {
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}