package com.example.spire.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spire.R
import com.example.spire.databinding.FragmentSearchBinding
import values.Datasource


class SearchFragment : Fragment() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<GameAdapter.GameViewHolder>? = null
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    //private lateinit var adapter: ArrayAdapter<*>
   // private lateinit var adapter2: ArrayAdapter<*>
    public val mylist = arrayListOf<String>()
    val gameName = arrayListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*val gameList = Datasource(this).getGameList()
        val recyclerView: RecyclerView = recycler_Game
        recyclerView.adapter = GameAdapter(gameList)*/

        _binding = FragmentSearchBinding.inflate(inflater, container, false)


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gameList = Datasource(this).getGameList()
        val recyclerView: RecyclerView = binding.recyclerGame
        //recyclerView.adapter = GameAdapter(gameList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = GameAdapter(gameList)
        }
        /*gameName.add("far Cry 6")
        gameName.add("DoodleJump")
        gameName.add("BattleField 2042")
        gameName.add("The witcher 3")
        gameName.add("Skyrim")
        gameName.add("Fifa22")
        gameName.add("Resident Evil VIllAGE")
        gameName.add("Deathloop")
        gameName.add("Halo")
        gameName.add("NBA 2K22")*/

        /*adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.games_name)
            //gameName
        )

        adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_2, mylist)
*/
        /*lv_listView.adapter = adapter
        //lv_listView2.adapter = adapter2
        lv_listView.onItemClickListener =
            AdapterView.OnItemClickListener { Parent, view, position, id ->
                Toast.makeText(
                    context,
                    Parent?.getItemAtPosition(position).toString()+" added",
                    Toast.LENGTH_SHORT
                ).show()

                mylist.add(Parent?.getItemAtPosition(position).toString())
            }



        lv_listView.emptyView = tv_emptyTextView
        search_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                adapter.filter.filter(charSequence)
            }

            override fun afterTextChanged(editable: Editable) {
                //after the change calling the method and passing the search input

            }
        })*/


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}