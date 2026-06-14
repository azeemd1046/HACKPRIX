package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ProfileEntity
import com.example.data.RoadmapTaskEntity
import com.example.data.Skill
import com.example.data.ResourceItem
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.DashboardTab
import com.example.viewmodel.SkillStackerViewModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun SkillStackerApp(viewModel: SkillStackerViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val activeOAuthUrl by viewModel.activeOAuthUrl.collectAsStateWithLifecycle()

    // Status snackbar alert popup
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            showSnackbar = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBg)
    ) {
        // App atmospheric subtle radial gradient background (70% Linear style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SkillSecondary.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.8f, size.height * 0.1f),
                            radius = size.width
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SkillPrimary.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.2f, size.height * 0.8f),
                            radius = size.width
                        )
                    )
                }
        )

        // Screen routing switcher
        when (currentScreen) {
            is AppScreen.Splash -> SplashScreen(
                onGetStarted = { viewModel.navigateToSignUp() },
                onLogin = { viewModel.navigateToLogin() }
            )
            is AppScreen.SignUp -> SignUpScreen(
                onSignUp = { email, pass, name -> viewModel.handleSignUp(email, pass, name) },
                onContinueWithGoogle = { viewModel.handleGoogleSignIn() },
                onNavigateToLogin = { viewModel.navigateToLogin() }
            )
            is AppScreen.Login -> LoginScreen(
                onLogin = { email, pass -> viewModel.handleLogin(email, pass) },
                onContinueWithGoogle = { viewModel.handleGoogleSignIn() },
                onNavigateToSignUp = { viewModel.navigateToSignUp() }
            )
            is AppScreen.Onboarding -> OnboardingScreen(viewModel = viewModel)
            is AppScreen.Dashboard -> MainDashboardWrapper(viewModel = viewModel)
        }

        // Beautiful floating Status Toast / Snackbar
        if (showSnackbar && statusMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SkillCard),
                border = BorderStroke(1.dp, SkillSecondary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .testTag("status_snackbar")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            showSnackbar = false
                            viewModel.clearStatusMessage()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Status",
                            tint = SkillMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Secure in-app WebView Google Sign-In portal (prevents external localhost:3000 redirection)
        if (activeOAuthUrl != null) {
            OAuthWebViewDialog(
                url = activeOAuthUrl!!,
                onDismissRequest = { viewModel.dismissOAuthFlow() },
                onTokenCaptured = { token -> viewModel.handleSupabaseOauthCallback(token) }
            )
        }
    }
}

