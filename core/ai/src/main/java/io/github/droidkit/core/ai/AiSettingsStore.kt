package io.github.droidkit.core.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.aiStore by preferencesDataStore(name = "ai")

/**
 * AI preferences + BYOK key storage. The API key is AES-256-GCM encrypted
 * under an Android Keystore key and only the ciphertext is persisted —
 * the plaintext never touches disk, BuildConfig, or logs.
 */
@Singleton
class AiSettingsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val enabledKey = booleanPreferencesKey("ai_enabled")
    private val autoSummarizeKey = booleanPreferencesKey("ai_auto_summarize")
    private val modelKey = stringPreferencesKey("ai_model")
    private val keyCipherKey = stringPreferencesKey("ai_key_cipher")
    private val keyIvKey = stringPreferencesKey("ai_key_iv")

    val enabled: Flow<Boolean> = context.aiStore.data.map { it[enabledKey] ?: false }
    val autoSummarize: Flow<Boolean> = context.aiStore.data.map { it[autoSummarizeKey] ?: false }
    val model: Flow<String> = context.aiStore.data.map { it[modelKey] ?: DEFAULT_MODEL }
    val hasKey: Flow<Boolean> = context.aiStore.data.map { !it[keyCipherKey].isNullOrBlank() }

    suspend fun setEnabled(value: Boolean) {
        context.aiStore.edit { it[enabledKey] = value }
    }

    suspend fun setAutoSummarize(value: Boolean) {
        context.aiStore.edit { it[autoSummarizeKey] = value }
    }

    suspend fun setModel(value: String) {
        context.aiStore.edit { it[modelKey] = value }
    }

    /** Encrypts and stores the user's Groq API key. */
    suspend fun saveApiKey(plain: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        context.aiStore.edit {
            it[keyCipherKey] = android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
            it[keyIvKey] = android.util.Base64.encodeToString(cipher.iv, android.util.Base64.NO_WRAP)
        }
    }

    /** Decrypts the stored key, or null when none is saved. */
    suspend fun apiKey(): String? {
        val prefs = context.aiStore.data.first()
        val cipherB64 = prefs[keyCipherKey] ?: return null
        val ivB64 = prefs[keyIvKey] ?: return null
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(
                    GCM_TAG_BITS,
                    android.util.Base64.decode(ivB64, android.util.Base64.NO_WRAP),
                ),
            )
            String(
                cipher.doFinal(android.util.Base64.decode(cipherB64, android.util.Base64.NO_WRAP)),
                Charsets.UTF_8,
            )
        }.getOrNull()
    }

    /** Wipes key material — Disconnect. */
    suspend fun clearApiKey() {
        context.aiStore.edit {
            it.remove(keyCipherKey)
            it.remove(keyIvKey)
        }
    }

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    companion object {
        const val DEFAULT_MODEL = "openai/gpt-oss-120b"
        val MODEL_CHOICES = listOf(
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "qwen/qwen3.8-27b",
        )
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "droidkit_ai_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}
