package com.example.data

import android.content.Context
import com.example.data.NetworkClients
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

// Skill model for the catalog
data class Skill(
    val name: String,
    val category: String, // Tech, Creative, Business, Finance, Communication
    val difficulty: String, // Beginner, Intermediate, Advanced
    val estimatedTime: String, // e.g., "12 weeks"
    val incomePotential: String // e.g., "$95k/yr"
)

// Resource model
data class ResourceItem(
    val name: String,
    val category: String, // Courses, YouTube Channels, Certifications, Platforms
    val provider: String,
    val description: String,
    val cost: String // Free, Paid
)

class SkillRepository(context: Context) {
    private val appDatabase = AppDatabase.getDatabase(context)
    private val appDao = appDatabase.appDao()

    val latestProfile: Flow<ProfileEntity?> = appDao.getLatestProfile()
    val roadmapTasks: Flow<List<RoadmapTaskEntity>> = appDao.getRoadmapTasks()

    // ------------------------------------------------------------------------
    // PREDEFINED DATA DIRECTORIES
    // ------------------------------------------------------------------------

    val skillLibraryList = listOf(
        // Tech
        Skill("Full Stack Web Dev", "Tech", "Intermediate", "16 Weeks", "$110,000/yr"),
        Skill("Android App Development", "Tech", "Intermediate", "12 Weeks", "$115,000/yr"),
        Skill("Cybersecurity Fundamentals", "Tech", "Beginner", "8 Weeks", "$90,000/yr"),
        Skill("Cloud Architecture (AWS)", "Tech", "Advanced", "14 Weeks", "$130,000/yr"),
        Skill("Python & Data Science", "Tech", "Beginner", "10 Weeks", "$105,000/yr"),
        
        // Creative
        Skill("Professional Video Editing", "Creative", "Intermediate", "8 Weeks", "$75,000/yr"),
        Skill("UI/UX Design Systems", "Creative", "Intermediate", "12 Weeks", "$95,000/yr"),
        Skill("Vector Graphic Illustration", "Creative", "Beginner", "6 Weeks", "$60,000/yr"),
        Skill("Content Writing & Copy", "Creative", "Beginner", "4 Weeks", "$55,000/yr"),
        
        // Business
        Skill("SaaS Entrepreneurship", "Business", "Advanced", "16 Weeks", "$150,000/yr"),
        Skill("Freelance Client Acquisition", "Business", "Beginner", "4 Weeks", "$80,000/yr"),
        Skill("SEO & Growth Marketing", "Business", "Intermediate", "8 Weeks", "$70,000/yr"),
        
        // Finance
        Skill("Personal Finance Mastery", "Finance", "Beginner", "3 Weeks", "Wealth Multiplier"),
        Skill("Stock & Index Fund Investing", "Finance", "Intermediate", "5 Weeks", "Passive Income"),
        Skill("Financial Model Analysis", "Finance", "Advanced", "8 Weeks", "$110,000/yr"),

        // Communication
        Skill("High-Stakes Public Speaking", "Communication", "Intermediate", "6 Weeks", "Leadership Accelerator"),
        Skill("Transformational Leadership", "Communication", "Advanced", "8 Weeks", "Manager Core")
    )

    val curatedResources = listOf(
        // Courses
        ResourceItem("Harvard CS50: Computer Science", "Courses", "edX", "The absolute gold standard introduction to programming, algorithms, and logical thinking.", "Free"),
        ResourceItem("Google UX Design Certificate", "Courses", "Coursera", "Understand design principles, wireframing, and user testing through hands-on projects.", "Paid"),
        ResourceItem("FullStackOpen: Modern Web Dev", "Courses", "University of Helsinki", "Learn React, Redux, Node.js, MongoDB, GraphQL, and Docker in one massive course.", "Free"),
        
        // YouTube Channels
        ResourceItem("Fireship", "YouTube Channels", "Fireship IO", "Bite-sized, high-tempo videos explaining concepts, languages, and frameworks in 100 seconds.", "Free"),
        ResourceItem("The Futur", "YouTube Channels", "Chris Do", "Incredible videos on design, typography, brand communication, and the business of freelancing.", "Free"),
        ResourceItem("Ali Abdaal", "YouTube Channels", "Ali Abdaal", "Explore evidence-based study techniques, productivity habits, and career strategy insights.", "Free"),

        // Certifications
        ResourceItem("AWS Certified Solutions Architect", "Certifications", "Amazon Web Services", "Highly recognized industry standard validation of cloud architecture capability.", "Paid"),
        ResourceItem("CompTIA Security+", "Certifications", "CompTIA", "Comprehensive global standard credential verifying core cybersecurity knowledge.", "Paid"),

        // Platforms
        ResourceItem("LeetCode", "Platforms", "LeetCode Team", "Practice data structures and algorithms questions to prep for rigorous tech interviews.", "Free/Paid"),
        ResourceItem("Scrimba", "Platforms", "Scrimba Team", "Interactive code sandbox cast environments where you check code directly inside the video.", "Free/Paid"),
        ResourceItem("Figma Community", "Platforms", "Figma", "Access thousands of open-source design systems, mobile UI kits, and vector assets.", "Free")
    )

