package com.example.myway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.myway.navigation.MyWayAppNavigation
import com.example.myway.ui.theme.MyWayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWayTheme {
                App() // solo llamas a App()
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    MyWayAppNavigation(navController = navController)
}
