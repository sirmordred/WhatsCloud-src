package app.mordred.whatscloud.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
    }

    /* Use custom menu items to inflate activity menu. */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Get menu inflater.
        val menuInflater = menuInflater

        // Inflate the menu with custom menu items.
        menuInflater.inflate(R.menu.custom_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /* Triggered when use click menu item. */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Get menu item id.
        val itemId = item.itemId

        when (itemId) {
            R.id.menu_pref1 -> Toast.makeText(this, "Pref1 clicked", Toast.LENGTH_LONG).show()
            R.id.menu_pref2 -> Toast.makeText(this, "Pref2 clicked", Toast.LENGTH_LONG).show()
            R.id.menu_pref3 -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
