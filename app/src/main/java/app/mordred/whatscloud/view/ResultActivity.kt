package app.mordred.whatscloud.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.mordred.whatscloud.R
import app.mordred.whatscloud.ad.AdManager
import app.mordred.whatscloud.billing.BillingManager
import app.mordred.whatscloud.presenter.Analyzer
import com.github.aakira.expandablelayout.ExpandableLayoutListener
import com.github.aakira.expandablelayout.ExpandableWeightLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.miguelcatalan.materialsearchview.MaterialSearchView
import java.util.*

class ResultActivity : AppCompatActivity() {

    var barChart: BarChart? = null
    var hrzBarChart: BarChart? = null
    var pieChart: PieChart? = null
    var chatTitleTv: TextView? = null
    var chatMsgCountTv: TextView? = null
    var chatMsgWordCountTv: TextView? = null
    var chatMsgFreqTv: TextView? = null
    var chatWdImgView: ImageView? = null
    var chatTitleDropDownInd: ImageView? = null
    var chatExpandLayout: ExpandableWeightLayout? = null
    var chatUsrListRecyclerView: RecyclerView? = null
    var chatTitleHeader: LinearLayout? = null
    var defLang: String = ""

    var searchView: MaterialSearchView? = null

    var customStopWordList: Set<String>? = null
    var customWordCloudWordCount: Int = 30

    var billMng: BillingManager? = null
    var adMng: AdManager? = null

    var exitAlert: AlertDialog? = null

    var isTest = false

    var analyzer: Analyzer? = null

    var shPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        searchView = findViewById(R.id.search_view)

        billMng = BillingManager(this)
        adMng = AdManager(this)

        defLang = getCountryCode()
        chatTitleHeader = findViewById(R.id.llChatTitle)
        chatTitleHeader?.visibility = View.INVISIBLE
        chatTitleTv = findViewById(R.id.chatTv)
        chatMsgCountTv = findViewById(R.id.tv_chat_msgcount)
        chatMsgWordCountTv = findViewById(R.id.tv_chat_msgwordcount)
        chatMsgFreqTv = findViewById(R.id.tv_chat_msgfreq)
        chatWdImgView = findViewById(R.id.chatWdImg)

        //barchart
        barChart = findViewById(R.id.chart)
        barChart?.setDescriptionTextSize(16f)
        barChart?.xAxis?.textSize = 16f

        //horizontal barchart
        hrzBarChart = findViewById(R.id.horizontalBarChart)
        hrzBarChart?.setDescriptionTextSize(16f)
        val xAxis = hrzBarChart?.xAxis
        xAxis?.textSize = 16f
        xAxis?.setDrawGridLines(false)
        xAxis?.textColor = Color.WHITE
        xAxis?.position = XAxis.XAxisPosition.BOTTOM
        xAxis?.isEnabled = true
        xAxis?.setDrawAxisLine(false)

        //piechart
        pieChart = findViewById(R.id.piechart)
        pieChart?.setDescriptionTextSize(16f)
        pieChart?.setUsePercentValues(true)

        chatUsrListRecyclerView = findViewById(R.id.chat_userlist_recyclerview)
        chatUsrListRecyclerView?.layoutManager = LinearLayoutManager(this)

        chatTitleDropDownInd = findViewById(R.id.chatTitleDropDown)

        chatExpandLayout = findViewById(R.id.expandableLayout)
        chatTitleHeader?.setOnClickListener {
            chatExpandLayout?.toggle()
        }
        chatExpandLayout?.setListener(object : ExpandableLayoutListener {
            override fun onAnimationStart() {
                // empty
            }

            override fun onAnimationEnd() {
                // empty
            }

            override fun onPreOpen() {
                // empty
            }

            override fun onPreClose() {
                // empty
            }

            override fun onOpened() {
                toggleDropDownImgview(chatTitleDropDownInd, true)
            }

            override fun onClosed() {
                toggleDropDownImgview(chatTitleDropDownInd, false)
            }
        })

        searchView?.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                // empty
            }

            override fun onSearchViewShown() {
                // if expandable view is open, close it since we are searching specific user on the list view
                if (chatExpandLayout?.isExpanded!!) {
                    chatExpandLayout?.collapse()
                }
            }
        })

        if (exitAlert == null) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.dialog_exit_title))
            builder.setCancelable(false)
            builder.setPositiveButton(getString(R.string.dialog_yes_label)) { _,_ ->
                finish()
            }
            builder.setNegativeButton(getString(R.string.dialog_no_label)) { dialog,_ ->
                dialog.cancel()
            }
            exitAlert = builder.create() as AlertDialog
        }

        pieChart?.isEnabled = false
        pieChart?.visibility = View.INVISIBLE
        hrzBarChart?.isEnabled = false
        hrzBarChart?.visibility = View.INVISIBLE
        barChart?.isEnabled = false
        barChart?.visibility = View.INVISIBLE

        // Get default values from sharedpref
        shPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        customStopWordList = shPref?.getStringSet("custStpWrds", mutableSetOf())
        customWordCloudWordCount = shPref?.getInt("defWordCntInWd", 30)!!


        if (intent != null && intent?.extras != null && intent?.extras?.getBoolean("isTestAnalyzer",
                false)!!) {
            // TEST Case
            isTest = true
            Analyzer(this).execute(Uri.EMPTY)
        } else {
            // NON-TEST Case
            if (intent != null && intent?.extras != null
                && intent.action != null && intent.action == Intent.ACTION_SEND_MULTIPLE) {
                val inputUri: Uri = (intent.extras?.get(Intent.EXTRA_STREAM) as ArrayList<*>)[0] as Uri
                analyzer = Analyzer(this)
                analyzer?.execute(inputUri)
            } else {
                // application opened normally
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    /* Use custom menu items to inflate activity menu. */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.custom_search_menu, menu)

        val menuItem = menu.findItem(R.id.action_search)
        searchView?.setMenuItem(menuItem)
        return true
    }

    /* Triggered when use click menu item. */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Get menu item id.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        return if (id == R.id.action_search) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onBackPressed() {
        if (searchView?.isSearchOpen!!) {
            searchView?.closeSearch()
        } else {
            if (!exitAlert?.isShowing!!) {
                exitAlert?.show()
            }
        }
    }

    private fun getCountryCode(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0).language
        } else {
            Resources.getSystem().configuration.locale.language
        }
    }

    private fun toggleDropDownImgview(imgView: ImageView?, isOpened: Boolean) {
        if (isOpened) {
            imgView?.setImageResource(R.mipmap.ic_collapse)
        } else {
            imgView?.setImageResource(R.mipmap.ic_expand)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billMng?.bp?.handleActivityResult(requestCode, resultCode, data)!!)
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        billMng?.updateProductStatus()
    }

    override fun onDestroy() {
        billMng?.destroy()
        analyzer?.dismissProgressDialog()
        super.onDestroy()
    }
}