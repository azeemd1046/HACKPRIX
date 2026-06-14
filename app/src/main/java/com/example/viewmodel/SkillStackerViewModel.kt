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

    private val _activeOAuthUrl = MutableStateFlow<String?>(null)
    val activeOAuthUrl: StateFlow<String?> = _activeOAuthUrl.asStateFlow()

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

    // Daily Coach state holders
    private val _dailyRecommendation = MutableStateFlow<DailyRecommendation?>(null)
    val dailyRecommendation: StateFlow<DailyRecommendation?> = _dailyRecommendation.asStateFlow()

    private val _isGeneratingDailyCoach = MutableStateFlow(false)
    val isGeneratingDailyCoach: StateFlow<Boolean> = _isGeneratingDailyCoach.asStateFlow()

    // Career Match state holders
    private val _careerMatchesList = MutableStateFlow<List<CareerMatch>?>(null)
    val careerMatchesList: StateFlow<List<CareerMatch>?> = _careerMatchesList.asStateFlow()

    private val _isGeneratingCareerMatches = MutableStateFlow(false)
    val isGeneratingCareerMatches: StateFlow<Boolean> = _isGeneratingCareerMatches.asStateFlow()

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
                    _isLoading.value = true
                    try {
                        val token = prefs.getString("auth_token", null)
                        repository.fetchAndSyncSupabaseData(cachedUser, token)
                        refreshFromCache()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        _isLoading.value = false
                    }
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

    fun refreshFromCache() {
        val cachedReport = repository.getCachedSkillGapReport()
        if (cachedReport != null) {
            _skillGapReport.value = cachedReport
        }
        val cachedDaily = repository.getCachedDailyRecommendation()
        if (cachedDaily != null) {
            _dailyRecommendation.value = cachedDaily
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
        if (!NetworkClients.hasSupabaseConfig()) {
            _statusMessage.value = "Supabase is not configured. Please use the Secrets panel to add credentials."
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val authUrl = "${com.example.BuildConfig.SUPABASE_URL}/auth/v1/authorize?provider=google&redirect_to=skillstacker://login"
                _activeOAuthUrl.value = authUrl
                _statusMessage.value = "Opening Google Sign-In secure portal..."
            } catch (e: Exception) {
                _statusMessage.value = "Failed to launch Google Sign-In: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissOAuthFlow() {
        _activeOAuthUrl.value = null
    }

    fun handleSupabaseOauthCallback(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Authenticating with Supabase..."
            try {
                val user = NetworkClients.fetchSupabaseUser(token)
                if (user != null) {
                    val (email, fullName) = user
                    prefs.edit()
                        .putString("user_email", email)
                        .putString("auth_token", token)
                        .apply()
                        
                    _currentUserEmail.value = email
                    onboardFullName.value = fullName
                    _statusMessage.value = "Google Authentication success!"
                    checkSession()
                } else {
                    _statusMessage.value = "Failed to fetch user session from Supabase."
                }
            } catch (e: Exception) {
                _statusMessage.value = "OAuth Callback Error: ${e.localizedMessage}"
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
        val email = _currentUserEmail.value ?: "offline_user@gmail.com"
        val token = prefs.getString("auth_token", null)
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, completed, email, token)
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
        val email = _currentUserEmail.value ?: "offline_user@gmail.com"
        val token = prefs.getString("auth_token", null)

        viewModelScope.launch {
            _isGeneratingRoadmap.value = true
            _statusMessage.value = "Consulting Gemini AI Career Advisor..."
            try {
                val success = repository.generateAiRoadmap(
                    email = email,
                    careerGoal = profile.careerGoal,
                    currentLevel = profile.currentLevel,
                    interests = profile.interests,
                    token = token
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
        val email = _currentUserEmail.value ?: "offline_user@gmail.com"
        val token = prefs.getString("auth_token", null)

        viewModelScope.launch {
            _isAnalyzingGap.value = true
            try {
                val report = repository.analyzeSkillGap(
                    email = email,
                    careerGoal = profile.careerGoal,
                    currentSkills = profile.interests,
                    token = token
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
        val email = _currentUserEmail.value ?: "offline_user@gmail.com"
        val token = prefs.getString("auth_token", null)

        viewModelScope.launch {
            _isAnalyzingGap.value = true
            try {
                val report = repository.analyzeSkillGap(
                    email = email,
                    careerGoal = profile?.careerGoal ?: "Full Stack Developer",
                    currentSkills = profile?.interests ?: "Web Development",
                    token = token
                )
                _skillGapReport.value = report
            } catch (e: Exception) {
                // ignore quiet fallback
            } finally {
                _isAnalyzingGap.value = false
            }
        }
    }

    fun generateDailyRecommendation() {
        val profile = _latestProfile.value ?: return
        viewModelScope.launch {
            _isGeneratingDailyCoach.value = true
            _statusMessage.value = "Consulting Gemini Study Coach..."
            try {
                val completed = roadmapTasks.value.count { it.isCompleted }
                val total = roadmapTasks.value.size
                val progressStr = "$completed of $total tasks completed"
                val rec = repository.generateDailyRecommendation(
                    careerGoal = profile.careerGoal,
                    progress = progressStr,
                    currentSkills = profile.interests
                )
                if (rec != null) {
                    _dailyRecommendation.value = rec
                    _statusMessage.value = "Coach advice generated for today!"
                } else {
                    _statusMessage.value = "Gemini key not configured. Daily recommendation is empty."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Coach consult failed: ${e.message}"
            } finally {
                _isGeneratingDailyCoach.value = false
            }
        }
    }

    fun generateCareerMatches() {
        val b = _latestProfile.value?.branch ?: onboardBranch.value
        val y = _latestProfile.value?.year ?: onboardYear.value
        val ints = _latestProfile.value?.interests ?: onboardInterests.value.joinToString(", ")

        viewModelScope.launch {
            _isGeneratingCareerMatches.value = true
            _statusMessage.value = "Evaluating university pathways on Gemini..."
            try {
                val list = repository.generateCareerMatches(b, y, ints)
                if (list != null) {
                    _careerMatchesList.value = list
                    _statusMessage.value = "Mapped career fits successfully!"
                } else {
                    _statusMessage.value = "Gemini key required to run Career Matcher."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Matching failed: ${e.message}"
            } finally {
                _isGeneratingCareerMatches.value = false
            }
        }
    }

    fun adoptCareerGoal(career: String) {
        viewModelScope.launch {
            val email = _currentUserEmail.value ?: "offline_user@gmail.com"
            _statusMessage.value = "Adopting career goal: $career"
            val profile = _latestProfile.value
            val fullName = profile?.fullName ?: onboardFullName.value.ifEmpty { "Skill Scholar" }
            val branch = profile?.branch ?: onboardBranch.value.ifEmpty { "CSE" }
            val year = profile?.year ?: onboardYear.value.ifEmpty { "1st" }
            val currentLevel = profile?.currentLevel ?: onboardCurrentLevel.value.ifEmpty { "Beginner" }
            val interests = profile?.interests ?: onboardInterests.value.joinToString(", ").ifEmpty { "Web Development" }
            val token = prefs.getString("auth_token", null)

            onboardCareerGoal.value = career
            _isLoading.value = true
            try {
                repository.saveProfile(
                    email, fullName, branch, year, career, currentLevel, interests, token
                )
                _statusMessage.value = "New Career Goal active. Created fresh milestones!"
                triggerDefaultSkillGapAnalysis()
            } catch (e: Exception) {
                _statusMessage.value = "Fail setting goal: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Exposed helpers
    val skillLibrary: List<Skill> = repository.skillLibraryList
    val curatedResources: List<ResourceItem> = repository.curatedResources

    fun getCachedRoadmapJson(): String? = repository.getCachedRoadmapJson()
    fun getCachedSkillGapReport(): SkillGapReport? = repository.getCachedSkillGapReport()

    // ------------------------------------------------------------------------
    // AI CAREER MENTOR CHATBOT SERVICES
    // ------------------------------------------------------------------------
    val isChatOpen = MutableStateFlow(false)
    val chatInput = MutableStateFlow("")

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("bot", "Hello! I am your AI Career Mentor. I'm here to support your professional transition, roadmap development, skill analysis, certifications advice, and interview preparation. What shall we focus on today?")
    ))
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun toggleChat(open: Boolean) {
        isChatOpen.value = open
    }

    fun setChatInput(text: String) {
        chatInput.value = text
    }

    fun triggerQuickAction(actionPrompt: String) {
        sendChatMessage(actionPrompt)
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage("bot", "Hello! I am your AI Career Mentor. I'm here to support your professional transition, roadmap development, skill analysis, certifications advice, and interview preparation. What shall we focus on today?")
        )
    }

    fun sendChatMessage(text: String = chatInput.value) {
        val messageText = text.trim()
        if (messageText.isEmpty()) return

        if (messageText == chatInput.value.trim()) {
            chatInput.value = ""
        }

        val userMsg = ChatMessage("user", messageText)
        _chatHistory.value = _chatHistory.value + userMsg

        viewModelScope.launch {
            _isChatLoading.value = true
            try {
                val profile = _latestProfile.value
                val careerGoal = profile?.careerGoal ?: onboardCareerGoal.value.ifEmpty { "Software Developer" }
                val interests = profile?.interests ?: onboardInterests.value.joinToString(", ").ifEmpty { "Computer Science" }
                val currentLevel = profile?.currentLevel ?: onboardCurrentLevel.value.ifEmpty { "Beginner" }
                val branch = profile?.branch ?: onboardBranch.value.ifEmpty { "CSE" }
                val year = profile?.year ?: onboardYear.value.ifEmpty { "1st" }

                val gapReport = _skillGapReport.value
                val readinessScore = gapReport?.readinessScore ?: 0
                val missingSkills = gapReport?.missingSkills?.joinToString(", ") ?: "No analysis report run yet"
                val recommendations = gapReport?.recommendations?.joinToString(". ") ?: "No general recommendations generated"

                val tasks = _roadmapTasks.value
                val totalCount = tasks.size
                val completedCount = tasks.count { it.isCompleted }
                val percent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

                val taskSummaries = if (tasks.isEmpty()) {
                    "No roadmap milestones established yet. Guide the user to run Gap Analysis to generate milestones."
                } else {
                    tasks.take(15).joinToString("\n") { task ->
                        "- Phase ${task.phase} [${task.phaseTitle}]: ${task.title} (Status: ${if (task.isCompleted) "Completed" else "Incomplete"})"
                    }
                }

                val sysInstruction = """
                    You are the "SkillStacker AI Career Mentor", an expert career coach and technical guidance specialist dedicated to helping the user achieve their career goals.
                    
                    SPECIALIZED SCOPE:
                    Your sole purpose is to serve as a Career Mentor. You are NOT a general-purpose assistant.
                    
                    ALLOWED TOPICS:
                    - Career Guidance
                    - Skill Development
                    - Learning Roadmaps (phases, tasks, and pacing)
                    - Certifications (suitability, exam preparation, paths)
                    - Practical Projects (project ideas, implementation suggestions)
                    - Interview Preparation (mock questions, behavioral and technical tips)
                    - Career Selection (choosing tracks, comparing roles)
                    - Resource Recommendations (courses, books, tutorials)
                    
                    FORBIDDEN TOPICS:
                    - Politics, Government, Elections
                    - Entertainment, Movies, Celebrities, Music
                    - Jokes, Humor, Off-topic creative writing
                    - Current News, Trends unrelated to technology careers
                    - Chatting about unrelated tasks, general coding outside educational career contexts, health, or food.
                    - Random unrelated questions.
                    
                    CONSTRAINT COMPLIANCE RULES:
                    If the user asks about ANY forbidden topic or something unrelated to professional career development, skill learning, or tech education:
                    You MUST respond EXACTLY with this sentence:
                    "I am SkillStacker's Career Mentor and can only assist with career growth, learning paths, skills, certifications, projects, and job readiness."
                    Do NOT provide ANY other text, comments, markdown, or preambles if this condition is triggered!
                    
                    USER PROFILE & PROGRESS CONTEXT:
                    Personalize your responses using the user's active profile and milestones progress listed below:
                    User Name: ${profile?.fullName ?: "Skill Scholar"}
                    User Career Goal: $careerGoal
                    User Interests: $interests
                    Current Level: $currentLevel
                    Academic Branch/Year: $branch / $year
                    
                    SKILL GAP ANALYTICS:
                    Overall readiness percentage: $readinessScore%
                    Missing Skills Identified: $missingSkills
                    Gap Recommendations: $recommendations
                    
                    ROADMAP MILESTONES:
                    Completion Rate: $completedCount/$totalCount completed tasks ($percent%)
                    Active Tasks:
                    $taskSummaries
                    
                    Be professional, encouraging, analytical, and extremely concise. Provide bullet points and actionable, structured lists!
                """.trimIndent()

                val apiHistory = _chatHistory.value.dropLast(1).mapNotNull { msg ->
                    val role = when (msg.sender) {
                        "user" -> "user"
                        "bot" -> "model"
                        else -> null
                    }
                    if (role != null) Pair(role, msg.text) else null
                }

                val response = NetworkClients.generateGeminiChat(
                    systemInstruction = sysInstruction,
                    history = apiHistory,
                    userInput = messageText
                )

                if (response != null) {
                    _chatHistory.value = _chatHistory.value + ChatMessage("bot", response)
                } else {
                    _chatHistory.value = _chatHistory.value + ChatMessage("bot", "I was unable to secure a connection. Please verify your internet connection and try again.")
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Please try again."
                _chatHistory.value = _chatHistory.value + ChatMessage("bot", "Coach consult error: $errorMsg")
            } finally {
                _isChatLoading.value = false
            }
        }
    }
}

data class ChatMessage(
    val sender: String, // "user" or "bot"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
