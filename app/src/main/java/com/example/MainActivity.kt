package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import android.content.Intent
import com.example.ui.SkillStackerApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SkillStackerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SkillStackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntentData(intent)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    SkillStackerApp(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentData(intent)
    }

    private fun handleIntentData(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "skillstacker" && uri.host == "login") {
            // Extract the access_token from fragment or query params
            val fragment = uri.fragment ?: uri.query ?: ""
            val params = fragment.split("&")
            var token: String? = null
            for (param in params) {
                val pair = param.split("=")
                if (pair.size == 2 && pair[0] == "access_token") {
                    token = pair[1]
                    break
                }
            }
            if (token != null) {
                viewModel.handleSupabaseOauthCallback(token)
            }
        }
    }
}

