package com.cardwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cardwise.app.ui.CardWiseApp
import com.cardwise.app.ui.theme.CardWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CardWiseTheme {
                CardWiseApp()
            }
        }
    }
}
