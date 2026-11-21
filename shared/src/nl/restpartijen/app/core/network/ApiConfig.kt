package nl.restpartijen.app.core.network

/**
 * Where the app finds the API: the only place that holds its address (FTD §11.1).
 *
 * A plain Kotlin object, because the Kotlin Toolchain cannot set a value per build type: it has no
 * buildConfigField or resValue (O-14). Running on a real phone instead of the emulator therefore means
 * changing [HOST] here and building again.
 */
object ApiConfig {
    // The emulator's alias for 127.0.0.1 on the development machine, where the server runs.
    // A real phone needs the laptop's address in the local network instead (O-14).
    const val HOST = "10.0.2.2"

    // The port from server/resources/application.yaml.
    const val PORT = 8080

    // Built from HOST, so the two cannot drift apart. http until the API has https (A-07, W-A1).
    // The trailing slash is required: without it, Ktor replaces the last segment ("v1") with the request path.
    const val BASE_URL = "http://$HOST:$PORT/api/v1/"
}
