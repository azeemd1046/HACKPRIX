package com.example.data

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object NetworkClients {
    private const val TAG = "NetworkClients"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json".toMediaType()

    // Retrieve Supabase credentials. Checked at runtime.
    fun hasSupabaseConfig(): Boolean {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        return url.isNotEmpty() && !url.contains("YOUR_") && key.isNotEmpty() && !key.contains("YOUR_")
    }

    // Retrieve Gemini credentials. Checked at runtime.
    fun hasGeminiConfig(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return apiKey.isNotEmpty() && !apiKey.contains("MY_") && !apiKey.contains("Placeholder")
    }

    // ------------------------------------------------------------------------
    // SUPABASE AUTH & DATABASE CALLS (Direct REST & Auth API)
    // ------------------------------------------------------------------------

    // Logs in an existing email/password user
    suspend fun supabaseLogin(email: String, password: String): String? = withContext(Dispatchers.IO) {
        if (!hasSupabaseConfig()) return@withContext null
        try {
            val url = "${BuildConfig.SUPABASE_URL}/auth/v1/token?grant_type=password"
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    Log.e(TAG, "Login failed: $bodyStr")
                    throw Exception("Login failed: ${parseError(bodyStr)}")
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyStr)
                return@withContext json.getString("access_token")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login exception", e)
            throw e
        }
    }

    // Signs up a new email/password user
    suspend fun supabaseSignUp(email: String, password: String): String? = withContext(Dispatchers.IO) {
        if (!hasSupabaseConfig()) return@withContext null
        try {
            val url = "${BuildConfig.SUPABASE_URL}/auth/v1/signup"
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    Log.e(TAG, "Sign Up failed: $bodyStr")
                    throw Exception("Signup failed: ${parseError(bodyStr)}")
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyStr)
                // If auto-confirm is enabled or response has active session, return token
                return@withContext json.optString("access_token", "pending_confirmation")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign Up exception", e)
            throw e
        }
    }

    // Inserts/Updates a profile in Supabase db
    suspend fun supabaseUpsertProfile(
        email: String,
        fullName: String,
        branch: String,
        year: String,
        careerGoal: String,
        currentLevel: String,
        interests: String,
        token: String?
    ): Boolean = withContext(Dispatchers.IO) {
        if (!hasSupabaseConfig()) return@withContext false
        try {
            val url = "${BuildConfig.SUPABASE_URL}/rest/v1/profiles"
            val profileJson = JSONObject().apply {
                put("id", email) // Using email or custom id
                put("email", email)
                put("full_name", fullName)
                put("branch", branch)
                put("year", year)
                put("career_goal", careerGoal)
                put("current_level", currentLevel)
                put("interests", interests)
                put("created_at", System.currentTimeMillis())
            }

            // Create array payload
            val arrayBody = JSONArray().put(profileJson).toString()

            val reqBuilder = Request.Builder()
                .url(url)
                .post(arrayBody.toRequestBody(JSON_MEDIA_TYPE))
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")

            if (!token.isNullOrEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }

            client.newCall(reqBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    Log.e(TAG, "Profile Save failed: $bodyStr")
                    return@withContext false
                }
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Profile Save exception", e)
            return@withContext false
        }
    }

    private fun parseError(bodyStr: String): String {
        return try {
            val json = JSONObject(bodyStr)
            json.optString("msg", json.optString("error_description", "Unknown error"))
        } catch (e: Exception) {
            "Unknown error occurred"
        }
    }

    // ------------------------------------------------------------------------
    // GEMINI AI INTEGRATION (REST Call conforming to Option B)
    // ------------------------------------------------------------------------

    suspend fun generateGeminiResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        if (!hasGeminiConfig()) return@withContext null
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            // Conforming to 'gemini-3.5-flash' as recommended by gemini-api/SKILL.md
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(
                            JSONObject().apply {
                                put("text", prompt)
                            }
                        ))
                    }
                ))
            }.toString()

            val request = Request.Builder()
                .url(url)
                .post(jsonRequest.toRequestBody(JSON_MEDIA_TYPE))
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorStr = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API failed: $errorStr")
                    return@withContext null
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.getJSONArray("candidates")
                val contentObj = candidates.getJSONObject(0).getJSONObject("content")
                val parts = contentObj.getJSONArray("parts")
                return@withContext parts.getJSONObject(0).getString("text")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call exception", e)
            return@withContext null
        }
    }
}
