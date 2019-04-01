package app.mordred.whatscloud.view

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import app.mordred.whatscloud.R
import app.mordred.whatscloud.adapter.StopWordListAdapter
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    var customStopWordList: MutableList<String>? = null
    var sharedPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val wordNumLl = findViewById<LinearLayout>(R.id.llWordNum)
        wordNumLl.setOnClickListener {
            showTextInputDialog()
        }

        customStopWordList  = sharedPref?.getStringSet("custStpWrds", mutableSetOf())?.toMutableList()

        rv_stopword_list.layoutManager = LinearLayoutManager(this)
        rv_stopword_list.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        rv_stopword_list.adapter = StopWordListAdapter(customStopWordList, this)
    }

    @SuppressLint("InflateParams")
    private fun showTextInputDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("With EditText")
        val dialogLayout = inflater.inflate(R.layout.input_dialog, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.edxInputDialog)
        builder.setView(dialogLayout)
        builder.setPositiveButton("OK") { _, _ ->
            Toast.makeText(applicationContext, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        builder.show()
    }

    /* Triggered when use click menu item. */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Get menu item id.
        val itemId = item.itemId

        when (itemId) {
            android.R.id.home-> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}