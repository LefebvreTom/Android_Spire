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
import com.example.spire.databinding.FragmentSearchBinding
import retrofit2.Call
import retrofit2.Retrofit
import com.example.spire.LoginActivity
import com.example.spire.MainActivity
import com.example.spire.R
import com.google.gson.GsonBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

import org.json.JSONException;
import retrofit2.Callback
import retrofit2.Response
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class SearchFragment : Fragment() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var gameAdapter: RecyclerView.Adapter<GameAdapter.GameViewHolder>? = null
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    public var mIsLoading = false
    private var DELAY_TIME_TEXTCHANGED = 1500;
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
        (activity as MainActivity).adapterOnClick(game) //passe la fonction onclick au mainactivity pour rediriger sur le fragment de gamesheet au clic
    }
    private fun showAllGames(games : List<Game>){
        mIsLoading = true
        gameAdapter = GameAdapter(games, false) { game -> adapterOnClick(game) }
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
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        val allgameQuery = arrayListOf<AllGameQuery>()
        val requests2 = arrayListOf<Observable<*>>()
        val item = api.fetchObservableAllGames() as Observable<*> //toutes les requ??tes de jeux sont stock??es
        requests2.add(item)
        Observable //on zip nos requ??tes pour les executer toutes une par une de mani??re synchrone (car impossible de le faire de mani??re asynchrone dans une boucle for)
            .zip(requests2) {args -> listOf(args) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {

                val response = it[0] // r??ponse cumul??e de toute les requ??tes

                response.forEach { allgameRequest ->
                    allgameQuery.add(allgameRequest as AllGameQuery) //chaque r??ponse est stock??e dans la gameList
                }
                showAllGames(allgameQuery[0].results) //on g??n??re la recyclerView


                val layoutManager = binding.recyclerSearchGame.layoutManager as LinearLayoutManager
                scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                        // Est trigger quand il faut ajouter des donn??es ?? la liste (bas de la liste atteint)
                        loadNextDataFromApi(page)
                    }
                }
                binding.recyclerSearchGame.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListener);
                binding.searchText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun afterTextChanged(p0: Editable?) { //est trigger quand on change le texte pour la recherche


                        Handler().postDelayed({ //handler mis en place car sinon les donn??es changent trop vite et l'adapter n'arrive plus ?? suivre
                            val retrofitSynchrone = Retrofit.Builder()
                                .baseUrl("https://rawg.io")
                                .addConverterFactory(GsonConverterFactory.create())
                                .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) //on ajoute un adapter sp??cial pour prendre en compte les Observers de RxJava3
                                .build()

                            val backendApiSynchrone =
                                retrofitSynchrone.create(ApiService::class.java)

                            if (binding.searchText.text.trim().toString() != "") {

                                val gameQuery = arrayListOf<AllGameQuery>()
                                val requests = arrayListOf<Observable<*>>()
                                val item = backendApiSynchrone.GetObservableSearchGames(
                                    binding.searchText.text.trim().toString()
                                ) as Observable<*> //chaque requ??te de jeu ?? partir de son ID est stock??e dans requests
                                requests.add(item)
                                Observable //on zip nos requ??tes pour les executer toutes une par une de mani??re synchrone (car impossible de le faire de mani??re asynchrone dans une boucle for)
                                    .zip(requests) { args -> listOf(args) }
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({

                                        val response =
                                            it[0] // r??ponse cumul??e de toute les requ??tes

                                        response.forEach { allgameRequest ->
                                            gameQuery.add(allgameRequest as AllGameQuery) //chaque r??ponse est stock??e dans la gameList
                                        }
                                        showAllGames(gameQuery[0].results) //on g??n??re la recyclerView
                                    }, {
                                    })
                            } else { //si on a supprim?? notre texte et que le champ est vide, on repart sur la liste basique avec scroll listener
                                val gameQuery = arrayListOf<AllGameQuery>()
                                val requests = arrayListOf<Observable<*>>()
                                val item = backendApiSynchrone.fetchObservableAllGames() as Observable<*> //toutes les requ??tes de jeux sont stock??es
                                requests.add(item)
                                Observable //on zip nos requ??tes pour les executer toutes une par une de mani??re synchrone (car impossible de le faire de mani??re asynchrone dans une boucle for)
                                    .zip(requests) { args -> listOf(args) }
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({

                                        val response =
                                            it[0] // r??ponse cumul??e de toute les requ??tes

                                        response.forEach { allgameRequest ->
                                            gameQuery.add(allgameRequest as AllGameQuery) //chaque r??ponse est stock??e dans la gameList
                                        }
                                        showAllGames(gameQuery[0].results) //on g??n??re la recyclerView

                                    }, {
                                    })
                            }
                        }, DELAY_TIME_TEXTCHANGED.toLong());
                    }
                })
            }
    }


    private fun loadNextDataFromApi(page: Int) {
        mIsLoading = true
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
                    (gameAdapter as GameAdapter).addAll(result.results) //ajout des  donn??es de la nouvelle page dans l'adapter

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