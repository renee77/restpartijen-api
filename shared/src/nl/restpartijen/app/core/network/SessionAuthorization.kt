package nl.restpartijen.app.core.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.firstOrNull
import nl.restpartijen.app.core.session.SessionStore

/** Settings of [SessionAuthorization]. [sessionStore] must be set when the plugin is installed. */
class SessionAuthorizationConfig {
    var sessionStore: SessionStore? = null
}

/**
 * Adds `Authorization: Bearer <token>` to every request when there is a session (GB-3).
 * The session is read again for every request, so a cleared session stops the header at once.
 */
val SessionAuthorization =
    createClientPlugin("SessionAuthorization", ::SessionAuthorizationConfig) {
        // The sessionStore must be set when the plugin is installed.
        val store = checkNotNull(pluginConfig.sessionStore) { "SessionAuthorization needs a sessionStore" }

        // Add the Authorization header to every request when there is a session.
        onRequest { request, _ ->
            val session = store.session.firstOrNull()
            if (session != null) {
                request.headers.append(HttpHeaders.Authorization, "Bearer ${session.token}")
            }
        }
    }
