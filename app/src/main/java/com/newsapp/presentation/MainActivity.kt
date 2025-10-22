package com.newsapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.newsapp.presentation.auth.BiometricAuthManager
import com.newsapp.presentation.navigation.NewsNavGraph
import com.newsapp.presentation.theme.NewsAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            NewsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isAuthenticated by remember { mutableStateOf(false) }
                    var isCheckingBiometric by remember { mutableStateOf(true) }
                    val isBiometricAvailable = remember {
                        biometricAuthManager.isBiometricAvailable(this)
                    }

                    LaunchedEffect(Unit) {
                        if (isBiometricAvailable) {
                            biometricAuthManager.authenticate(
                                activity = this@MainActivity,
                                onSuccess = {
                                    isAuthenticated = true
                                    isCheckingBiometric = false
                                },
                                onError = { error ->
                                    // Show error but allow access
                                    isAuthenticated = true
                                    isCheckingBiometric = false
                                },
                                onFailed = {
                                    // User cancelled, close the app
                                    finish()
                                }
                            )
                        } else {
                            // No biometric available, proceed directly
                            isAuthenticated = true
                            isCheckingBiometric = false
                        }
                    }

                    when {
                        isCheckingBiometric -> {
                            BiometricAuthScreen()
                        }
                        isAuthenticated -> {
                            val navController = rememberNavController()
                            NewsNavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricAuthScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biometric Authentication",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Authenticating...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
