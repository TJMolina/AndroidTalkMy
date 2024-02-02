package com.tjm.talkmy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.MenuProvider
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.tjm.talkmy.databinding.ActivityMainBinding
import com.tjm.talkmy.ui.taskEdit.EditTaskFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var firstTime = true
    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        screenSplash.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUtils()

    }

    private fun initUtils() {
        initToolbar()
        Logger.addLogAdapter(AndroidLogAdapter())
    }

    override fun onResume() {
        initSharedListener()
        super.onResume()
    }


    private fun initToolbar() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.top_app_bar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return true
            }
        })
    }
    private fun initSharedListener() {
        val urlCompartido = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!urlCompartido.isNullOrEmpty()) {
            val fragment = EditTaskFragment().apply {
                arguments = Bundle().apply {
                    putString("url", urlCompartido)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }
        intent.replaceExtras(Bundle().apply {
            putString(
                "nothig",
                ""
            )
        }) // Elimina el extra del intent después de usarlo
    }
}
