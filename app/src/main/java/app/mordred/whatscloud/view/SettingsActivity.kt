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
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import app.mordred.whatscloud.R
import app.mordred.whatscloud.adapter.StopWordListAdapter
import app.mordred.whatscloud.billing.BillingManager
import com.github.aakira.expandablelayout.ExpandableRelativeLayout
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    var sharedPref: SharedPreferences? = null
    var stopWordEdx: EditText? = null
    var stpWrdListAdapter: StopWordListAdapter? = null
    var defWordCountInWd = 30
    var tvWordCount: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val customSettingLayout = findViewById<LinearLayout>(R.id.custSettingLayout)
        val exUpgrdProLayout = findViewById<ExpandableRelativeLayout>(R.id.expInAppBillingLayout)
        if(BillingManager.isPremiumApp) {
            exUpgrdProLayout.collapse()
            customSettingLayout.isEnabled = true
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        defWordCountInWd = sharedPref?.getInt("defWordCntInWd", 30)!!

        tvWordCount = findViewById(R.id.tvWordNumVal)
        tvWordCount?.text = defWordCountInWd.toString()

        val wordNumLl = findViewById<LinearLayout>(R.id.llWordNum)
        wordNumLl.setOnClickListener {
            showWordCountInputDialog()
        }

        rv_stopword_list.layoutManager = LinearLayoutManager(this)
        rv_stopword_list.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        stpWrdListAdapter = StopWordListAdapter(this)
        rv_stopword_list.adapter = stpWrdListAdapter

        stopWordEdx = findViewById(R.id.edxStopWord)

        val addStopWordButton = findViewById<Button>(R.id.addStopWordBtn)
        addStopWordButton.setOnClickListener {
            val userEnteredStopWord = stopWordEdx?.text?.toString()?.trimStart()?.trimEnd()
            if (userEnteredStopWord != null && userEnteredStopWord.isNotEmpty() && userEnteredStopWord.isNotBlank()) {
                stpWrdListAdapter?.addElementToList(userEnteredStopWord)
            }
            stopWordEdx?.text?.clear()
        }
    }

    @SuppressLint("InflateParams")
    private fun showWordCountInputDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Number of words in wordcloud")
        val dialogLayout = inflater.inflate(R.layout.input_dialog, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.edxInputDialog)
        editText?.setText(defWordCountInWd.toString())
        builder.setView(dialogLayout)
        builder.setPositiveButton("OK") { dialogInterface, _ ->
            val userEnteredNum = editText.text.toString().toInt()
            if (userEnteredNum in 1..499 && userEnteredNum != defWordCountInWd) {
                defWordCountInWd = userEnteredNum
                sharedPref?.edit()?.putInt("defWordCntInWd", defWordCountInWd)?.apply()
                tvWordCount?.text = userEnteredNum.toString()
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        builder.show()
    }

    /* Triggered when use click menu item. */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Get menu item id.
        when (item.itemId) {
            android.R.id.home-> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}