package com.example.expensemanager

import android.os.Bundle
import android.text.TextUtils.replace
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.example.expensemanager.ui.theme.ExpenseManagerTheme
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val botomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        loadFragment(HomeFragment())

        botomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_report -> loadFragment(ReportFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
                .commit()

    }
}

