package nl.restpartijen.app.core.session

import android.content.Context

internal class AndroidSessionStorage {

    companion object {
        private const val SESSIONFILENAME = "encrypted_session"
        private const val KEYSETFILENAME = "tink_key_set"

        fun create(context: Context): SessionStore {
            // TODO: Register Tink

            // TODO: Build AndroidKeysetManager

            // TODO: Get keyset handle

            // TODO: Get Aead primitive

            // TODO: Check whether Keystore is used

            // TODO: Create DataStore lazily

            // TODO: Return EncryptedSessionStore.create(...)
            return EncryptedSessionStore.create(
                keystoreIsUsed = false,
                encryptedDataStore = {
                    throw NotImplementedError("AndroidSessionStorage.create is not implemented yet")
                },
            )
        }
    }
}
