package nl.restpartijen.app.core.network

/** Why a call to the API failed (FTD §11.2). Sealed, so a ViewModel handles every kind (Stefan, 14 Nov). */
sealed interface ApiError {
    /**
     * The server answered with an error. [message] is the server's English text: log it, never show it;
     * the screen shows its own text from strings.xml. [field] names the invalid input, if any.
     */
    data class Http(val status: Int, val message: String, val field: String? = null) : ApiError

    /** No answer: no network or a time-out, so there is no status code. */
    data object Network : ApiError
}
