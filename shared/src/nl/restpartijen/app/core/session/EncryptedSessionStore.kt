package nl.restpartijen.app.core.session

/**
 * The [SessionStore] of the app (A-13, GB-3). It passes every call on to one of two stores: an encrypted
 * DataStore, or, when the Android Keystore cannot be used, a store that lives in memory only. Create it with
 * [create].
 */
public class EncryptedSessionStore private constructor(private val store: SessionStore) : SessionStore by store {
    /**
     * Picks the store. When [keystoreIsUsed] is false, [encryptedStore] is never called, so nothing is written
     * to disk.
     */
    public companion object {
        public fun create(keystoreIsUsed: Boolean, encryptedDataStore: () -> SessionStore): SessionStore =
            if (keystoreIsUsed) {
                encryptedDataStore()
            } else {
                MemorySessionStore()
            }
    }
}
