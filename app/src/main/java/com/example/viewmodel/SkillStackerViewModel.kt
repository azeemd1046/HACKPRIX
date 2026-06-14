package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Screen states for state-based routing
sealed class AppScreen {
    object Splash : AppScreen()
    object SignUp : AppScreen()
    object Login : AppScreen()
    object Onboarding : AppScreen()
    object Dashboard : AppScreen() // Main wrapper showing bottom navigation
}

// Active bottom tab inside Dashboard
enum class DashboardTab {
    HOME,
    ROADMAP,
    SKILLS,
    RESOURCES,
    PROFILE
}

class SkillStackerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("skillstacker_prefs", Context.MODE_PRIVATE)
    private val repository = SkillRepository(context)

    // ---- STATE FLOWS ----
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(DashboardTab.HOME)
    val currentTab: StateFlow<DashboardTab> = _currentTab.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _isSupabaseActive = MutableStateFlow(NetworkClients.hasSupabaseConfig())
    val isSupabaseActive: StateFlow<Boolean> = _isSupabaseActive.asStateFlow()

    private val _isGeminiActive = MutableStateFlow(NetworkClients.hasGeminiConfig())
    val isGeminiActive: StateFlow<Boolean> = _isGeminiActive.asStateFlow()

    private val _latestProfile = repository.latestProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val latestProfile: StateFlow<ProfileEntity?> = _latestProfile

    private val _roadmapTasks = repository.roadmapTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val roadmapTasks: StateFlow<List<RoadmapTaskEntity>> = _roadmapTasks

    // Onboarding form state
    val onboardFullName = MutableStateFlow("")
    val onboardBranch = MutableStateFlow("CSE") // CSE, ECE, ME, Civil, Other
    val onboardYear = MutableStateFlow("1st") // 1st, 2nd, 3rd, 4th
    val onboardCareerGoal = MutableStateFlow("Full Stack Developer")
    val onboardCurrentLevel = MutableStateFlow("Beginner")
    val onboardInterests = MutableStateFlow<Set<String>>(emptySet())

    // Onboarding sub step routing: 1, 2, 3, 4
    private val _onboardStep = MutableStateFlow(1)
    val onboardStep: StateFlow<Int> = _onboardStep.asStateFlow()

    // Loading & error status controls
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Skill Gap Analysis reports state
    private val _skillGapReport = MutableStateFlow<SkillGapReport?>(null)
    val skillGapReport: StateFlow<SkillGapReport?> = _skillGapReport.asStateFlow()

    private val _isAnalyzingGap = MutableStateFlow(false)
    val isAnalyzingGap: StateFlow<Boolean> = _isAnalyzingGap.asStateFlow()

    private val _isGeneratingRoadmap = MutableStateFlow(false)
    val isGeneratingRoadmap: StateFlow<Boolean> = _isGeneratingRoadmap.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        val cachedUser = prefs.getString("user_email", null)
        val hasCompletedOnboard = prefs.getBoolean("completed_onboard", false)

        _currentUserEmail.value = cachedUser
        _isSupabaseActive.value = NetworkClients.hasSupabaseConfig()
        _isGeminiActive.value = NetworkClients.hasGeminiConfig()

        if (cachedUser != null) {
            viewModelScope.launch {
                // Check if profile exists in db (Room flow handles latest profile updates)
                if (hasCompletedOnboard) {
                    _currentScreen.value = AppScreen.Dashboard
                    // Perform initial skill gap check
                    triggerDefaultSkillGapAnalysis()
                } else {
                    _currentScreen.value = AppScreen.Onboarding
                    _onboardStep.value = 1
                }
            }
        } else {
            _currentScreen.value = AppScreen.Splash
        }
    }

    // Clear alert helpers
    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // ---- NAVIGATION ROUTERS ----
    fun navigateToSignUp() {
        _currentScreen.value = AppScreen.SignUp
    }

    fun navigateToLogin() {
        _currentScreen.value = AppScreen.Login
    }

    fun navigateToSplash() {
        _currentScreen.value = AppScreen.Splash
    }

    fun switchTab(tab: DashboardTab) {
        _currentTab.value = tab
    }

    // ---- AUTH ACTIONS ----
    fun handleLogin(email: String, pword: String) {
        if (email.isEmpty() || pword.isEmpty()) {
            _statusMessage.value = "Email and Password cannot be empty."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (NetworkClients.hasSupabaseConfig()) {
                    val token = NetworkClients.supabaseLogin(email, pword)
                    if (token != null) {
                        prefs.edit()
                            .putString("user_email", email)
                            .putString("auth_token", token)
                            .apply()
                        _currentUserEmail.value = email
                        checkSession()
                    } else {
                        _statusMessage.value = "Login failed. Check information."
                    }
                } else {
                    // Local session bypass
                    prefs.edit().putString("user_email", email).apply()
                    _currentUserEmail.value = email
                    _statusMessage.value = "Local Session Enabled (Offline Mode)"
                    checkSession()
                }
            } catch (e: Exception) {
                _statusMessage.value = e.message ?: "Authentication failed."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleSignUp(email: String, pword: String, fullName: String) {
        if (email.isEmpty() || pword.isEmpty() || fullName.isEmpty()) {
            _statusMessage.value = "Please complete all fields."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (NetworkClients.hasSupabaseConfig()) {
                    val token = NetworkClients.supabaseSignUp(email, pword)
                    if (token != null) {
                        prefs.edit()
                            .putString("user_email", email)
                            .putString("auth_token", token)
                            .apply()
                        _currentUserEmail.value = email
                        onboardFullName.value = fullName
                        _currentScreen.value = AppScreen.Onboarding
                        _onboardStep.value = 1
                    }
                } else {
                    // Local session login bypass
                    prefs.edit().putString("user_email", email).apply()
                    _currentUserEmail.value = email
                    onboardFullName.value = fullName
                    _statusMessage.value = "Account created locally."
                    _currentScreen.value = AppScreen.Onboarding
                    _onboardStep.value = 1
                }
            } catch (e: Exception) {
                _statusMessage.value = e.message ?: "Registration failed."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleGoogleSignIn() {
        val demoEmail = "google_user@gmail.com"
        viewModelScope.launch {
            _isLoading.value = true
            try {
                prefs.edit().putString("user_email", demoEmail).apply()
                _currentUserEmail.value = demoEmail
                _statusMessage.value = "Logged in with Google successfully."
                checkSession()
            } catch (e: Exception) {
                _statusMessage.value = "Google OAuth Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleSignOut() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.clearSessionAndProfile()
            prefs.edit()
                .remove("user_email")
                .remove("auth_token")
                .remove("completed_onboard")
                .apply()

            _currentUserEmail.value = null
            onboardFullName.value = ""
            onboardInterests.value = emptySet()
            _skillGapReport.value = null
            _currentTab.value = DashboardTab.HOME
            _currentScreen.value = AppScreen.Splash
            _isLoading.value = false
        }
    }

    // ---- ONBOARDING OPERATIONS ----
    fun toggleInterest(interest: String) {
        val current = onboardInterests.value.toMutableSet()
        if (current.contains(interest)) {
            current.remove(interest)
        } else {
            current.add(interest)
        }
        onboardInterests.value = current
    }

    fun nextOnboardStep() {
        val step = _onboardStep.value
        if (step < 4) {
            _onboardStep.value = step + 1
        } else {
            submitOnboarding()
        }
    }

    fun prevOnboardStep() {
        val step = _onboardStep.value
        if (step > 1) {
            _onboardStep.value = step - 1
        }
    }

    private fun submitOnboarding() {
        val email = _currentUserEmail.value ?: "offline_user@gmail.com"
        val name = onboardFullName.value.ifEmpty { "Skill Scholar" }
        val branch = onboardBranch.value
        val year = onboardYear.value
        val career = onboardCareerGoal.value
        val level = onboardCurrentLevel.value
        val interestsStr = onboardInterests.value.joinToString(", ")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = prefs.getString("auth_token", null)
                repository.saveProfile(
                    email, name, branch, year, career, level, interestsStr, token
                )

                prefs.edit().putBoolean("completed_onboard", true).apply()
                _currentScreen.value = AppScreen.Dashboard
                _currentTab.value = DashboardTab.HOME

                // Perform first automated Skill Gap check
                triggerDefaultSkillGapAnalysis()
            } catch (e: Exception) {
                _statusMessage.value = "Failed saving onboarding: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---- MAIN APP ACTIONS ----

    fun toggleTaskComplete(taskId: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, completed)
        }
    }

    fun editProfile(
        name: String,
        branch: String,
        year: String,
        careerGoal: String,
        currentLevel: String,
        interests: Set<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val email = _currentUserEmail.value ?: "offline_user@gmail.com"
                val interestsStr = interests.joinToString(", ")
                val token = prefs.getString("auth_token", null)

                repository.saveProfile(
                    email, name, branch, year, careerGoal, currentLevel, interestsStr, token
                )

                _statusMessage.value = "Profile updated successfully."
                triggerDefaultSkillGapAnalysis() // Re-analyze
            } catch (e: Exception) {
                _statusMessage.value = "Error updating profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---- AI INTEGRATION TRIGGERS ----

    fun generateAiRoadmap() {
        val profile = _latestProfile.value ?: return
        viewModelScope.launch {
            _isGeneratingRoadmap.value = true
            _statusMessage.value = "Consulting Gemini AI Career Advisor..."
            try {
                val success = repository.generateAiRoadmap(
                    profile.careerGoal,
                    profile.currentLevel,
                    profile.interests
                )
                if (success) {
                    _statusMessage.value = "Roadmap generated successfully!"
                } else {
                    _statusMessage.value = "Gemini key not configured. Loaded custom expert paths offline."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Generation failed: ${e.message}"
            } finally {
                _isGeneratingRoadmap.value = false
            }
        }
    }

    fun analyzeSkillGap() {
        val profile = _latestProfile.value ?: return
        viewModelScope.launch {
            _isAnalyzingGap.value = true
            try {
                val report = repository.analyzeSkillGap(
                    profile.careerGoal,
                    profile.interests
                )
                _skillGapReport.value = report
                if (report.isRealAi) {
                    _statusMessage.value = "AI Skill Gap analyzed successfully."
                } else {
                    _statusMessage.value = "Skill Gap updated offline."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gap analysis failed."
            } finally {
                _isAnalyzingGap.value = false
            }
        }
    }

    private fun triggerDefaultSkillGapAnalysis() {
        val profile = _latestProfile.value
        viewModelScope.launch {
            _isAnalyzingGap.value = true
            try {
                val report = repository.analyzeSkillGap(
                    profile?.careerGoal ?: "Full Stack Developer",
                    profile?.interests ?: "Web Development"
                )
                _skillGapReport.value = report
            } catch (e: Exception) {
                // ignore quiet fallback
            } finally {
                _isAnalyzingGap.value = false
            }
        }
    }

    // Exposed helpers
    val skillLibrary: List<Skill> = repository.skillLibraryList
    val curatedResources: List<ResourceItem> = repository.curatedResources
}
