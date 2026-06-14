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
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
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

    // Fetches user profile/email/metadata from Supabase auth with JWT token
    suspend fun fetchSupabaseUser(token: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        if (!hasSupabaseConfig()) return@withContext null
        try {
            val url = "${BuildConfig.SUPABASE_URL}/auth/v1/user"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    Log.e(TAG, "Fetch user failed: $bodyStr")
                    return@withContext null
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyStr)
                val email = json.getString("email")
                val userMetadata = json.optJSONObject("user_metadata")
                val fullName = userMetadata?.optString("full_name") ?: userMetadata?.optString("name") ?: "Skill Scholar"
                return@withContext Pair(email, fullName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch user exception", e)
            return@withContext null
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

    // Inserts/Updates a roadmap JSON in Supabase db
    suspend fun supabaseUpsertRoadmap(
        email: String,
        roadmapJson: String,
        token: String?
    ): Boolean = withContext(Dispatchers.IO) {
        if (!hasSupabaseConfig()) return@withContext false
        try {
            val url = "${BuildConfig.SUPABASE_URL}/rest/v1/roadmaps"
            val json = JSONObject().apply {
                put("id", email)
                put("user_id", email)
                put("email", email)
                put("roadmap_json", roadmapJson)
                put("created_at", System.currentTimeMillis())
            }

            val arrayBody = JSONArray().put(json).toString()
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
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Roadmap Save failed", e)
            return@withContext false
        }
    }

    // Fetches roadmap JSON from Supabase db
    suspend fun supabaseFetchRoadmap(email: String, token: String?): String? = withContext(Dispatchers.IO) {
        if (!hasSupabaseConfig()) return@withContext null
        try {
            val url = "${BuildConfig.SUPABASE_URL}/rest/v1/roadmaps?or=(user_id.eq.$email,email.eq.$email,id.eq.$email)&select=*"
            val reqBuilder = Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)

            if (!token.isNullOrEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }

            client.newCall(reqBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                val array = JSONArray(bodyStr)
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    return@withContext obj.optString("roadmap_json", null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Roadmap Fetch failed", e)
        }
        return@withContext null
    }

    // Inserts/Updates a skill analysis in Supabase db
    suspend fun supabaseUpsertSkillAnalysis(
        email: String,
        currentSkills: String,
        targetCareer: String,
        analysisJson: String,
        token: String?
    ): Boolean = withContext(Dispatchers.IO) {
        if (!hasSupabaseConfig()) return@withContext false
        try {
            val url = "${BuildConfig.SUPABASE_URL}/rest/v1/skill_analysis"
            val json = JSONObject().apply {
                put("id", email)
                put("user_id", email)
                put("email", email)
                put("current_skills", currentSkills)
                put("target_career", targetCareer)
                put("analysis_json", analysisJson)
                put("created_at", System.currentTimeMillis())
            }

            val arrayBody = JSONArray().put(json).toString()
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
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Skill Analysis Save failed", e)
            return@withContext false
        }
    }

    // Fetches skill analysis JSON from Supabase db
    suspend fun supabaseFetchSkillAnalysis(email: String, token: String?): String? = withContext(Dispatchers.IO) {
        if (!hasSupabaseConfig()) return@withContext null
        try {
            val url = "${BuildConfig.SUPABASE_URL}/rest/v1/skill_analysis?or=(user_id.eq.$email,email.eq.$email,id.eq.$email)&select=*"
            val reqBuilder = Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)

            if (!token.isNullOrEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }

            client.newCall(reqBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                val array = JSONArray(bodyStr)
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    return@withContext obj.optString("analysis_json", null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Skill Analysis Fetch failed", e)
        }
        return@withContext null
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
        if (!hasGeminiConfig()) {
            throw Exception("Gemini key not configured. Please add GEMINI_API_KEY.")
        }
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
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
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
                    throw Exception("Unable to generate roadmap. Please try again.")
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.getJSONArray("candidates")
                val contentObj = candidates.getJSONObject(0).getJSONObject("content")
                val parts = contentObj.getJSONArray("parts")
                return@withContext parts.getJSONObject(0).getString("text")
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Gemini call network exception", e)
            throw Exception("Check your internet connection.")
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Gemini call network exception", e)
            throw Exception("Check your internet connection.")
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call exception", e)
            if (e.message != null && (e.message!!.contains("Gemini key not configured") || e.message!!.contains("internet"))) {
                throw e
            }
            throw Exception("Unable to generate roadmap. Please try again.")
        }
    }

    suspend fun generateGeminiChat(
        systemInstruction: String,
        history: List<Pair<String, String>>,
        userInput: String
    ): String? = withContext(Dispatchers.IO) {
        if (!hasGeminiConfig()) {
            throw Exception("Gemini key not configured. Please add GEMINI_API_KEY.")
        }
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val jsonRequest = JSONObject().apply {
                val contentsArray = JSONArray()
                for (turn in history) {
                    contentsArray.put(JSONObject().apply {
                        put("role", turn.first) // "user" or "model" (model works for assistant)
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", turn.second)
                        }))
                    })
                }
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", userInput)
                    }))
                })
                put("contents", contentsArray)

                if (systemInstruction.isNotEmpty()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", systemInstruction)
                        }))
                    })
                }
            }.toString()

            val request = Request.Builder()
                .url(url)
                .post(jsonRequest.toRequestBody(JSON_MEDIA_TYPE))
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorStr = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini Chat API failed: $errorStr")
                    throw Exception("Unable to get coach response. Please try again.")
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.getJSONArray("candidates")
                val contentObj = candidates.getJSONObject(0).getJSONObject("content")
                val parts = contentObj.getJSONArray("parts")
                return@withContext parts.getJSONObject(0).getString("text")
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Gemini Chat network exception", e)
            throw Exception("Check your internet connection.")
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Gemini Chat network exception", e)
            throw Exception("Check your internet connection.")
        } catch (e: Exception) {
            Log.e(TAG, "Gemini Chat general exception", e)
            throw e
        }
    }
}
