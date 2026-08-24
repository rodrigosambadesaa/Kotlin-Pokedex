package dev.marcosfarias.pokedex

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.marcosfarias.pokedex.utils.ConnectivityAndInternetAccess

class MainActivity : AppCompatActivity() {

    private var networkObserver: ConnectivityAndInternetAccess.NetworkObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        networkObserver = ConnectivityAndInternetAccess.observeNetwork(this) { state ->
            Log.d(
                "PokedexConnectivity",
                "Network State Changed: connected=${state.connected}, validated=${state.internetValidated}, captivePortal=${state.captivePortalDetected}"
            )
        }
    }

    override fun onStop() {
        networkObserver?.close()
        networkObserver = null
        super.onStop()
    }
}

