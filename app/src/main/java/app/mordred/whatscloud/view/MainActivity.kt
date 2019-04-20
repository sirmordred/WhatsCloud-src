package app.mordred.whatscloud.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import app.mordred.whatscloud.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                    "WhatsApp is not installed on this phone", Toast.LENGTH_LONG).show()
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
            R.id.menu_upgrd_pro -> Toast.makeText(this, "UpgradePro preference clicked",
                Toast.LENGTH_LONG).show()
            R.id.menu_info_bug -> {
                val intent = Intent(this, InfoActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
