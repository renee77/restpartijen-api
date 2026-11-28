package nl.restpartijen.app.core.session

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException

private const val TAG = "SessionStorage"

/** Name of the file that holds the encrypted session, in the private files of the app. Also listed in L2. */
private const val SESSION_FILE_NAME = "session.bin"

/** Name of the preferences file that holds the Tink key set. Also listed in the backup rules (L2). */
private const val KEYSET_PREFS_NAME = "session_keyset_prefs"

private const val KEYSET_NAME = "session_keyset"
private const val MASTER_KEY_URI = "android-keystore://session_master_key"

private class Keyset(val aead: Aead, val keystoreIsUsed: Boolean)

/**
 * Builds the real [SessionStore] for the app. It registers Tink, creates or opens the key set that the Android
 * Keystore protects, and puts an encrypted DataStore in the private files of the app. This is only checked on
 * the emulator, not in a JVM test. Both files it creates, the session and the key set, must stay out of
 * backups (L2). When the key set cannot be used at all, the session lives in memory only.
 */
internal fun encryptedSessionStore(context: Context): SessionStore {
    val keyset = openKeyset(context) ?: return MemorySessionStore()
    return chooseSessionStore(
        keystoreIsUsed = keyset.keystoreIsUsed,
        encryptedStore = { DataStoreSessionStore(createDataStore(context, keyset.aead)) },
    )
}

private fun openKeyset(context: Context): Keyset? = try {
    AeadConfig.register()
    val manager = AndroidKeysetManager.Builder()
        .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS_NAME)
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri(MASTER_KEY_URI)
        .build()
    val aead = manager.keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    Keyset(aead, manager.isUsingKeystore())
} catch (e: GeneralSecurityException) {
    Log.w(TAG, "The key set cannot be used: ${e::class.simpleName}")
    null
} catch (e: IOException) {
    Log.w(TAG, "The key set cannot be read: ${e::class.simpleName}")
    null
}

private fun createDataStore(context: Context, aead: Aead): DataStore<SessionData> = DataStoreFactory.create(
    serializer = AeadSerializer(aead, SessionDataSerializer),
    corruptionHandler = ReplaceFileCorruptionHandler { SessionData() },
    produceFile = { File(context.filesDir, SESSION_FILE_NAME) },
)
