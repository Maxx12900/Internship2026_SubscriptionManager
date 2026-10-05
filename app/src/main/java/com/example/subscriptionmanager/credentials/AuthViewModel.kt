package com.example.subscriptionmanager.credentials

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.io.encoding.Base64
import androidx.core.content.edit

class LocalAccountRepository(context: Context){
    private val key = "user"
    private val prefs = context.applicationContext
        .getSharedPreferences("accounts", Context.MODE_PRIVATE)

    fun isRegistered(): Boolean {
        return prefs.getBoolean("registered", false)
    }

    suspend fun register(password: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            prefs.edit(commit = true) { putString(key, "${encode(salt)}:${encode(hash(password, salt))}") }
            prefs.edit(commit = true) { putBoolean("registered", true) }
            Result.success(Unit)
        }

    suspend fun authenticate(password: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            val stored = prefs.getString(key, null)
                ?: return@withContext Result.failure(Exception("Account does not exist"))
            val (saltB64, hashB64) = stored.split(":")
            val actual = hash(password, Base64.Default.decode(saltB64))
            if (MessageDigest.isEqual(actual, Base64.Default.decode(hashB64))) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Incorrect password"))
            }
        }

    private fun hash(password: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(password.toCharArray(), salt, 120_000, 256))
            .encoded

    private fun encode(bytes: ByteArray) = Base64.Default.encode(bytes)
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val accounts = LocalAccountRepository(app)
    var password by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    fun onPasswordChange(value: String) { password = value; error = null }

    fun isRegistered(): Boolean {
        return accounts.isRegistered()
    }

    private fun validate(): String? = when {
        password.length < MIN_PASSWORD -> "Password must be at least $MIN_PASSWORD characters"
        else -> null
    }

    fun signUp(onSuccess: () -> Unit) {
        validate()?.let { error = it; return }
        viewModelScope.launch {
            loading = true
            validate()?.let { error = it; loading = false; return@launch }
            accounts.register(password)
                .onFailure { error = it.message ?: "Could not create account"; loading = false; return@launch }
            loading = false
            onSuccess()
        }
    }

    fun logIn(onSuccess: () -> Unit) {
        viewModelScope.launch {
            loading = true
            validate()?.let { error = it; loading = false; return@launch }
            accounts.authenticate(password)
                .onSuccess { onSuccess() }
                .onFailure { error = it.message ?: "Incorrect password" }
            loading = false
        }
    }

    private companion object { const val MIN_PASSWORD = 8 }
}