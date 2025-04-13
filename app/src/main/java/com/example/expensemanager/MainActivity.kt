package com.example.expensemanager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        if (auth.currentUser == null) {
            Log.d("MainActivity", "🔐 Chưa có user → đăng nhập ẩn danh")
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Log.d("MainActivity", "✅ Đăng nhập ẩn danh thành công: ${it.user?.uid}")
                    setupBottomNav()
                    bottomNav.selectedItemId = R.id.nav_home
                    loadFragment(HomeFragment())
                }
                .addOnFailureListener {
                    Log.e("MainActivity", "❌ Lỗi đăng nhập ẩn danh: ${it.message}")
                }
        } else {
            Log.d("MainActivity", "✅ Đã có user: ${auth.currentUser?.uid}")
            setupBottomNav()
            bottomNav.selectedItemId = R.id.nav_home
            loadFragment(HomeFragment())
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    Log.d("MainActivity", "🏠 Trang Thu Chi")
                    loadFragment(HomeFragment())
                }
                R.id.nav_calendar -> {
                    Log.d("MainActivity", "📅 Trang Lịch (test)")
                    loadFragment(CalendarFragment()) // tạm load HomeFragment để test
                }

                R.id.nav_report -> {
                    Log.d("MainActivity", "📊 Trang Báo Cáo")
                    loadFragment(ReportFragment())
                }
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



