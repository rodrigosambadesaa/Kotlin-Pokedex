package dev.marcosfarias.pokedex.utils

import android.content.Context

data class AppConnectivityResult(
    val isReachable: Boolean,
    val isAppDomainAvailable: Boolean,
    val reachedEndpoint: String?,
    val isExtremeFallbackUsed: Boolean,
    val diagnosticMessage: String
)

class AppConnectivityManager {

    companion object {
        val APP_PRIMARY_HOSTS = listOf(
            "https://gist.githubusercontent.com/mrcsxsiq/b94dbe9ab67147b642baa9109ce16e44/raw/97811a5df2df7304b5bc4fbb9ee018a0339b8a38/",
            "https://assets.pokemon.com/assets/cms2/img/pokedex/full/001.png",
            "https://raw.githubusercontent.com/",
            "https://pokeapi.co/"
        )

        val EXTREME_FALLBACK_DNS_RESOLVERS = listOf(
            "1.1.1.1",
            "8.8.8.8",
            "9.9.9.9",
            "208.67.222.222"
        )

        val EXTREME_FALLBACK_HOSTS = listOf(
            "https://www.google.com/generate_204",
            "https://www.apple.com/",
            "https://www.amazon.com/"
        )
    }

    private val primaryConnectivity = ConnectivityAndInternetAccess.Builder()
        .setHosts(APP_PRIMARY_HOSTS)
        .build()

    private val fallbackConnectivity = ConnectivityAndInternetAccess.Builder()
        .setDnsResolvers(EXTREME_FALLBACK_DNS_RESOLVERS)
        .setHosts(EXTREME_FALLBACK_HOSTS)
        .build()

    /**
     * Executes a multi-tier connectivity check:
     * 1. Checks primary app domains first.
     * 2. If primary domains fail, falls back to extreme fallback (DNS resolvers + default hosts).
     */
    fun checkConnectivity(
        context: Context,
        onResult: (AppConnectivityResult) -> Unit
    ): ConnectivityAndInternetAccess.Request {
        return primaryConnectivity.checkInternetAsync(context) { primaryResult ->
            if (primaryResult.reachable) {
                onResult(
                    AppConnectivityResult(
                        isReachable = true,
                        isAppDomainAvailable = true,
                        reachedEndpoint = primaryResult.reachedHost,
                        isExtremeFallbackUsed = false,
                        diagnosticMessage = "App domain reachable via ${primaryResult.reachedHost}"
                    )
                )
            } else {
                fallbackConnectivity.checkInternetAsync(context) { fallbackResult ->
                    if (fallbackResult.reachable) {
                        onResult(
                            AppConnectivityResult(
                                isReachable = true,
                                isAppDomainAvailable = false,
                                reachedEndpoint = fallbackResult.reachedHost,
                                isExtremeFallbackUsed = true,
                                diagnosticMessage = "General Internet reachable via ${fallbackResult.reachedHost}, but primary app domains unreachable."
                            )
                        )
                    } else {
                        onResult(
                            AppConnectivityResult(
                                isReachable = false,
                                isAppDomainAvailable = false,
                                reachedEndpoint = null,
                                isExtremeFallbackUsed = true,
                                diagnosticMessage = "No Internet connection detected."
                            )
                        )
                    }
                }
            }
        }
    }

    fun observeNetwork(
        context: Context,
        callback: ConnectivityAndInternetAccess.NetworkStateCallback
    ): ConnectivityAndInternetAccess.NetworkObserver {
        return ConnectivityAndInternetAccess.observeNetwork(context, callback)
    }
}
