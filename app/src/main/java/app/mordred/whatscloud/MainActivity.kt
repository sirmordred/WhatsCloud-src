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
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var barChart: BarChart? = null
    var chat: Chat? = null
    var chatTitleTv: TextView? = null
    var chatWdImgView: ImageView? = null
    var wpChatFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wpChatFilePath = filesDir.absolutePath + File.separator + "wp.txt"
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
            pd?.setIndeterminate(true)
            pd?.setCancelable(false)
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
                val outputStream = FileOutputStream(File(activity.wpChatFilePath))

                inpStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                outputStream.flush()
                outputStream.close()
                inpStream.close()

                var bf: BufferedReader? = null
                var fr: FileReader? = null
                try {
                    fr = FileReader(activity.wpChatFilePath)
                    bf = BufferedReader(fr)

                    var currLine: String? = bf.readLine()
                    while (currLine != null) {
                        if (currLine.length > 18) {
                            try {
                                val str: String = currLine.substring(18, currLine.length).trimStart()
                                val msgText = str.substring(str.indexOf(':') + 1, str.length).trimStart()
                                if (!msgText.startsWith('<') && !msgText.startsWith("http")) {
                                    val msgDate = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                                        .parse(currLine.substring(0, 10).trimEnd())
                                    val msgOwner = str.substring(0, str.indexOf(':'))

                                    activity.chat?.add(Message(msgDate, msgOwner, msgText))
                                }
                            } catch (exception: Exception) {
                                // empty handler
                            }
                        }

                        currLine = bf.readLine()
                    }
                } catch (e: Exception) {
                    // empty handler
                } finally {
                    try {
                        bf?.close()
                        fr?.close()
                    } catch (ex: Exception) {
                        // empty handler
                    }
                }

                if (activity.chat?.getUserSize()!! > 0) {
                    val wd = WordCloud(activity.chat?.commonWordFreq?.generate(30),
                        480,480, Color.BLACK, Color.WHITE)
                    wd.setWordColorOpacityAuto(true)
                    wd.setPaddingX(5)
                    wd.setPaddingY(5)
                    wd.setBoundingYAxis(false)
                    activity.chat?.chatCommonWordCloud = wd.generate()

                    // generate barentries
                    var count = 0
                    activity.chat?.userMessageMap?.forEach { (_, userObjects) ->
                        barEntries.add(BarEntry(userObjects.usrMsgCount.toFloat(), count))
                        count++
                    }

                    // generate bardataset
                    val barDataSet = BarDataSet(barEntries, "MessageCount")
                    barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toMutableList()

                    // generate bardata and return
                    return BarData(activity.chat?.getUserNameList(), barDataSet)
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
                activity.chatWdImgView?.setImageBitmap(activity.chat?.chatCommonWordCloud)
            }
            if (pd != null) {
                pd?.dismiss()
            }
            super.onPostExecute(result)
        }
    }
}