    // ------------------------------------------------------------------------
    // DATABASE ACTIONS
    // ------------------------------------------------------------------------

    suspend fun saveProfile(
        email: String,
        fullName: String,
        branch: String,
        year: String,
        careerGoal: String,
        currentLevel: String,
        interests: String,
        token: String? = null
    ) {
        // 1. Save locally to SQLite via Room
        val entity = ProfileEntity(
            email = email,
            fullName = fullName,
            branch = branch,
            year = year,
            careerGoal = careerGoal,
            currentLevel = currentLevel,
            interests = interests
        )
        appDao.insertProfile(entity)

        // 2. Generate and populate custom beautiful local pre-made roadmap for the career goal!
        createPredefinedRoadmap(careerGoal)

        // 3. Attempt Supabase sync
        try {
            if (NetworkClients.hasSupabaseConfig()) {
                NetworkClients.supabaseUpsertProfile(
                    email, fullName, branch, year, careerGoal, currentLevel, interests, token
                )
            }
        } catch (e: Exception) {
            // Log & ignore (offline-fallback)
        }
    }

    suspend fun clearSessionAndProfile() {
        appDao.clearProfiles()
        appDao.clearRoadmapTasks()
    }

    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean) {
        appDao.updateTaskStatus(id, isCompleted)
    }

    // ------------------------------------------------------------------------
    // ROADMAP GENERATORS & CORE ALGORITHMS
    // ------------------------------------------------------------------------

    private suspend fun createPredefinedRoadmap(careerGoal: String) {
        appDao.clearRoadmapTasks()
        val defaultTasks = getPredefinedTasksForGoal(careerGoal)
        appDao.insertRoadmapTasks(defaultTasks)
    }

    fun getPredefinedTasksForGoal(careerGoal: String): List<RoadmapTaskEntity> {
        return when (careerGoal) {
            "Full Stack Developer" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Foundations", title = "HTML, CSS, and modern CSS Frameworks", description = "Master flexbox, grid, responsive design, and Tailwind structures."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Foundations", title = "JavaScript (ES6+) Core Mechanics", description = "Deep dive into async-await, fetch APIs, promises, array methods, and DOM manipulation."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Frontend & Frameworks", title = "React Framework Mastery", description = "Learn hooks, custom state providers, routing, and component lifecycles."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Frontend & Frameworks", title = "Client-Side Build Optimizations", description = "Understand package bundles, bundlers, and critical rendering paths."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Service Layer & Backends", title = "NodeJS, RESTful APIs, and SQL Data Layer", description = "Create Express routes, build schemas, and connect PostgreSQL or Room database."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Service Layer & Backends", title = "Secure Deployments & Pipelines", description = "Manage cloud deployments, setup CORS, authenticate user credentials securely, and audit security.")
            )
            "Cybersecurity Engineer" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Networking & Systems", title = "TCP/IP Suite & Network Topologies", description = "Understand Subnetting, routing tables, DNS, and fundamental data transit."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Networking & Systems", title = "Linux Command Shell Foundations", description = "Learn system processes, permissions, logs auditing, and shell scripting."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Defensive Operations", title = "Cryptography & Secure Architecture", description = "Implement AES/RSA, verify certificate authority parameters, hashing, and HTTPS protocols."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Defensive Operations", title = "Firewalling & Network Guarding", description = "Configure IDS/IPS, manage packet filtering rules, and analyze Wireshark payloads."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Offensive Auditing", title = "Penetration Testing & Breach Audits", description = "Use Kali tools to perform network mapping, discover code exploits, and document architectural security reports.")
            )
            "Data Scientist" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Core Mathematical Toolkit", title = "Linear Algebra, Calculus, & Basic Stats", description = "Master probabilities, vector calculus, distributions, and matrix operations."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Core Mathematical Toolkit", title = "Data Wrangling with Python", description = "Master Pandas, NumPy, text transformations, and advanced file IO pipelines."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Visualizing & Modeling", title = "Descriptive Data Mining & Visuals", description = "Build rich charts using Matplotlib, Seaborn, and clean up missing values safely."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Visualizing & Modeling", title = "Classical Supervised Algorithms", description = "Implement linear/logistic regressions, decision trees, and evaluate models (ROC, F1)."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Production Pipeline", title = "SQL Storage & Cloud ML Deploy", description = "Query large database systems, write server endpoints block, and ship prediction models to API instances.")
            )
            "UI/UX Designer" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Visual Design Axioms", title = "Typography, Spacing, and Hierarchies", description = "Understand type scale, golden ratios, layout alignment, and positive/negative spacing."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Visual Design Axioms", title = "Figma Vector Precision & Tooling", description = "Master auto-layout templates, components, variants, properties, and micro-layouts."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Research & Prototypes", title = "Heuristic Auditing & Mapping Flows", description = "Perform active user testing, map persona actions, and resolve critical usability blocks."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Research & Prototypes", title = "Micro-Interactions & Prototyping", description = "Build smart animated prototypes modeling complex state interactions elegantly."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Design Specs & Handoff", title = "Design Systems & Token Architecture", description = "Structure design systems, annotate tokens for developer handoff, and inspect CSS layouts.")
            )
            "App Developer" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Platform Foundations", title = "Kotlin Core Syntax & Coroutines", description = "Learn OOP/FP ideas, properties, flow controls, and asynchronous programming model."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Platform Foundations", title = "Jetpack Compose Declarative UI", description = "Build responsive layouts with Scaffold, Rows, Columns, Cards, and Material 3."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: State & Storage", title = "Room Database Local Persistence", description = "Implement local SQL caching, model entities, write query DAOs, and observe states with Flows."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: State & Storage", title = "REST Networking & Retrofit Client", description = "Connect app to external endpoints securely, parse JSON configurations, and handle network states."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Production Polishing", title = "Custom Graphics, Icons & Play Release", description = "Add custom Canvas drawings, design adaptive vector launcher symbols, optimize Gradle variables, and upload AAB bundle.")
            )
            // Fallback for creative, business, other digital goals
            else -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Getting Started", title = "Core Terminology & Essential Tools", description = "Familiarize yourself with the fundamental workflows and tools used by top industry professionals."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Getting Started", title = "Case Study Analysis", description = "Examine industry-leading examples of work and reverse-engineer their success patterns."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Deep Skill Drills", title = "Hands-on Guided Experiments", description = "Build smaller focused exercises designed to test and consolidate intermediate capabilities."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Deep Skill Drills", title = "Portfolio Piece Creation", description = "Plan and design a professional-grade project showcasing your individual voice and technique."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Mastering Commercial Handoff", title = "Business Operations & Professional Brand", description = "Setup a clean visual portfolio website, frame your services, and audit growth metrics.")
            )
        }
    }

    // ------------------------------------------------------------------------
    // AI INTERNALS: GEMINI CONTEXT BUILDERS
    // ------------------------------------------------------------------------

    suspend fun generateAiRoadmap(
        careerGoal: String,
        currentLevel: String,
        interests: String
    ): Boolean {
        if (!NetworkClients.hasGeminiConfig()) {
            return false // Let caller know we should fallback to simulation
        }

        val prompt = """
            You are SkillStacker AI Career Consultant.
            Generate a personalized professional learning roadmap in strict JSON format.
            The user wants to become a: '$careerGoal'.
            Their current level is: '$currentLevel'.
            Their stated interests are: '$interests'.
            
            Return a JSON array containing up to 4 elements representing educational milestones/tasks.
            Each element MUST have exactly these fields:
            - "phase": number (1, 2, or 3)
            - "phaseTitle": string like "Phase X: Title"
            - "title": string (the focused skill name or framework)
            - "description": string (one sentence summarizing what to study)

            Do NOT wrap the JSON in Markdown backticks or any other text. Return ONLY the JSON array.
        """.trimIndent()

        return try {
            val rawResponse = NetworkClients.generateGeminiResponse(prompt)
            if (rawResponse.isNullOrEmpty()) return false

            // Clean output if model included backticks
            val cleaned = rawResponse.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = JSONArray(cleaned)
            val generatedTasks = ArrayList<RoadmapTaskEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                generatedTasks.add(
                    RoadmapTaskEntity(
                        phase = obj.optInt("phase", 1),
                        phaseTitle = obj.optString("phaseTitle", "Phase 1: Generated"),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        isAiGenerated = true
                    )
                )
            }

            if (generatedTasks.isNotEmpty()) {
                appDao.clearRoadmapTasks()
                appDao.insertRoadmapTasks(generatedTasks)
                return true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun analyzeSkillGap(
        careerGoal: String,
        currentSkills: String
    ): SkillGapReport {
        if (!NetworkClients.hasGeminiConfig()) {
            // Local high-fidelity heuristic gap analyzer
            return getLocalSkillGapReport(careerGoal, currentSkills)
        }

        val prompt = """
            You are SkillStacker AI Talent Analytics.
            Perform a professional skill gap analysis for a student targeting: '$careerGoal'.
            The skills they currently know or are interested in are: '$currentSkills'.

            Return a direct JSON object (DO NOT use backticks or other explanation) with these properties:
            - "readinessScore": integer between 10 and 100 representing market readiness.
            - "missingSkills": array of 3 strings outlining high-demand missing skills.
            - "recommendations": array of 3 strings with tactical actions.
        """.trimIndent()

        return try {
            val rawResponse = NetworkClients.generateGeminiResponse(prompt)
            if (rawResponse.isNullOrEmpty()) return getLocalSkillGapReport(careerGoal, currentSkills)

            val cleaned = rawResponse.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleaned)
            val missing = ArrayList<String>()
            val missingArr = json.getJSONArray("missingSkills")
            for (i in 0 until missingArr.length()) {
                missing.add(missingArr.getString(i))
            }

            val recs = ArrayList<String>()
            val recsArr = json.getJSONArray("recommendations")
            for (i in 0 until recsArr.length()) {
                recs.add(recsArr.getString(i))
            }

            SkillGapReport(
                readinessScore = json.optInt("readinessScore", 45),
                missingSkills = missing,
                recommendations = recs,
                isRealAi = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalSkillGapReport(careerGoal, currentSkills)
        }
    }

    private fun getLocalSkillGapReport(careerGoal: String, currentSkills: String): SkillGapReport {
        // High-fidelity prewired gap intelligence model
        val parsedInterests = currentSkills.lowercase()
        val hasProgramming = parsedInterests.contains("web") || parsedInterests.contains("science") || parsedInterests.contains("developer")
        val score = if (hasProgramming) 55 else 35

        val missing = when (careerGoal) {
            "Full Stack Developer" -> listOf("Enterprise NestJS, GraphQL, and serverless queries", "Industrial CI/CD & GitHub Actions pipelines", "Dockerization, Kubernetes, and VPC management")
            "Cybersecurity Engineer" -> listOf("Intrusion Prevention configurations (Snort/Suricata)", "SIEM orchestration & ELK stack dashboards", "Deep Assembly reverse engineering")
            "Data Scientist" -> listOf("PySpark & distributed cluster databases", "Production ML pipelines (Kubeflow/MLflow)", "Deep PyTorch Convolutional neural networks")
            "UI/UX Designer" -> listOf("Advanced Design Token workflows (Style Dictionary)", "Detailed accessibility design audits", "Automated usability telemetry setups")
            "App Developer" -> listOf("Kotlin Symbol Processors & Multiplatform", "Custom Android Audio synthesis systems", "Advanced Play Asset Deliveries")
            else -> listOf("Professional corporate brand identity handoffs", "Advanced metrics tracking & SEO tag campaigns", "Inbound sales client prospecting channels")
        }

        val recs = when (careerGoal) {
            "Full Stack Developer" -> listOf(
                "Build a complex local fullstack project using SQLite Room and Retrofit.",
                "Solve 2 advanced algorithm exercises daily on LeetCode.",
                "Enroll in the free FullStackOpen curriculum."
            )
            "Cybersecurity Engineer" -> listOf(
                "Set up a local isolated virtual sandboxed server environment.",
                "Begin checking off Kali Linux penetration labs.",
                "Review the official CompTIA Security+ materials."
            )
            else -> listOf(
                "Complete one high-fidelity portfolio entry weekly.",
                "Join an active community of creators and get reviews.",
                "Build templates to streamline your startup execution speed."
            )
        }

        return SkillGapReport(
            readinessScore = score,
            missingSkills = missing,
            recommendations = recs,
            isRealAi = false
        )
    }
}

// Helper models for AI analysis
data class SkillGapReport(
    val readinessScore: Int,
    val missingSkills: List<String>,
    val recommendations: List<String>,
    val isRealAi: Boolean = false
)