@Composable
fun OAuthWebViewDialog(
    url: String,
    onDismissRequest: () -> Unit,
    onTokenCaptured: (String) -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = SkillBg,
            border = BorderStroke(1.dp, SkillPrimary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header/Title bar with Secure Lock representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SkillCard)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure Login",
                            tint = SkillPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google Secure Sign-in",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                }

                // In-App OAuth Redirect Interceptor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                ) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            android.webkit.WebView(context).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    // Non-webview-restricted Chrome agent to bypass Google Security checks
                                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
                                }
                                webViewClient = object : android.webkit.WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: android.webkit.WebView?,
                                        request: android.webkit.WebResourceRequest?
                                    ): Boolean {
                                        val reqUrl = request?.url?.toString() ?: ""
                                        if (handleRedirect(view, reqUrl)) {
                                            return true
                                        }
                                        return false
                                    }

                                    override fun onPageStarted(
                                        view: android.webkit.WebView?,
                                        url: String?,
                                        favicon: android.graphics.Bitmap?
                                    ) {
                                        super.onPageStarted(view, url, favicon)
                                        url?.let { handleRedirect(view, it) }
                                    }

                                    private fun handleRedirect(view: android.webkit.WebView?, currentUrl: String): Boolean {
                                        android.util.Log.d("OAuthWebView", "Redirect intercept loading URL: $currentUrl")
                                        
                                        val isCallback = currentUrl.contains("access_token=") || 
                                                         currentUrl.contains("skillstacker://") || 
                                                         currentUrl.contains("localhost:3000") || 
                                                         currentUrl.contains("127.0.0.1:3000")
                                        
                                        if (isCallback) {
                                            if (currentUrl.contains("access_token=")) {
                                                val token = extractToken(currentUrl)
                                                if (token != null) {
                                                    view?.stopLoading()
                                                    onTokenCaptured(token)
                                                    onDismissRequest()
                                                    return true
                                                }
                                            } else if (currentUrl.contains("error_description=")) {
                                                view?.stopLoading()
                                                onDismissRequest()
                                                return true
                                            } else if (currentUrl.contains("localhost:3000") || currentUrl.contains("127.0.0.1:3000")) {
                                                // Localhost redirect fallback support
                                                view?.stopLoading()
                                                onDismissRequest()
                                                return true
                                            }
                                        }
                                        return false
                                    }

                                    private fun extractToken(urlStr: String): String? {
                                        return try {
                                            val tokenPart = urlStr.split("access_token=").getOrNull(1)
                                            tokenPart?.split("&")?.firstOrNull()
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                }
                                loadUrl(url)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// 1. SPLASH SCREEN
// ------------------------------------------------------------------------
@Composable
fun SplashScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Large Premium Logo (30% Apple inspired minimalist glyph)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SkillPrimary, SkillSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Logo Graph",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SkillStacker",
                fontSize = 36.sp,
                color = SkillText,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1.5).sp,
                modifier = Modifier.testTag("app_logo_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline requested
            Text(
                text = "Build the Skills That Matter.",
                fontSize = 18.sp,
                color = SkillSecondary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Platform Intro text
        Text(
            text = "AI-powered career and skill development. Discover career paths, build valuable roadmaps, and land your dream job.",
            fontSize = 14.sp,
            color = SkillMutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 20.sp
        )

        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = SkillPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("splash_get_started"),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogin,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, SkillPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("splash_login")
            ) {
                Text(
                    text = "Sign In with Email",
                    fontSize = 15.sp,
                    color = SkillPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------------
// 2. SIGN UP SCREEN
// ------------------------------------------------------------------------
@Composable
fun SignUpScreen(
    onSignUp: (String, String, String) -> Unit,
    onContinueWithGoogle: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillText,
                modifier = Modifier.fillMaxWidth().testTag("signup_header_title")
            )
            Text(
                text = "Start stacking your credentials today.",
                fontSize = 14.sp,
                color = SkillMutedText,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Full Name field
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                textStyle = LocalTextStyle.current.copy(color = SkillText),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkillPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = SkillPrimary,
                    unfocusedLabelColor = SkillMutedText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("signup_fullname_input"),
                singleLine = true
            )
        }

        item {
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                textStyle = LocalTextStyle.current.copy(color = SkillText),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkillPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = SkillPrimary,
                    unfocusedLabelColor = SkillMutedText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }

        item {
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                textStyle = LocalTextStyle.current.copy(color = SkillText),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkillPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = SkillPrimary,
                    unfocusedLabelColor = SkillMutedText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSignUp(email, password, fullName) },
                colors = ButtonDefaults.buttonColors(containerColor = SkillPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("signup_submit_button")
            ) {
                Text("Create Account", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
                Text(" OR ", color = SkillMutedText, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
            }
        }

        item {
            OutlinedButton(
                onClick = onContinueWithGoogle,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillText),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("signup_google_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Adjust, // Custom Google representation
                        contentDescription = "Google Sign In",
                        tint = SkillSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", color = SkillText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", color = SkillMutedText, fontSize = 14.sp)
                Text(
                    text = "Sign In",
                    color = SkillPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .testTag("signup_switch_login")
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------------
// 3. LOGIN SCREEN
// ------------------------------------------------------------------------
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onContinueWithGoogle: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillText,
                modifier = Modifier.fillMaxWidth().testTag("login_header_title")
            )
            Text(
                text = "Let's continue building those skills.",
                fontSize = 14.sp,
                color = SkillMutedText,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                textStyle = LocalTextStyle.current.copy(color = SkillText),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkillPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = SkillPrimary,
                    unfocusedLabelColor = SkillMutedText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                textStyle = LocalTextStyle.current.copy(color = SkillText),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkillPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = SkillPrimary,
                    unfocusedLabelColor = SkillMutedText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onLogin(email, password) },
                colors = ButtonDefaults.buttonColors(containerColor = SkillPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button")
            ) {
                Text("Login", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
                Text(" OR ", color = SkillMutedText, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
            }
        }

        item {
            OutlinedButton(
                onClick = onContinueWithGoogle,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillText),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_google_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Adjust, // Google representation
                        contentDescription = "Google Login",
                        tint = SkillSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", color = SkillText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account? ", color = SkillMutedText, fontSize = 14.sp)
                Text(
                    text = "Sign Up",
                    color = SkillPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToSignUp() }
                        .testTag("login_switch_signup")
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------------
// 4. ONBOARDING SCREEN (Multi-Step flow)
// ------------------------------------------------------------------------
@Composable
fun OnboardingScreen(viewModel: SkillStackerViewModel) {
    val step by viewModel.onboardStep.collectAsStateWithLifecycle()

    val name by viewModel.onboardFullName.collectAsStateWithLifecycle()
    val branch by viewModel.onboardBranch.collectAsStateWithLifecycle()
    val year by viewModel.onboardYear.collectAsStateWithLifecycle()
    val careerGoal by viewModel.onboardCareerGoal.collectAsStateWithLifecycle()
    val level by viewModel.onboardCurrentLevel.collectAsStateWithLifecycle()
    val interests by viewModel.onboardInterests.collectAsStateWithLifecycle()

    val totalSteps = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Multi-Step Header Index Progress bar (Apple design inspiration)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP $step OF $totalSteps",
                    color = SkillSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = when (step) {
                        1 -> "Who Are You?"
                        2 -> "Career Target"
                        3 -> "Experience"
                        else -> "Skill Interests"
                    },
                    color = SkillText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Beautiful micro segment progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .then(
                                if (i <= step) {
                                    Modifier.background(Brush.linearGradient(colors = listOf(SkillPrimary, SkillSecondary)))
                                } else {
                                    Modifier.background(Color(0xFFE2E8F0))
                                }
                            )
                    )
                }
            }
        }

        // Active Inner Step layout (Scrollable container to handle small devices)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 24.dp)
        ) {
            when (step) {
                1 -> OnboardStepOne(
                    onNameChange = { viewModel.onboardFullName.value = it },
                    onBranchSelect = { viewModel.onboardBranch.value = it },
                    onYearSelect = { viewModel.onboardYear.value = it },
                    name = name,
                    branch = branch,
                    year = year
                )
                2 -> OnboardStepTwo(
                    viewModel = viewModel,
                    selectedGoal = careerGoal,
                    onGoalSelect = { viewModel.onboardCareerGoal.value = it }
                )
                3 -> OnboardStepThree(
                    selectedLevel = level,
                    onLevelSelect = { viewModel.onboardCurrentLevel.value = it }
                )
                4 -> OnboardStepFour(
                    selectedInterests = interests,
                    onInterestToggle = { viewModel.toggleInterest(it) }
                )
            }
        }

        // Onboarding Action Buttons panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { viewModel.prevOnboardStep() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, SkillPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillPrimary),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .testTag("onboard_prev_button")
                ) {
                    Text("Back", color = SkillPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Button(
                onClick = { viewModel.nextOnboardStep() },
                colors = ButtonDefaults.buttonColors(containerColor = SkillPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(52.dp)
                    .weight(if (step > 1) 1.5f else 1f)
                    .testTag("onboard_next_button")
            ) {
                Text(
                    text = if (step == totalSteps) "Build My Dashboard" else "Next Step",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OnboardStepOne(
    onNameChange: (String) -> Unit,
    onBranchSelect: (String) -> Unit,
    onYearSelect: (String) -> Unit,
    name: String,
    branch: String,
    year: String
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Text(
                text = "Configure Your Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillText,
                modifier = Modifier.testTag("onboard_s1_title")
            )
            Text(
                text = "We will customize your track based on your field.",
                fontSize = 14.sp,
                color = SkillMutedText
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Text("What is your name?", color = SkillText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("e.g. John Doe", color = SkillMutedText) },
                textStyle = LocalTextStyle.current.copy(color = SkillText),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkillPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("onboard_name_input"),
                singleLine = true
            )
        }

        item {
            Text("Choose Academic Branch", color = SkillText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val branches = listOf("CSE", "ECE", "ME", "Civil", "Other")

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(branches) { b ->
                    val isSelected = branch == b
                    Surface(
                        modifier = Modifier
                            .clickable { onBranchSelect(b) }
                            .testTag("branch_chip_$b"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SkillPrimary else SkillSurface,
                        border = BorderStroke(1.dp, if (isSelected) SkillPrimary else Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = b,
                            color = if (isSelected) Color.White else SkillText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Text("Select Academic Year", color = SkillText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val years = listOf("1st", "2nd", "3rd", "4th")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                years.forEach { y ->
                    val isSelected = year == y
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onYearSelect(y) }
                            .testTag("year_chip_$y"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SkillSecondary else SkillSurface,
                        border = BorderStroke(1.dp, if (isSelected) SkillSecondary else Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = y,
                            color = if (isSelected) Color.White else SkillText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardStepTwo(
    viewModel: com.example.viewmodel.SkillStackerViewModel,
    selectedGoal: String,
    onGoalSelect: (String) -> Unit
) {
    val options = listOf(
        Pair("Full Stack Developer", "Tech"),
        Pair("Cybersecurity Engineer", "Tech"),
        Pair("Data Scientist", "Tech"),
        Pair("Cloud Engineer", "Tech"),
        Pair("UI/UX Designer", "Creative"),
        Pair("App Developer", "Tech"),
        Pair("Video Editor", "Creative"),
        Pair("Graphic Designer", "Creative"),
        Pair("Content Creator", "Creative"),
        Pair("Freelancer", "Business"),
        Pair("Entrepreneur", "Business"),
        Pair("Digital Marketer", "Business"),
        Pair("Personal Finance", "Finance"),
        Pair("Investing", "Finance"),
        Pair("Public Speaking", "Communication"),
        Pair("Leadership", "Communication"),
        Pair("I don't know yet", "Undecided")
    )

    val matches by viewModel.careerMatchesList.collectAsStateWithLifecycle()
    val isMatching by viewModel.isGeneratingCareerMatches.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select Career Target",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SkillText,
            modifier = Modifier.testTag("onboard_s2_title")
        )
        Text(
            text = "We will structure your steps and tasks based on this target.",
            fontSize = 14.sp,
            color = SkillMutedText
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Large list of career goals formatted like neat chips
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(options) { (goal, cat) ->
                val isSelected = selectedGoal == goal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoalSelect(goal) }
                        .testTag("career_goal_$goal"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SkillSecondary.copy(alpha = 0.05f) else SkillSurface
                    ),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) SkillPrimary else Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = goal,
                                color = SkillText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Category: $cat",
                                color = SkillMutedText,
                                fontSize = 12.sp
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { onGoalSelect(goal) },
                            colors = RadioButtonDefaults.colors(selectedColor = SkillPrimary)
                        )
                    }
                }
            }

            // AI Career Match recommendations insert block
            if (selectedGoal == "I don't know yet") {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SkillSurface),
                        border = BorderStroke(1.dp, SkillPrimary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ai_onboard_matcher_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Gemini AI Career Matcher",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillPrimary
                            )
                            Text(
                                text = "We will analyze your academic background & interests with Gemini to target perfect pathways.",
                                fontSize = 11.sp,
                                color = SkillMutedText,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isMatching) {
                                CircularProgressIndicator(
                                    color = SkillPrimary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .padding(vertical = 12.dp)
                                )
                            } else if (matches != null && matches!!.isNotEmpty()) {
                                matches!!.forEach { match ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SkillSurface),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(match.career, color = SkillText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Surface(color = SkillSecondary.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                                    Text(
                                                        text = "${match.matchPercentage}% Fit",
                                                        color = SkillSecondary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(match.reasoning, color = SkillMutedText, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                                            Text("Focus Area: ${match.roadmapSummary}", color = SkillMutedText, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { onGoalSelect(match.career) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SkillPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Select ${match.career}", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.generateCareerMatches() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                ) {
                                    Text("Find My Tailored Career Paths", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardStepThree(
    selectedLevel: String,
    onLevelSelect: (String) -> Unit
) {
    val levels = listOf(
        Triple("Beginner", "I'm starting from scratch. No prior experience.", Icons.Default.Adjust),
        Triple("Intermediate", "I have some experience and foundational skills built.", Icons.Default.Layers),
        Triple("Advanced", "I am highly competent. Looking to bridge minor skill gaps.", Icons.Default.Grade)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What is your current level?",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SkillText,
            modifier = Modifier.testTag("onboard_s3_title")
        )
        Text(
            text = "We'll configure the difficulty of your roadmap steps.",
            fontSize = 14.sp,
            color = SkillMutedText
        )
        Spacer(modifier = Modifier.height(24.dp))

        levels.forEach { (lvl, desc, icon) ->
            val isSelected = selectedLevel == lvl
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onLevelSelect(lvl) }
                    .testTag("level_card_$lvl"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SkillSecondary.copy(alpha = 0.05f) else SkillSurface
                ),
                border = BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) SkillSecondary else Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = lvl,
                        tint = if (isSelected) SkillSecondary else SkillMutedText,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = lvl,
                            color = SkillText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = desc,
                            color = SkillMutedText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardStepFour(
    selectedInterests: Set<String>,
    onInterestToggle: (String) -> Unit
) {
    val chips = listOf(
        "Web Development",
        "Cybersecurity",
        "Data Science",
        "UI/UX",
        "Video Editing",
        "Copywriting",
        "Digital Marketing",
        "Investing",
        "Public Speaking",
        "Entrepreneurship"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select Your Interests",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SkillText,
            modifier = Modifier.testTag("onboard_s4_title")
        )
        Text(
            text = "Multi-select chips to curate complementary exercises.",
            fontSize = 14.sp,
            color = SkillMutedText
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Dense staggered multi-select tags (70% Linear style)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(chips) { chip ->
                val isSelected = selectedInterests.contains(chip)
                Surface(
                    modifier = Modifier
                        .clickable { onInterestToggle(chip) }
                        .testTag("interest_chip_$chip"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SkillSecondary.copy(alpha = 0.12f) else SkillSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) SkillSecondary else Color(0xFFE2E8F0)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chip,
                            color = if (isSelected) SkillSecondary else SkillText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Checked",
                                tint = SkillSecondary,
                                modifier = Modifier.size(16.dp)
                              )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// 5. MAIN DASHBOARD VIEW WRAPPER
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardWrapper(viewModel: SkillStackerViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            // Respect proper WindowInsets and safe area guidelines
            NavigationBar(
                containerColor = SkillSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                // Home tab
                NavigationBarItem(
                    selected = currentTab == DashboardTab.HOME,
                    onClick = { viewModel.switchTab(DashboardTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SkillPrimary,
                        selectedTextColor = SkillPrimary,
                        unselectedIconColor = SkillMutedText,
                        unselectedTextColor = SkillMutedText,
                        indicatorColor = SkillCard
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // Roadmap tab
                NavigationBarItem(
                    selected = currentTab == DashboardTab.ROADMAP,
                    onClick = { viewModel.switchTab(DashboardTab.ROADMAP) },
                    icon = { Icon(Icons.Default.Route, contentDescription = "Roadmap") },
                    label = { Text("Roadmap", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SkillSecondary,
                        selectedTextColor = SkillSecondary,
                        unselectedIconColor = SkillMutedText,
                        unselectedTextColor = SkillMutedText,
                        indicatorColor = SkillCard
                    ),
                    modifier = Modifier.testTag("nav_tab_roadmap")
                )

                // Skills tab
                NavigationBarItem(
                    selected = currentTab == DashboardTab.SKILLS,
                    onClick = { viewModel.switchTab(DashboardTab.SKILLS) },
                    icon = { Icon(Icons.Default.Widgets, contentDescription = "Skills") },
                    label = { Text("Skills", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SkillPrimary,
                        selectedTextColor = SkillPrimary,
                        unselectedIconColor = SkillMutedText,
                        unselectedTextColor = SkillMutedText,
                        indicatorColor = SkillCard
                    ),
                    modifier = Modifier.testTag("nav_tab_skills")
                )

                // Resources tab
                NavigationBarItem(
                    selected = currentTab == DashboardTab.RESOURCES,
                    onClick = { viewModel.switchTab(DashboardTab.RESOURCES) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Resources") },
                    label = { Text("Resources", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SkillSecondary,
                        selectedTextColor = SkillSecondary,
                        unselectedIconColor = SkillMutedText,
                        unselectedTextColor = SkillMutedText,
                        indicatorColor = SkillCard
                    ),
                    modifier = Modifier.testTag("nav_tab_resources")
                )

                // Profile tab
                NavigationBarItem(
                    selected = currentTab == DashboardTab.PROFILE,
                    onClick = { viewModel.switchTab(DashboardTab.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SkillPrimary,
                        selectedTextColor = SkillPrimary,
                        unselectedIconColor = SkillMutedText,
                        unselectedTextColor = SkillMutedText,
                        indicatorColor = SkillCard
                    ),
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                DashboardTab.HOME -> HomeLayout(viewModel = viewModel)
                DashboardTab.ROADMAP -> RoadmapLayout(viewModel = viewModel)
                DashboardTab.SKILLS -> SkillLibraryLayout(viewModel = viewModel)
                DashboardTab.RESOURCES -> ResourcesLayout(viewModel = viewModel)
                DashboardTab.PROFILE -> ProfileLayout(viewModel = viewModel)
            }
        }
    }
}

// ------------------------------------------------------------------------
// RECHARTS-INSPIRED INTERACTIVE ANALYTICS DASHBOARD
// ------------------------------------------------------------------------
data class PhasePlotData(
    val phase: Int,
    val title: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val percent: Float
)

@Composable
fun RechartsPlaygroundCard(tasks: List<RoadmapTaskEntity>) {
    // Process real tasks from local db
    val phases = tasks.groupBy { it.phase }.keys.sorted()
    val realPhaseData = phases.map { p ->
        val phaseTasks = tasks.filter { it.phase == p }
        val total = phaseTasks.size
        val completed = phaseTasks.count { it.isCompleted }
        val title = phaseTasks.firstOrNull()?.phaseTitle ?: "Phase $p"
        PhasePlotData(
            phase = p,
            title = title,
            totalTasks = total,
            completedTasks = completed,
            percent = if (total > 0) completed.toFloat() / total else 0f
        )
    }

    // High fidelity default mock milestones for initial launch when database is fresh
    val defaultPhaseData = listOf(
        PhasePlotData(1, "Phase 1: Foundation Basics", 4, 3, 0.75f),
        PhasePlotData(2, "Phase 2: Advanced Core Stack", 5, 2, 0.40f),
        PhasePlotData(3, "Phase 3: Building Capstones", 4, 1, 0.25f),
        PhasePlotData(4, "Phase 4: Portfolios & Outreach", 3, 0, 0.00f)
    )

    val activePhaseData = if (realPhaseData.isNotEmpty()) realPhaseData else defaultPhaseData
    
    // Chart Switcher state
    var selectedChartType by remember { mutableStateOf("bar") } // "bar", "area", "radial"
    var selectedPhaseIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(activePhaseData.size) {
        selectedPhaseIndex = if (activePhaseData.isNotEmpty()) 0 else -1
    }
    
    val safeSelectedIndex = if (selectedPhaseIndex in activePhaseData.indices) selectedPhaseIndex else 0
    val selectedData = if (activePhaseData.isNotEmpty() && safeSelectedIndex in activePhaseData.indices) {
        activePhaseData[safeSelectedIndex]
    } else null

    Card(
        colors = CardDefaults.cardColors(containerColor = SkillSurface),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_dashboard_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Roadmap Insights",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SkillText
                    )
                    Text(
                        text = if (realPhaseData.isNotEmpty()) "Real-time interactive analytics" else "Initial career path sandbox index",
                        fontSize = 12.sp,
                        color = SkillMutedText
                    )
                }
                
                // Pulsate Indicator node
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SkillSuccess)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Modern Web Segmented Tab Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SkillBg, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair("bar", "Bar Chart"),
                    Pair("area", "Area Chart"),
                    Pair("radial", "Radial Gauge")
                ).forEach { (type, label) ->
                    val isSelected = selectedChartType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { selectedChartType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SkillPrimary else SkillMutedText
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Main Chart Drawing Canvas Node
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (selectedChartType == "radial") {
                    RadialProgressGauge(activePhaseData = activePhaseData)
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Y-Axis labels list style
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(36.dp)
                                .padding(bottom = 30.dp, top = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            listOf("100%", "75%", "50%", "25%", "0%").forEach { prc ->
                                Text(
                                    text = prc,
                                    color = SkillMutedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        // Right Canvas Plot Channel
                        Column(modifier = Modifier.fillMaxSize()) {
                            val mutedGridColor = Color(0xFFE2E8F0)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .pointerInput(activePhaseData) {
                                        detectTapGestures { offset ->
                                            val w = size.width
                                            val step = w / activePhaseData.size
                                            val tappedIdx = (offset.x / step).toInt().coerceIn(0, activePhaseData.size - 1)
                                            selectedPhaseIndex = tappedIdx
                                        }
                                    }
                            ) {
                                // Draw horizontal grid lines on canvas back plate
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    val gridCount = 4
                                    val verticalSpacing = size.height / gridCount
                                    for (i in 0..gridCount) {
                                        val y = i * verticalSpacing
                                        drawLine(
                                            color = mutedGridColor,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 2f
                                        )
                                    }
                                }
                                
                                // Render representations
                                if (selectedChartType == "bar") {
                                    BarChartCanvas(
                                        activePhaseData = activePhaseData,
                                        selectedIdx = safeSelectedIndex
                                    )
                                } else if (selectedChartType == "area") {
                                    AreaChartCanvas(
                                        activePhaseData = activePhaseData,
                                        selectedIdx = safeSelectedIndex
                                    )
                                }
                            }
                            
                            // Bottom Line Divider for X Axis
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                            
                            // Bottom Column X-Axis labels
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                activePhaseData.forEachIndexed { idx, item ->
                                    val isSelected = idx == safeSelectedIndex
                                    Text(
                                        text = "Phase ${item.phase}",
                                        fontSize = 10.sp,
                                        color = if (isSelected) SkillPrimary else SkillMutedText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.clickable { selectedPhaseIndex = idx }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = SkillPrimary, label = "Milestone Targets")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(color = SkillSecondary, label = "Current Progress")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detailed Floating / Tooltip Card
            if (selectedData != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkillSurface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedData.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedData.percent >= 1f) SkillSuccess.copy(alpha = 0.15f) else SkillPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${(selectedData.percent * 100).toInt()}% Clear",
                                    color = if (selectedData.percent >= 1f) SkillSuccess else SkillPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val statusDesc = if (selectedData.percent >= 1f) {
                            "Status: Milestones achieved! Foundations are rock solid."
                        } else if (selectedData.percent > 0f) {
                            "Status: Learning track ongoing. Click to complete actions in Roadmap tab."
                        } else {
                            "Status: Pending milestone unlock. Work through uncompleted task stacks."
                        }
                        
                        Text(
                            text = statusDesc,
                            fontSize = 12.sp,
                            color = SkillMutedText,
                            lineHeight = 16.sp
                        )
                        
                        Text(
                            text = "Actions Completed: ${selectedData.completedTasks} of ${selectedData.totalTasks} roadmap items.",
                            fontSize = 12.sp,
                            color = SkillPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = SkillMutedText,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BarChartCanvas(
    activePhaseData: List<PhasePlotData>,
    selectedIdx: Int
) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val count = activePhaseData.size
        val blockWidth = size.width / count
        val barWidthRatio = 0.35f
        val calculatedBarWidth = blockWidth * barWidthRatio
        
        activePhaseData.forEachIndexed { i, d ->
            val isSelected = i == selectedIdx
            
            val barHeight = d.percent * size.height
            val x = (i * blockWidth) + (blockWidth - calculatedBarWidth) / 2
            val y = size.height - barHeight
            
            val barBrush = Brush.verticalGradient(
                colors = if (isSelected) {
                    listOf(SkillPrimary, SkillSecondary)
                } else {
                    listOf(SkillCard, SkillSecondary.copy(alpha = 0.35f))
                }
            )
            
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(calculatedBarWidth, barHeight.coerceAtLeast(10f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            
            if (isSelected) {
                drawRoundRect(
                    color = SkillPrimary,
                    topLeft = Offset(x, y - 4f),
                    size = androidx.compose.ui.geometry.Size(calculatedBarWidth, 6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun AreaChartCanvas(
    activePhaseData: List<PhasePlotData>,
    selectedIdx: Int
) {
    val primColor = SkillPrimary
    val secColor = SkillSecondary
    
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val count = activePhaseData.size
        val blockWidth = size.width / count
        val points = activePhaseData.mapIndexed { i, d ->
            val x = (i * blockWidth) + blockWidth / 2
            val y = size.height - (d.percent * size.height)
            Offset(x, y)
        }
        
        if (points.isNotEmpty()) {
            val filledPath = Path().apply {
                moveTo(points.first().x, size.height)
                lineTo(points.first().x, points.first().y)
                
                for (j in 1 until points.size) {
                    val pPrev = points[j - 1]
                    val pCurr = points[j]
                    quadraticTo(
                        x1 = (pPrev.x + pCurr.x) / 2,
                        y1 = pPrev.y,
                        x2 = pCurr.x,
                        y2 = pCurr.y
                    )
                }
                
                lineTo(points.last().x, size.height)
                close()
            }
            
            val strokePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (j in 1 until points.size) {
                    val pPrev = points[j - 1]
                    val pCurr = points[j]
                    quadraticTo(
                        x1 = (pPrev.x + pCurr.x) / 2,
                        y1 = pPrev.y,
                        x2 = pCurr.x,
                        y2 = pCurr.y
                    )
                }
            }
            
            drawPath(
                path = filledPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        secColor.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
            
            drawPath(
                path = strokePath,
                color = secColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
            
            points.forEachIndexed { idx, point ->
                val isSelected = idx == selectedIdx
                
                drawCircle(
                    color = if (isSelected) primColor.copy(alpha = 0.25f) else Color.Transparent,
                    radius = 16.dp.toPx(),
                    center = point
                )
                
                drawCircle(
                    color = if (isSelected) primColor else secColor,
                    radius = 6.dp.toPx(),
                    center = point
                )
                
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
fun RadialProgressGauge(activePhaseData: List<PhasePlotData>) {
    val completionAvg = if (activePhaseData.isNotEmpty()) {
        activePhaseData.map { it.percent }.average().toFloat()
    } else 0.45f
    
    val ring1Sweep = completionAvg * 360f
    val ring2Sweep = (activePhaseData.firstOrNull()?.percent ?: 0.60f) * 360f
    val ring3Sweep = (activePhaseData.lastOrNull()?.percent ?: 0.10f) * 360f
    
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val baseStrokeWidth = 10.dp.toPx()
                val gap = 16.dp.toPx()
                
                // Ring 1 (External)
                drawArc(
                    color = Color(0xFFE2E8F0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseStrokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(SkillPrimary, SkillSecondary)),
                    startAngle = -90f,
                    sweepAngle = ring1Sweep.coerceAtLeast(10f),
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseStrokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                
                // Ring 2 (Middle)
                val innerRadius1 = (size.width - baseStrokeWidth - gap)
                drawArc(
                    color = Color(0xFFE2E8F0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseStrokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    topLeft = Offset(baseStrokeWidth/2 + gap/2, baseStrokeWidth/2 + gap/2),
                    size = androidx.compose.ui.geometry.Size(innerRadius1, innerRadius1)
                )
                drawArc(
                    color = SkillSecondary,
                    startAngle = -90f,
                    sweepAngle = ring2Sweep.coerceAtLeast(10f),
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseStrokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    topLeft = Offset(baseStrokeWidth/2 + gap/2, baseStrokeWidth/2 + gap/2),
                    size = androidx.compose.ui.geometry.Size(innerRadius1, innerRadius1)
                )
                
                // Ring 3 (Internal)
                val innerRadius2 = innerRadius1 - baseStrokeWidth - gap
                drawArc(
                    color = Color(0xFFE2E8F0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseStrokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    topLeft = Offset(baseStrokeWidth + gap, baseStrokeWidth + gap),
                    size = androidx.compose.ui.geometry.Size(innerRadius2, innerRadius2)
                )
                drawArc(
                    color = SkillSuccess,
                    startAngle = -90f,
                    sweepAngle = ring3Sweep.coerceAtLeast(10f),
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseStrokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    topLeft = Offset(baseStrokeWidth + gap, baseStrokeWidth + gap),
                    size = androidx.compose.ui.geometry.Size(innerRadius2, innerRadius2)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(completionAvg * 100).toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillText
                )
                Text(
                    text = "Overall",
                    fontSize = 10.sp,
                    color = SkillMutedText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            RadialLegendRow(color = SkillPrimary, label = "Growth Progress", value = "${(completionAvg * 100).toInt()}%")
            RadialLegendRow(color = SkillSecondary, label = "Phase 1 Stack", value = "${(ring2Sweep/360f*100).toInt()}%")
            RadialLegendRow(color = SkillSuccess, label = "Final Milestones", value = "${(ring3Sweep/360f*100).toInt()}%")
        }
    }
}

@Composable
fun RadialLegendRow(color: Color, label: String, value: String) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SkillText
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = SkillMutedText,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}

// ------------------------------------------------------------------------

// ------------------------------------------------------------------------
// 5.A HOME TAB LAYOUT (Dashboard)
// ------------------------------------------------------------------------
@Composable
fun HomeLayout(viewModel: SkillStackerViewModel) {
    val profile by viewModel.latestProfile.collectAsStateWithLifecycle()
    val tasks by viewModel.roadmapTasks.collectAsStateWithLifecycle()
    val isSupabaseActive by viewModel.isSupabaseActive.collectAsStateWithLifecycle()
    val isGeminiActive by viewModel.isGeminiActive.collectAsStateWithLifecycle()

    val completedTasks = tasks.count { it.isCompleted }
    val totalTasks = tasks.size
    val progressPercent = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

    // Today's key missing tasks to focus on
    val nextPendingTask = tasks.firstOrNull { !it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header space
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SkillText,
                        modifier = Modifier.testTag("home_welcome_title")
                    )
                    Text(
                        text = "Year ${profile?.year ?: "1st"} • ${profile?.branch ?: "CSE"}",
                        fontSize = 14.sp,
                        color = SkillMutedText,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Cloud Status integration chip (Supabase, Gemini check)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val supabaseBadgeColor = if (isSupabaseActive) SkillSuccess else SkillMutedText
                    Surface(
                        shape = CircleShape,
                        color = supabaseBadgeColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, supabaseBadgeColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = if (isSupabaseActive) "Supa Connected" else "SQLite Mode",
                            color = if (isSupabaseActive) SkillSuccess else SkillMutedText,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // High-fidelity Gradient Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("premium_hero_card")
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SkillPrimary, SkillSecondary)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "HELLO, ${profile?.fullName?.uppercase() ?: "DEVELOPER"} 👋",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You are $progressPercent% closer to becoming a ${profile?.careerGoal ?: "Full Stack Developer"}.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 26.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "CURRENT PHASE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = nextPendingTask?.phaseTitle ?: "Foundations Track",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NEXT MILESTONE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = nextPendingTask?.title ?: "All Completed!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline, 
                                contentDescription = "Time", 
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Est. Level: ${profile?.currentLevel ?: "Beginner"}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { viewModel.switchTab(DashboardTab.ROADMAP) }
                        ) {
                            Text(
                                text = "View Roadmap",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SkillSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth().testTag("home_progress_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Roadmap Completion Progress",
                            fontSize = 14.sp,
                            color = SkillText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$progressPercent%",
                            fontSize = 16.sp,
                            color = SkillPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0.0f,
                        color = SkillPrimary,
                        trackColor = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$completedTasks of $totalTasks tasks checked",
                            fontSize = 12.sp,
                            color = SkillMutedText
                        )
                        Text(
                            text = "Let's review",
                            fontSize = 11.sp,
                            color = SkillPrimary,
                            modifier = Modifier.clickable { viewModel.switchTab(DashboardTab.ROADMAP) }
                        )
                    }
                }
            }
        }

        // 3.B Recharts-Inspired Analytics Card
        item {
            RechartsPlaygroundCard(tasks = tasks)
        }

        // AI Daily Study Coach Card
        item {
            val dailyCoach by viewModel.dailyRecommendation.collectAsStateWithLifecycle()
            val isGeneratingCoach by viewModel.isGeneratingDailyCoach.collectAsStateWithLifecycle()

            Card(
                colors = CardDefaults.cardColors(containerColor = SkillSurface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("ai_daily_coach_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = "Coach icon",
                                tint = SkillPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI Daily Coach",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillText
                            )
                        }
                        if (isGeneratingCoach) {
                            CircularProgressIndicator(color = SkillPrimary, modifier = Modifier.size(16.dp))
                        } else {
                            IconButton(onClick = { viewModel.generateDailyRecommendation() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Coach", tint = SkillPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (dailyCoach != null) {
                        Text(
                            text = "TODAY'S TOPIC FOCUS",
                            color = SkillPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = dailyCoach!!.todaysFocus,
                            color = SkillText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = dailyCoach!!.reason,
                            color = SkillMutedText,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ESTIMATED STUDY TIME", color = SkillSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Text(dailyCoach!!.estimatedDuration, color = SkillText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("RECOMMENDED MATERIAL", color = SkillSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Text(dailyCoach!!.recommendedResource, color = SkillText, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            }
                        }
                    } else {
                        Text(
                            text = "Get daily customized AI study advice based on your current goal and checkbox progress.",
                            color = SkillMutedText,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.generateDailyRecommendation() },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBg),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Sparkle", tint = SkillPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Today's Study Guide", color = SkillPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Today's Focus Card
        item {
            val taskCardTitle = if (nextPendingTask != null) "Today's Focus Task" else "All Caught Up!"
            val taskCardDesc = nextPendingTask?.title ?: "Outstanding job! You've cleared your roadmap tasks. Click 'Generate AI Roadmap' or Edit Profile to inject fresh challenges."

            Card(
                colors = CardDefaults.cardColors(containerColor = SkillSurface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("home_focus_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = taskCardTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = taskCardDesc,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillText
                    )

                    if (nextPendingTask != null) {
                        Text(
                            text = nextPendingTask.description,
                            fontSize = 13.sp,
                            color = SkillMutedText,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        Button(
                            onClick = { viewModel.toggleTaskComplete(nextPendingTask.id, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Mark As Completed", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Quick Actions block as buttons in column/grid
        item {
            Text(
                text = "Quick Navigation",
                color = SkillMutedText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.switchTab(DashboardTab.ROADMAP) },
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("My Roadmap", color = SkillText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.switchTab(DashboardTab.SKILLS) },
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Core Skills", color = SkillText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.switchTab(DashboardTab.RESOURCES) },
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Resources", color = SkillText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.switchTab(DashboardTab.PROFILE) },
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("My Profile", color = SkillText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------------
// 5.B ROADMAP TAB LAYOUT
// ------------------------------------------------------------------------
@Composable
fun RoadmapLayout(viewModel: SkillStackerViewModel) {
    val tasks by viewModel.roadmapTasks.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingRoadmap.collectAsStateWithLifecycle()
    val isGeminiActive by viewModel.isGeminiActive.collectAsStateWithLifecycle()
    val profile by viewModel.latestProfile.collectAsStateWithLifecycle()

    val phasesGroup = tasks.groupBy { it.phase }

    // Expandable state variable for phases
    var expandedPhases by remember { mutableStateOf(setOf<Int>()) }
    val cachedRoadmapJson = remember(tasks) { viewModel.getCachedRoadmapJson() }
    val parsedRoadmap = remember(cachedRoadmapJson) {
        if (!cachedRoadmapJson.isNullOrEmpty()) {
            try {
                org.json.JSONObject(cachedRoadmapJson)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "My Roadmap",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillText,
                modifier = Modifier.testTag("roadmap_header_title")
            )
            val pathSummary = "Targeting ${profile?.careerGoal ?: "Full Stack"} (${profile?.currentLevel ?: "Beginner"})"
            Text(
                text = pathSummary,
                fontSize = 14.sp,
                color = SkillSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        // Gemini AI Roadmap Generator controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SkillSurface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("ai_roadmap_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Roadmap Generator",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillText
                        )
                        Text(
                            text = if (isGeminiActive) "Gemini Active" else "Sandbox",
                            color = if (isGeminiActive) SkillPrimary else SkillMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Inputs: Career Goal ('${profile?.careerGoal}'), Level ('${profile?.currentLevel}'), Interests ('${profile?.interests}').",
                        fontSize = 12.sp,
                        color = SkillMutedText,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Text(
                        text = "Let Gemini AI write customized learning milestones, resources, and specific projects dynamically in your layout.",
                        fontSize = 12.sp,
                        color = SkillMutedText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = SkillPrimary,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Button(
                            onClick = { viewModel.generateAiRoadmap() },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("generate_ai_roadmap_button")
                        ) {
                            Text("Generate AI Roadmap", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        if (phasesGroup.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Empty Roadmap",
                            tint = SkillMutedText,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No milestones configured",
                            fontSize = 16.sp,
                            color = SkillText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Please complete onboarding or click 'Generate AI Roadmap' above.",
                            fontSize = 12.sp,
                            color = SkillMutedText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Render phases
        phasesGroup.keys.sorted().forEach { phaseNum ->
            val phaseTasks = phasesGroup[phaseNum] ?: emptyList()
            val sampleTask = phaseTasks.firstOrNull()
            val phaseTitle = sampleTask?.phaseTitle ?: "Phase $phaseNum"
            val isExpanded = expandedPhases.contains(phaseNum)

            // Attempt to fetch extra details from parsed AI JSON
            var skillsToLearn = emptyList<String>()
            var resources = emptyList<String>()
            var projects = emptyList<String>()
            var weeklyMilestones = emptyList<String>()
            var certifications = emptyList<String>()

            if (parsedRoadmap != null) {
                val phasesArr = parsedRoadmap.optJSONArray("phases")
                if (phasesArr != null) {
                    for (i in 0 until phasesArr.length()) {
                        val pObj = phasesArr.optJSONObject(i)
                        if (pObj != null && pObj.optInt("phase") == phaseNum) {
                            val skillsArr = pObj.optJSONArray("skillsToLearn")
                            if (skillsArr != null) {
                                val list = mutableListOf<String>()
                                for (j in 0 until skillsArr.length()) list.add(skillsArr.optString(j))
                                skillsToLearn = list
                            }
                            val resArr = pObj.optJSONArray("recommendedResources")
                            if (resArr != null) {
                                val list = mutableListOf<String>()
                                for (j in 0 until resArr.length()) list.add(resArr.optString(j))
                                resources = list
                            }
                            val projArr = pObj.optJSONArray("beginnerProjects")
                            if (projArr != null) {
                                val list = mutableListOf<String>()
                                for (j in 0 until projArr.length()) list.add(projArr.optString(j))
                                projects = list
                            }
                            val mileArr = pObj.optJSONArray("weeklyMilestones")
                            if (mileArr != null) {
                                val list = mutableListOf<String>()
                                for (j in 0 until mileArr.length()) list.add(mileArr.optString(j))
                                weeklyMilestones = list
                            }
                            val certsArr = pObj.optJSONArray("certifications")
                            if (certsArr != null) {
                                val list = mutableListOf<String>()
                                for (j in 0 until certsArr.length()) list.add(certsArr.optString(j))
                                certifications = list
                            }
                            break
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkillSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isExpanded) SkillPrimary else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedPhases = if (isExpanded) {
                                expandedPhases - phaseNum
                            } else {
                                expandedPhases + phaseNum
                            }
                        }
                        .testTag("roadmap_phase_card_$phaseNum")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = SkillSecondary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp),
                                    border = BorderStroke(1.dp, SkillSecondary)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$phaseNum",
                                            color = SkillSecondary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = phaseTitle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillText
                                    )
                                    Text(
                                        text = if (isExpanded) "Hide Phase Milestones" else "Show Details & Checklists",
                                        fontSize = 11.sp,
                                        color = SkillMutedText
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand phase details",
                                tint = SkillSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // 1. Skills to Learn
                            if (skillsToLearn.isNotEmpty()) {
                                Text(
                                    text = "SKILLS TO LEARN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    skillsToLearn.forEach { skill ->
                                        Surface(
                                            color = SkillBg,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Text(
                                                text = skill,
                                                color = SkillText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // 2. Weekly Milestones
                            if (weeklyMilestones.isNotEmpty()) {
                                Text(
                                    text = "WEEKLY MILESTONES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                weeklyMilestones.forEach { milestone ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Flag,
                                            contentDescription = "milestone",
                                            tint = SkillSecondary,
                                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = milestone,
                                            color = SkillText,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // 3. Recommended Resources
                            if (resources.isNotEmpty()) {
                                Text(
                                    text = "RECOMMENDED COMPLIMENTARY RESOURCES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillSuccess,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                resources.forEach { res ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = "resourceLink",
                                            tint = SkillSuccess,
                                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = res,
                                            color = SkillSuccess,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // 4. Hands-on Training Projects
                            if (projects.isNotEmpty()) {
                                Text(
                                    text = "PRACTICAL PROJECTS & CHALLENGES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                projects.forEach { proj ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = "project",
                                            tint = SkillSecondary,
                                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = proj,
                                            color = SkillText,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // 5. Industry Certifications
                            if (certifications.isNotEmpty()) {
                                Text(
                                    text = "TARGET CERTIFICATIONS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                certifications.forEach { cert ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CardMembership,
                                            contentDescription = "certification",
                                            tint = SkillPrimary,
                                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cert,
                                            color = SkillText,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // 6. Checkable Tasks
                            if (phaseTasks.isNotEmpty()) {
                                Text(
                                    text = "CHECKLIST PROGRESSION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillText,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                phaseTasks.forEach { task ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SkillSurface),
                                        border = BorderStroke(
                                            1.dp,
                                            if (task.isCompleted) SkillSuccess.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .testTag("roadmap_task_${task.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = task.title,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (task.isCompleted) SkillMutedText else SkillText
                                                    )
                                                    if (task.isAiGenerated) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            color = SkillPrimary.copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "AI Generated",
                                                                color = SkillPrimary,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = task.description,
                                                    fontSize = 12.sp,
                                                    color = SkillMutedText,
                                                    lineHeight = 16.sp
                                                )
                                            }

                                            Checkbox(
                                                checked = task.isCompleted,
                                                onCheckedChange = { viewModel.toggleTaskComplete(task.id, it) },
                                                colors = CheckboxDefaults.colors(checkedColor = SkillSuccess),
                                                modifier = Modifier.testTag("task_checkbox_${task.id}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ------------------------------------------------------------------------
// 5.C SKILL LIBRARY TAB LAYOUT
// ------------------------------------------------------------------------
@Composable
fun SkillLibraryLayout(viewModel: SkillStackerViewModel) {
    val skills = viewModel.skillLibrary
    val categories = listOf("Tech", "Creative", "Business", "Finance", "Communication")
    var selectedCategory by remember { mutableStateOf("Tech") }

    val filteredSkills = skills.filter { it.category == selectedCategory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Skill Library",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillText,
                modifier = Modifier.testTag("skills_header_title")
            )
            Text(
                text = "Study core operational cards & assess income specs.",
                fontSize = 14.sp,
                color = SkillMutedText
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Categories LazyRow filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("skills_category_row")
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        modifier = Modifier.clickable { selectedCategory = cat },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SkillPrimary else SkillSurface,
                        border = BorderStroke(1.dp, if (isSelected) SkillPrimary else Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else SkillText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Skills List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).testTag("skills_list")
        ) {
            items(filteredSkills) { sk ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkillSurface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("skill_card_${sk.name}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sk.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillText
                            )

                            // Difficulty label
                            val diffColor = when (sk.difficulty) {
                                "Beginner" -> SkillSuccess
                                "Intermediate" -> SkillPrimary
                                else -> SkillSecondary
                            }
                            Surface(
                                color = diffColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, diffColor.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = sk.difficulty,
                                    color = diffColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "ESTIMATED TIME", color = SkillMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = sk.estimatedTime, color = SkillText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "INCOME POTENTIAL", color = SkillMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = sk.incomePotential, color = SkillSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// 5.D RESOURCES TAB LAYOUT
// ------------------------------------------------------------------------
@Composable
fun ResourcesLayout(viewModel: SkillStackerViewModel) {
    val items = viewModel.curatedResources
    val categories = listOf("Courses", "YouTube Channels", "Certifications", "Platforms")
    var selectedCategory by remember { mutableStateOf("Courses") }

    val filteredItems = items.filter { it.category == selectedCategory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Curated Resources",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillText,
                modifier = Modifier.testTag("resources_header_title")
            )
            Text(
                text = "Premium material to help complete roadmap phases.",
                fontSize = 14.sp,
                color = SkillMutedText
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Filter row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("resources_category_row")
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        modifier = Modifier.clickable { selectedCategory = cat },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SkillSecondary else SkillSurface,
                        border = BorderStroke(1.dp, if (isSelected) SkillSecondary else Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else SkillText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Resources list
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).testTag("resources_list")
        ) {
            items(filteredItems) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkillSurface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("resource_card_${item.name}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillText
                            )

                            Surface(
                                color = if (item.cost == "Free") SkillSuccess.copy(alpha = 0.15f) else SkillPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                              ) {
                                Text(
                                    text = item.cost,
                                    color = if (item.cost == "Free") SkillSuccess else SkillPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Provider: ${item.provider}",
                            fontSize = 12.sp,
                            color = SkillSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = item.description,
                            fontSize = 13.sp,
                            color = SkillMutedText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// 5.E PROFILE TAB LAYOUT (Includes Edit Profile inline and Gap analyzer)
// ------------------------------------------------------------------------
@Composable
fun ProfileLayout(viewModel: SkillStackerViewModel) {
    val profile by viewModel.latestProfile.collectAsStateWithLifecycle()
    val gapReport by viewModel.skillGapReport.collectAsStateWithLifecycle()
    val isAnalyzingGap by viewModel.isAnalyzingGap.collectAsStateWithLifecycle()
    val isGeminiActive by viewModel.isGeminiActive.collectAsStateWithLifecycle()

    var isEditing by remember { mutableStateOf(false) }

    // Edit form states
    var editName by remember { mutableStateOf("") }
    var editBranch by remember { mutableStateOf("") }
    var editYear by remember { mutableStateOf("") }
    var editGoal by remember { mutableStateOf("") }
    var editLevel by remember { mutableStateOf("") }
    var editInterestsSet by remember { mutableStateOf(setOf<String>()) }

    // Local lists for selections
    val branchesList = listOf("CSE", "ECE", "ME", "Civil", "Other")
    val yearsList = listOf("1st", "2nd", "3rd", "4th")
    val goalsList = listOf(
        "Full Stack Developer", "Cybersecurity Engineer", "Data Scientist", "Cloud Engineer",
        "UI/UX Designer", "App Developer", "Video Editor", "Graphic Designer", "Content Creator",
        "Freelancer", "Entrepreneur", "Digital Marketer", "Personal Finance", "Investing",
        "Public Speaking", "Leadership"
    )
    val levelsList = listOf("Beginner", "Intermediate", "Advanced")
    val allInterests = listOf(
        "Web Development", "Cybersecurity", "Data Science", "UI/UX", "Video Editing",
        "Copywriting", "Digital Marketing", "Investing", "Public Speaking", "Entrepreneurship"
    )

    // Trigger edit initialization
    LaunchedEffect(isEditing, profile) {
        if (isEditing && profile != null) {
            editName = profile!!.fullName
            editBranch = profile!!.branch
            editYear = profile!!.year
            editGoal = profile!!.careerGoal
            editLevel = profile!!.currentLevel
            editInterestsSet = profile!!.interests.split(", ").filter { it.isNotEmpty() }.toSet()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillText,
                    modifier = Modifier.testTag("profile_header_title")
                )
                Button(
                    onClick = {
                        if (isEditing) {
                            // save action
                            viewModel.editProfile(
                                editName, editBranch, editYear, editGoal, editLevel, editInterestsSet
                            )
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isEditing) SkillSuccess else SkillPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp).testTag("profile_edit_toggle_button")
                ) {
                    Text(if (isEditing) "Save Changes" else "Edit Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (isEditing) {
            // ---- EDIT MODE PANEL ----
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkillSurface),
                    border = BorderStroke(1.dp, SkillPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Modify Settings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillText
                        )

                        // Name
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            textStyle = LocalTextStyle.current.copy(color = SkillText),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SkillPrimary, unfocusedBorderColor = Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                            singleLine = true
                        )

                        // Branch select
                        Column {
                            Text("Academic Branch", fontSize = 12.sp, color = SkillMutedText, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                branchesList.forEach { valSelected ->
                                    val actSel = editBranch == valSelected
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { editBranch = valSelected }
                                            .testTag("edit_branch_chip_$valSelected"),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (actSel) SkillPrimary else SkillSurface,
                                        border = BorderStroke(1.dp, if (actSel) SkillPrimary else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = valSelected,
                                            color = if (actSel) Color.White else SkillText,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Year Select
                        Column {
                            Text("Current Year", fontSize = 12.sp, color = SkillMutedText, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                yearsList.forEach { valSelected ->
                                    val actSel = editYear == valSelected
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { editYear = valSelected }
                                            .testTag("edit_year_chip_$valSelected"),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (actSel) SkillSecondary else SkillSurface,
                                        border = BorderStroke(1.dp, if (actSel) SkillSecondary else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = valSelected,
                                            color = if (actSel) Color.White else SkillText,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Goal Select (simple text outline / or selector row)
                        Column {
                            Text("Career Goal", fontSize = 12.sp, color = SkillMutedText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            // Row of standard targets or scrollable list
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(goalsList) { goalOption ->
                                    val actSel = editGoal == goalOption
                                    Surface(
                                        modifier = Modifier.clickable { editGoal = goalOption },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (actSel) SkillPrimary else SkillSurface,
                                        border = BorderStroke(1.dp, if (actSel) SkillPrimary else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = goalOption,
                                            color = if (actSel) Color.White else SkillText,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Current Level
                        Column {
                            Text("Personal Experience Level", fontSize = 12.sp, color = SkillMutedText, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                levelsList.forEach { lvl ->
                                    val actSel = editLevel == lvl
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { editLevel = lvl },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (actSel) SkillSecondary else SkillSurface,
                                        border = BorderStroke(1.dp, if (actSel) SkillSecondary else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = lvl,
                                            color = if (actSel) Color.White else SkillText,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Edit Interests chips
                        Column {
                            Text("Choose Interests", fontSize = 12.sp, color = SkillMutedText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                items(allInterests) { intr ->
                                    val isSelected = editInterestsSet.contains(intr)
                                    Surface(
                                        modifier = Modifier.clickable {
                                            val nextSet = editInterestsSet.toMutableSet()
                                            if (isSelected) nextSet.remove(intr) else nextSet.add(intr)
                                            editInterestsSet = nextSet
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) SkillSecondary.copy(alpha = 0.15f) else SkillSurface,
                                        border = BorderStroke(1.dp, if (isSelected) SkillSecondary else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = intr,
                                            color = if (isSelected) SkillSecondary else SkillText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillText),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    viewModel.editProfile(
                                        editName, editBranch, editYear, editGoal, editLevel, editInterestsSet
                                    )
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkillSuccess),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.5f).testTag("edit_profile_save_btn")
                            ) {
                                Text("Save Profile", color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            // ---- NORMAL VIEW MODE ----
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkillSurface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_view_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colors = listOf(SkillPrimary, SkillSecondary))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (profile?.fullName?.take(1) ?: "S").uppercase(),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = profile?.fullName ?: "Skill Scholar",
                                    color = SkillText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = profile?.email ?: "azeemd1046@gmail.com",
                                    color = SkillMutedText,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Key detail indices columns
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("ACADEMIC FIELD", color = SkillMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${profile?.branch} Branch (${profile?.year} Year)", color = SkillText, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TARGET REVEAL", color = SkillMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(profile?.currentLevel ?: "Beginner", color = SkillSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("STATED INTEREST CHIPS", color = SkillMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Wrap row layout for personal interests
                        val intList = profile?.interests?.split(", ")?.filter { it.isNotEmpty() } ?: emptyList()
                        if (intList.isEmpty()) {
                            Text("No complementary interests registered.", color = SkillMutedText, fontSize = 12.sp)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                intList.take(3).forEach { chip ->
                                    Surface(
                                        color = SkillBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = chip,
                                            color = SkillText,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (intList.size > 3) {
                                    Text("+${intList.size - 3} more", color = SkillPrimary, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterVertically))
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Skill Gap Analyzer Module (Inputs, Outputs, Readiness score)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SkillSurface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("skill_gap_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Skill Gap Analyzer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillText
                            )
                            Text(
                                text = "Assess market readiness dynamically",
                                fontSize = 12.sp,
                                color = SkillMutedText
                            )
                        }
                        IconButton(
                            onClick = { viewModel.analyzeSkillGap() },
                            modifier = Modifier.background(SkillSecondary.copy(alpha = 0.1f), CircleShape).testTag("trigger_gap_btn")
                        ) {
                            if (isAnalyzingGap) {
                                CircularProgressIndicator(color = SkillSecondary, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Run Gap Analysis", tint = SkillSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (gapReport != null) {
                        val report = gapReport!!

                        // Readiness gauge (30% Apple style minimalist arc/gauge layout representation)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(SkillBg),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = report.readinessScore.toFloat() / 100f,
                                    color = if (report.readinessScore > 50) SkillSuccess else SkillSecondary,
                                    trackColor = Color(0xFFE2E8F0),
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 6.dp
                                )
                                Text(
                                    text = "${report.readinessScore}%",
                                    color = SkillText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Market Readiness Score",
                                    fontSize = 14.sp,
                                    color = SkillText,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Based on commercial benchmarks as of June 2026.",
                                    fontSize = 11.sp,
                                    color = SkillMutedText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("IDENTIFIED INDUSTRY GAPS", color = SkillSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        report.missingSkills.forEach { gap ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = "Gap details", tint = SkillSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = gap, color = SkillText, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("TACTICAL STEPS", color = SkillPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        report.recommendations.forEach { rec ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ArrowRightAlt, contentDescription = "Step icon", tint = SkillPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = rec, color = SkillMutedText, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "Please tap the analysis refresh arrow in the top right to analyze gaps.",
                            color = SkillMutedText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Action panel (Sign Out option)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.handleSignOut() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("profile_sign_out")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = SkillText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = SkillText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
