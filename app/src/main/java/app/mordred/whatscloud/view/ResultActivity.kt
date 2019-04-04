package app.mordred.whatscloud.view

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.mordred.whatscloud.R
import app.mordred.whatscloud.presenter.Analyzer
import com.github.aakira.expandablelayout.ExpandableLayoutListener
import com.github.aakira.expandablelayout.ExpandableWeightLayout
import com.github.mikephil.charting.charts.BarChart
import java.util.*

class ResultActivity : AppCompatActivity() {

    var barChart: BarChart? = null
    var chatTitleTv: TextView? = null
    var chatMsgCountTv: TextView? = null
    var chatMsgFreqTv: TextView? = null
    var chatWdImgView: ImageView? = null
    var chatExpandLayout: ExpandableWeightLayout? = null
    var chatUsrListRecyclerView: RecyclerView? = null
    var defLang: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        defLang = getCountryCode()
        chatTitleTv = findViewById(R.id.chatTv)
        chatMsgCountTv = findViewById(R.id.tv_chat_msgcount)
        chatMsgFreqTv = findViewById(R.id.tv_chat_msgfreq)
        chatWdImgView = findViewById(R.id.chatWdImg)
        barChart = findViewById(R.id.chart)

        chatUsrListRecyclerView = findViewById(R.id.chat_userlist_recyclerview)
        chatUsrListRecyclerView?.layoutManager = LinearLayoutManager(this)


        chatExpandLayout = findViewById(R.id.expandableLayout)
        chatTitleTv?.setOnClickListener {
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
                toggleDropDownTextview(chatTitleTv, true)
            }

            override fun onClosed() {
                toggleDropDownTextview(chatTitleTv, false)
            }
        })

        barChart?.isEnabled = false
        barChart?.visibility = View.INVISIBLE

        if (intent != null && intent?.extras != null
            && intent.action != null && intent.action == Intent.ACTION_SEND_MULTIPLE) {
            val inputUri: Uri = (intent.extras?.get(Intent.EXTRA_STREAM) as ArrayList<*>)[0] as Uri
            Analyzer(this).execute(inputUri)
        } else {
            // application opened normally
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun getCountryCode(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0).language
        } else {
            Resources.getSystem().configuration.locale.language
        }
    }

    private fun toggleDropDownTextview(tv: TextView?, isOpened: Boolean) {
        if (isOpened) {
            tv?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_collapse, 0)
        } else {
            tv?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_expand, 0)
        }
    }
}