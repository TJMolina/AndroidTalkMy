package com.tjm.talkmy

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
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
    private val multiplePermissionsNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayListOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

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
        receivePermission()
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

    private fun receivePermission() {
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val permission = entry.key
                val isGranted = entry.value
                if (isGranted) {
                    Logger.d("accedio")
                    // El usuario concedió el permiso
                    // openFilePicker()
                } else {
                    // El usuario no concedió el permiso
                }
            }
        }

        requestPermissionLauncher.launch(multiplePermissionsNameList.toTypedArray())
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
                .replace(R.id.fragmentContainerView, fragment, fragmentTag)
                .addToBackStack(null)
                .commit()
        }
        intent.replaceExtras(Bundle().apply {
            putString(
                "nothig",
                ""
            )
        })
    }

}
