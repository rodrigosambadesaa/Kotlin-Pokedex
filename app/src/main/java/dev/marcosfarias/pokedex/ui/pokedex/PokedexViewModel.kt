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
        initNetworkRequest()
    }

    fun checkAndFetchData(context: Context) {
        appConnectivityManager.checkConnectivity(context) { result ->
            _connectivityResult.postValue(result)
            if (result.isReachable) {
                initNetworkRequest()
            }
        }
    }

    private fun initNetworkRequest() {
        val call = pokemonService.get()

        call.enqueue(object : Callback<List<Pokemon>?> {
            override fun onResponse(
                call: Call<List<Pokemon>?>?,
                response: Response<List<Pokemon>?>?
            ) {
                response?.body()?.let { pokemons: List<Pokemon> ->
                    thread {
                        pokemonDAO.add(pokemons)
                    }
                }
            }

            override fun onFailure(call: Call<List<Pokemon>?>?, t: Throwable?) {
                // TODO handle failure
            }
        })
    }

    fun getListPokemon() = pokemonDAO.all()
}

