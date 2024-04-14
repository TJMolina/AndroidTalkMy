package com.tjm.talkmy

import android.content.Intent
import android.content.res.Resources.Theme
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.databinding.ActivityMainBinding
import com.tjm.talkmy.ui.taskEdit.EditTaskFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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
        initSharedListener(intent.getStringExtra(Intent.EXTRA_TEXT))
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


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            val argument = it.getStringExtra(Intent.EXTRA_TEXT)
            val fragment = supportFragmentManager.findFragmentByTag("EditTaskFragment")
            if (fragment != null && fragment is EditTaskFragment) {
                fragment.reiveTask(argument!!)
            } else {
                initSharedListener(argument)
            }
        }
    }

    private fun initSharedListener(url: String?) {
        if (!url.isNullOrEmpty()) {
            val fragmentTag = "EditTaskFragment"

            val fragment = EditTaskFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment, fragmentTag).addToBackStack(null)
                .commit()
        }
        intent.replaceExtras(Bundle().apply {
            putString(
                "nothig", ""
            )
        })
    }

}
