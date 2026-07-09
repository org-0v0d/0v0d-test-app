package com.org.b0v0d_test_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.org.b0v0d_test_app.ui.SubscriptionApp
import com.org.b0v0d_test_app.ui.theme._0v0dtestappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _0v0dtestappTheme {
                SubscriptionApp()
            }
        }
    }
}
