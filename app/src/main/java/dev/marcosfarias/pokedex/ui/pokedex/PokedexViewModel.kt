package dev.marcosfarias.pokedex.ui.pokedex

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.marcosfarias.pokedex.database.dao.PokemonDAO
import dev.marcosfarias.pokedex.model.Pokemon
import dev.marcosfarias.pokedex.repository.PokemonService
import dev.marcosfarias.pokedex.utils.AppConnectivityManager
import dev.marcosfarias.pokedex.utils.AppConnectivityResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class PokedexViewModel(
    private val pokemonDAO: PokemonDAO,
    private val pokemonService: PokemonService,
    private val appConnectivityManager: AppConnectivityManager
) : ViewModel() {

    private val _connectivityResult = MutableLiveData<AppConnectivityResult>()
    val connectivityResult: LiveData<AppConnectivityResult> get() = _connectivityResult

    init {
        initNetworkRequest(null)
    }

    fun checkAndFetchData(context: Context) {
        appConnectivityManager.checkConnectivity(context) { result ->
            _connectivityResult.postValue(result)
            if (result.isReachable) {
                initNetworkRequest(context)
            } else {
                loadLocalAssets(context)
            }
        }
    }

    private fun initNetworkRequest(context: Context?) {
        val call = pokemonService.get()

        call.enqueue(object : Callback<List<Pokemon>?> {
            override fun onResponse(
                call: Call<List<Pokemon>?>,
                response: Response<List<Pokemon>?>
            ) {
                val pokemons = response.body()
                if (!pokemons.isNullOrEmpty()) {
                    thread {
                        pokemonDAO.add(pokemons)
                    }
                } else if (context != null) {
                    loadLocalAssets(context)
                }
            }

            override fun onFailure(call: Call<List<Pokemon>?>, t: Throwable) {
                if (context != null) {
                    loadLocalAssets(context)
                }
            }
        })
    }

    private fun loadLocalAssets(context: Context) {
        thread {
            try {
                val jsonString = context.assets.open("pokemon.json").bufferedReader().use { it.readText() }
                val listType = object : com.google.gson.reflect.TypeToken<List<Pokemon>>() {}.type
                val pokemons: List<Pokemon> = com.google.gson.Gson().fromJson(jsonString, listType)
                if (!pokemons.isNullOrEmpty()) {
                    pokemonDAO.add(pokemons)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getListPokemon() = pokemonDAO.all()
}

