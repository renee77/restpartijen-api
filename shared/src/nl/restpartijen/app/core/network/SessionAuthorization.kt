package nl.restpartijen.app.core.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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

// The one place that knows what the header value looks like, so both hooks always agree.
private fun bearer(token: String) = "Bearer $token"

/**
 * Handles the session for every request (GB-3):
 * - L4: adds `Authorization: Bearer <token>` when there is a session. A request that already has
 *   an Authorization header keeps it, and a request to another host than
 *   [SessionAuthorizationConfig.allowedHost] gets none.
 * - L5: when the answer is 401, clears the session, but only if the request carried the token of
 *   the current session. The 401 itself is passed on unchanged, so the repository can turn it
 *   into `ApiError.Http(401)` (A-22) and the screen can ask for a login (A-27).
 *
 * The session is read again for every request, so nothing is remembered between requests.
 */
val SessionAuthorization =
    createClientPlugin("SessionAuthorization", ::SessionAuthorizationConfig) {
        // The session store is required, so we check it once when the plugin is installed.
        val store = checkNotNull(pluginConfig.sessionStore) { "SessionAuthorization needs a sessionStore" }
        // The allowed host is optional, so we just read it once.
        val allowedHost = pluginConfig.allowedHost

        // Hook 1 (L4): runs before the request is sent and sets the header.
        onRequest { request, _ ->
            val hasHeader = request.headers.contains(HttpHeaders.Authorization)
            val otherHost = allowedHost != null && request.url.host != allowedHost
            if (hasHeader || otherHost) return@onRequest

            // Read the session from the store. This is a suspend function, so it can be called here.
            val session = store.session.firstOrNull()
            if (session != null) {
                request.headers.append(HttpHeaders.Authorization, bearer(session.token))
            }
        }

        // Hook 2 (L5): wraps the sending of the request and sees the answer before the
        // `expectSuccess` check can turn a 4xx into an exception.
        on(Send) { request ->
            // The header that really goes out; hook 1 has already run, so it is set by now.
            // A request without a header (login, other host, no session) leaves this null.
            val sentAuthorization = request.headers[HttpHeaders.Authorization]

            val call = proceed(request)

            if (call.response.status == HttpStatusCode.Unauthorized && sentAuthorization != null) {
                // Read the session again: the user may have logged in anew while this request
                // was on its way. Only clear when the request carried the current token.
                val current = store.session.firstOrNull()
                if (current != null && sentAuthorization == bearer(current.token)) {
                    store.clear()
                }
            }

            // Always hand the answer untouched; the caller still has to see the 401.
            call
        }
    }
