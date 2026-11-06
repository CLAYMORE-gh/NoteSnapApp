package com.example.notesnap.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.notesnap.R
import com.example.notesnap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: androidx.navigation.NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.pagesFragment, R.id.scanFragment)
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        // Handle top-level navigation manually so the already-selected tab
        // (e.g. Home) still re-navigates when Scan is on screen.
        binding.bottomNav.setOnItemSelectedListener { item ->
            navigateTopLevel(item.itemId)
            true
        }
        binding.bottomNav.setOnItemReselectedListener { item ->
            navigateTopLevel(item.itemId)
        }

        binding.fabScan.setOnClickListener {
            navigateTopLevel(R.id.scanFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val id = destination.id
            val isNested = id == R.id.editorFragment || id == R.id.detailFragment
            binding.bottomBarContainer.visibility = if (isNested) View.GONE else View.VISIBLE

            when (id) {
                R.id.homeFragment, R.id.pagesFragment, R.id.scanFragment -> {
                    binding.bottomNav.menu.findItem(id)?.isChecked = true
                }
            }
        }
    }

    private fun navigateTopLevel(destId: Int) {
        if (navController.currentDestination?.id == destId) return

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.homeFragment, false)
            .build()

        navController.navigate(destId, null, options)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.pagesFragment, R.id.scanFragment)
        )
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
