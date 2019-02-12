package app.mordred.whatscloud

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.mordred.wordcloud.WordCloud
import com.mordred.wordcloud.WordFrequency
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var barChart: BarChart? = null
    var chat: Chat? = null
    var chatTitleTv: TextView? = null
    var chatWdImgView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatTitleTv = findViewById(R.id.chatTv)
        chatWdImgView = findViewById(R.id.chatWdImg)
        barChart = findViewById(R.id.chart)
        barChart?.isEnabled = false
        barChart?.visibility = View.INVISIBLE
        barChart?.setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
                Log.e("VAL SELECTED", "Value: " + e?.`val` + ", index: " + h?.xIndex
                        + ", DataSet index: ")
            }
            override fun onNothingSelected() {
                // empty
            }
        })

        when {
            intent?.action == Intent.ACTION_SEND_MULTIPLE -> {
                val inputUri: Uri = (intent?.extras?.get(Intent.EXTRA_STREAM) as ArrayList<*>)[0] as Uri
                Processer(this).execute(inputUri)
            } else -> {
                // application opened normally
            }
        }


    }

    @SuppressLint("StaticFieldLeak")
    class Processer(private var activity: MainActivity) : AsyncTask<Uri, Void, BarData?>() {

        var pd: ProgressDialog? = null
        var barEntries: MutableList<BarEntry> = mutableListOf()
        //var msgList: MutableList<Message> = mutableListOf()
        //var userMsgList = UserMessage()

        private fun getNameFromUri(cntresolver: ContentResolver, uri: Uri): String? {
            val cursor = cntresolver.query(uri, null, null, null, null)
            if (cursor != null) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val name = cursor.getString(index)
                cursor.close()
                return name.removeSuffix(".txt")
            }
            return null
        }

        override fun onPreExecute() {
            pd = ProgressDialog(activity)
            pd?.setTitle("Processer")
            pd?.setMessage("Processing... Please wait...")
            pd?.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg p0: Uri): BarData? {
            val inpStream = activity.contentResolver?.openInputStream(p0[0])
            val chatName = getNameFromUri(activity.contentResolver, p0[0])
            if (chatName != null) {
                activity.chat = Chat(chatName)
            } else {
                activity.chat = Chat("WP")
            }
            if (inpStream != null) {
                val outputStream = FileOutputStream(File(activity.filesDir?.absolutePath, "wp.txt"))

                inpStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                outputStream.flush()
                outputStream.close()
                inpStream.close()

                File(activity.filesDir?.absolutePath, "wp.txt").forEachLine {
                    if (it.isNotEmpty() && it.isNotBlank()) {
                        try {
                            val str: String = it.substring(it.indexOf('-') + 2,it.length)
                            val msgText = str.substring(str.indexOf(':') + 1,str.length).trimStart()
                            if (!msgText.startsWith('<') && !msgText.startsWith("www") &&
                                    !msgText.startsWith("http")) {
                                val msgDate = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                                    .parse(it.substring(0,it.indexOf(' ')))
                                val msgOwner = str.substring(0,str.indexOf(':'))

                                //activity.chat?.chatMsgList?.add(Message(msgDate, msgOwner, msgText))
                                activity.chat?.charUsrMsgMap?.add(Message(msgDate, msgOwner, msgText))
                            }
                        } catch (e: Exception) {
                            // empty handler
                        }
                    }
                }

                if (activity.chat?.charUsrMsgMap?.size!! > 0) {
                    ////////// WORDCLOUD BEGIN ///////////
                    //val usrWf = WordFrequency()
                    val wf = WordFrequency()
                    for ((_,msgList) in activity.chat?.charUsrMsgMap!!) {
                        for(msg in msgList) {
                            wf.insertWord(msg.messageText)
                            //usrWf.insertWord(msg.messageText);
                        }
                        //val usrWd = WordCloud(usrWf.generate(15),240,400, Color.BLACK, Color.WHITE)
                        //usrWd.setWordColorOpacityAuto(true)
                        //activity.chat?.chatUsrBmpList?.put(user, usrWd.generate())
                    }
                    val wd = WordCloud(wf.generate(30),480,480,
                        Color.BLACK, Color.WHITE)
                    wd.setWordColorOpacityAuto(true)
                    wd.setPaddingX(20)
                    wd.setPaddingY(20)
                    activity.chat?.chatBmp = wd.generate()
                    ////////// WORDCLOUD END ///////////

                    // generate barentries
                    for ((count, usrMsg) in activity.chat?.getUserMsgList()!!.withIndex()) {
                        barEntries.add(BarEntry(usrMsg.size.toFloat(), count))
                    }

                    // generate bardataset
                    val barDataSet = BarDataSet(barEntries, "MessageCount")
                    barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toMutableList()

                    // generate bardata and return
                    return BarData(activity.chat?.charUsrMsgMap?.keys?.toTypedArray(), barDataSet)
                }
            }
            return null
        }

        override fun onPostExecute(result: BarData?) {
            if (result != null) {
                activity.chatTitleTv?.text = activity.chat?.chatTitle
                activity.barChart?.isEnabled = true
                activity.barChart?.setData(result)
                activity.barChart?.xAxis?.setLabelsToSkip(0)
                activity.barChart?.invalidate()
                activity.barChart?.visibility = View.VISIBLE
                activity.chatWdImgView?.setImageBitmap(activity.chat?.chatBmp)
            }
            if (pd != null) {
                pd?.dismiss()
            }
            super.onPostExecute(result)
        }
    }
}
