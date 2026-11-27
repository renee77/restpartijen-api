package nl.restpartijen.app.core.session

/**
 * Picks the store for the app. When [keystoreIsUsed] is false, [encryptedStore] is never called, so nothing is
 * written to disk and the session lives in memory only (O-7, FTD §16.3).
 */
internal fun chooseSessionStore(keystoreIsUsed: Boolean, encryptedStore: () -> SessionStore): SessionStore =
    if (keystoreIsUsed) encryptedStore() else MemorySessionStore()
