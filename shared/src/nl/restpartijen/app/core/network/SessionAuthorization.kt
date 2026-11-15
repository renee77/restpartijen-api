package nl.restpartijen.app.core.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.firstOrNull
import nl.restpartijen.app.core.session.SessionStore

/**
 * Settings of [SessionAuthorization]. [sessionStore] must be set when the plugin is installed.
 * [allowedHost] limits the header to one host; when it is null, every host gets it.
 */
class SessionAuthorizationConfig {
    var sessionStore: SessionStore? = null
    var allowedHost: String? = null
}

/**
 * Adds `Authorization: Bearer <token>` to a request when there is a session (GB-3).
 * The session is read again for every request, so a cleared session stops the header at once.
 * A request that already has an Authorization header keeps it, and a request to another host
 * than [SessionAuthorizationConfig.allowedHost] gets none, so the token never leaves for a third party.
 */
val SessionAuthorization =
    createClientPlugin("SessionAuthorization", ::SessionAuthorizationConfig) {
        // The session store is required, so we check it once when the plugin is installed.
        val store = checkNotNull(pluginConfig.sessionStore) { "SessionAuthorization needs a sessionStore" }
        // The allowed host is optional, so we just read it once.
        val allowedHost = pluginConfig.allowedHost

        // The onRequest block is called for every request, so we can read the session and add the header.
        onRequest { request, _ ->
            val hasHeader = request.headers.contains(HttpHeaders.Authorization)
            val otherHost = allowedHost != null && request.url.host != allowedHost
            if (hasHeader || otherHost) return@onRequest

            // Read the session from the store. This is a suspend function, so it can be called here.
            val session = store.session.firstOrNull()
            if (session != null) {
                request.headers.append(HttpHeaders.Authorization, "Bearer ${session.token}")
            }
        }
        // TEMPORARY (step 2): only look at what this hook sees. Remove in step 3.
        on(Send) { request ->
            val call = proceed(request)
            println("SEND-HOOK saw status ${call.response.status}")
            call
        }
    }
