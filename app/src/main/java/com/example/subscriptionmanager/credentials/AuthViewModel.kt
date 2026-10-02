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

// In case we want to replace it with something else
interface AccountRepository {
    suspend fun register(username: String, password: String): Result<Unit>
    suspend fun authenticate(username: String, password: String): Result<Unit>
}

class LocalAccountRepository(context: Context) : AccountRepository {
    private val prefs = context.applicationContext
        .getSharedPreferences("accounts", Context.MODE_PRIVATE)

    override suspend fun register(username: String, password: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            val key = key(username)
            if (prefs.contains(key)) {
                return@withContext Result.failure(Exception("That username is already taken"))
            }
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            prefs.edit(commit = true) { putString(key, "${encode(salt)}:${encode(hash(password, salt))}") }
            Result.success(Unit)
        }

    override suspend fun authenticate(username: String, password: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            val stored = prefs.getString(key(username), null)
                ?: return@withContext Result.failure(Exception("Incorrect username or password"))
            val (saltB64, hashB64) = stored.split(":")
            val actual = hash(password, Base64.Default.decode(saltB64))
            if (MessageDigest.isEqual(actual, Base64.Default.decode(hashB64))) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Incorrect username or password"))
            }
        }

    private fun key(username: String) = "user_" + username.trim().lowercase()

    private fun hash(password: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(password.toCharArray(), salt, 120_000, 256))
            .encoded

    private fun encode(bytes: ByteArray) = Base64.Default.encode(bytes)
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val accounts: AccountRepository = LocalAccountRepository(app)

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var loading by mutableStateOf(false)
        private set

    fun onUsernameChange(value: String) { username = value; error = null }
    fun onPasswordChange(value: String) { password = value; error = null }

    private fun validate(): String? = when {
        username.isBlank() -> "Please enter a username"
        password.length < MIN_PASSWORD -> "Password must be at least $MIN_PASSWORD characters"
        else -> null
    }

    fun signUp(onSuccess: () -> Unit) {
        validate()?.let { error = it; return }
        viewModelScope.launch {
            loading = true
            validate()?.let { error = it; loading = false; return@launch }
            accounts.register(username.trim(), password)
                .onFailure { error = it.message ?: "Could not create account"; loading = false; return@launch }
            loading = false
            onSuccess()
        }
    }

    fun logIn(onSuccess: () -> Unit) {
        viewModelScope.launch {
            loading = true
            validate()?.let { error = it; loading = false; return@launch }
            accounts.authenticate(username.trim(), password)
                .onSuccess { onSuccess() }
                .onFailure { error = it.message ?: "Incorrect username or password" }
            loading = false
        }
    }

    private companion object { const val MIN_PASSWORD = 8 }
}