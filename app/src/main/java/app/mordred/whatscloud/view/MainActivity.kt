package app.mordred.whatscloud.view

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import app.mordred.whatscloud.BuildConfig
import app.mordred.whatscloud.R
import android.os.Build
import android.content.ActivityNotFoundException
import app.mordred.whatscloud.billing.BillingManager

class MainActivity : AppCompatActivity() {

    var bm: BillingManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bm = BillingManager(this)

        // Get the toolbar component.
        val toolbar = findViewById<View>(R.id.custom_toolbar) as Toolbar

        // Replace current action bar use toolbar.
        setSupportActionBar(toolbar)

        val openWpBtn = findViewById<Button>(R.id.btnGoToWp)
        openWpBtn.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (intent != null) {
                // We found the activity now start the activity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext,
                    getString(R.string.wp_install_error_label), Toast.LENGTH_LONG).show()
            }
        }
    }

    /* Use custom menu items to inflate activity menu. */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu with custom menu items.
        menuInflater.inflate(R.menu.custom_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /* Triggered when use click menu item. */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_upgrd_pro -> bm?.upgradeToPro()
            R.id.menu_undo_upgrd_pro -> bm?.undoUpgradeToPro() // TODO will be removed in production
            R.id.menu_info_bug -> {
                val intent = Intent(this, InfoActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_share_app -> shareApp()
            R.id.menu_rate_app -> rateApp()
            R.id.menu_test_chat -> testAnalyzer() // TODO will be removed in production
        }
        return super.onOptionsItemSelected(item)
    }

    fun testAnalyzer() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("isTestAnalyzer", true)
        startActivity(intent)
    }

    fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "WhatsCloud")
            var shareMessage = getString(R.string.share_app_title)
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (e: Exception) {
            // empty
        }
    }

    fun rateApp() {
        try {
            val rateIntent = rateIntentForUrl("market://details")
            startActivity(rateIntent)
        } catch (e: ActivityNotFoundException) {
            val rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details")
            startActivity(rateIntent)
        }
    }

    private fun rateIntentForUrl(url: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, packageName)))
        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        flags = if (Build.VERSION.SDK_INT >= 21) {
            flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        } else {
            flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
        }
        intent.addFlags(flags)
        return intent
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bm?.bp?.handleActivityResult(requestCode, resultCode, data)!!)
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        bm?.updateProductStatus()
    }

    override fun onDestroy() {
        bm?.destroy()
        super.onDestroy()
    }
}
