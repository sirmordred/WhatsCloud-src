package app.mordred.whatscloud.presenter

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.provider.OpenableColumns
import android.support.v7.widget.SearchView
import android.view.View
import app.mordred.whatscloud.Chat
import app.mordred.whatscloud.Message
import app.mordred.whatscloud.adapter.UserListAdapter
import app.mordred.whatscloud.model.UserListItem
import app.mordred.whatscloud.view.ResultActivity
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.mordred.wordcloud.WordCloud
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


@SuppressLint("StaticFieldLeak")
class Analyzer(private var activity: ResultActivity) : AsyncTask<Uri, Int, Boolean>() {
    var barEntries: MutableList<BarEntry> = mutableListOf()
    var barDataSet: BarDataSet? = null
    var barData: BarData? = null
    var hrzBarEntries: MutableList<BarEntry> = mutableListOf()
    var hrzBarDataSet: BarDataSet? = null
    var hrzBarData: BarData? = null
    var pieEntries: MutableList<Entry> = mutableListOf()
    var pieData: PieData? = null
    var pd: ProgressDialog = ProgressDialog(activity)
    var chat: Chat? = null
    val resultUserList: ArrayList<UserListItem> = ArrayList()
    var chatMsgCount: Int = 0
    var chatMsgFreq: Int = 0
    var chatTitle: String = "WhatsApp Chat"
    val cal: Calendar = Calendar.getInstance()

    val availableDateFormats = arrayOf(
        SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yy", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd.MM.yy", Locale.ENGLISH)
    )

    override fun onPreExecute() {
        pd.setTitle("Processor")
        pd.setMessage("Processing... Please wait...")
        pd.setCancelable(false)
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        pd.max = 100
        pd.progress = 0
        pd.show()
        super.onPreExecute()
    }

    override fun doInBackground(vararg p0: Uri): Boolean {
        // Extract and assign chat name from given Uri
        chatTitle = getNameFromUri(activity.contentResolver, p0[0])

        chat = Chat(activity)

        val inpStream = activity.contentResolver?.openInputStream(p0[0])
        if (inpStream != null) {
            val sizeOfWpFile = inpStream.available()
            var sizeOfLine = 0L

            var msgDateFormat: SimpleDateFormat? = null

            val bf = BufferedReader(InputStreamReader(inpStream))
            try {
                var currLine: String? = bf.readLine()
                while (currLine != null) {
                    if (currLine.length > 18) {
                        try {
                            if (msgDateFormat == null) {
                                for (dateFormat in availableDateFormats) {
                                    try {
                                        dateFormat.parse(currLine.substring(0, currLine.indexOf(' ')))
                                        msgDateFormat = dateFormat
                                        break
                                    } catch (ex: ParseException) {
                                        // empty handler
                                    }
                                }
                            }

                            val str: String = currLine.substring(currLine.indexOf('-') + 2, currLine.length)
                            val msgTextDelimIndex = str.indexOf(':')
                            val msgText = str.substring(msgTextDelimIndex + 2, str.length)
                            if (!msgText.startsWith('<') && !msgText.startsWith("http")) {
                                val msgDate: Date = if (msgDateFormat != null) {
                                    msgDateFormat.parse(currLine.substring(0, currLine.indexOf(' ')))
                                } else {
                                    Calendar.getInstance().time
                                }
                                val msgOwner = str.substring(0, msgTextDelimIndex)

                                chat?.add(Message(msgDate, msgOwner, msgText))
                            }
                            sizeOfLine += currLine.toByteArray().size.toLong()
                            publishProgress(((sizeOfLine.toFloat() / sizeOfWpFile) * 100).toInt())
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
                    bf.close()
                    inpStream.close()
                } catch (ex: Exception) {
                    // empty handler
                }
            }

            if (chat?.getUserSize()!! > 0) {
                // Append date interval to chat title
                chatTitle += "\n"
                chatTitle += getDateInterval(chat?.chatFirstMsgDate!!, chat?.chatLastMsgDate!!,
                    Locale.getDefault())

                val wd = WordCloud(chat?.commonWordFreq?.generate(activity.customWordCloudWordCount),
                    480,480, Color.BLACK, Color.TRANSPARENT)
                wd.setWordColorOpacityAuto(true)
                wd.setPaddingX(5)
                wd.setPaddingY(5)
                wd.setBoundingYAxis(false)
                chat?.chatCommonWordCloud = placeWpBackground(activity, "wp_bg.png", wd.generate())

                // Calculate chat msg total count
                chatMsgCount = chat?.chatTotalMsgCount!!

                // Calculate chat msg freq
                val diffInMilliesChat = Math.abs(chat?.chatLastMsgDate?.time!! - chat?.chatFirstMsgDate?.time!!)
                var diffInDaysChat = TimeUnit.DAYS.convert(diffInMilliesChat, TimeUnit.MILLISECONDS).toFloat()
                if (diffInDaysChat == 0f) {
                    diffInDaysChat = 1f // Avoid zero-division
                }
                chatMsgFreq = Math.round(chatMsgCount / diffInDaysChat)

                // generate barentries
                var count = 0
                chat?.userMessageMap?.forEach { (userName, userObject) ->
                    // Generate user wordcloud
                    val wdUser = WordCloud(userObject.usrMsgWordFreq?.generate(activity.customWordCloudWordCount),
                        480,480, Color.BLACK, Color.TRANSPARENT)
                    wdUser.setWordColorOpacityAuto(true)
                    wdUser.setPaddingX(5)
                    wdUser.setPaddingY(5)
                    wdUser.setBoundingYAxis(false)

                    // Calculate user msg freq
                    val diffInMillies = Math.abs(userObject.lastMsgDate?.time!! - userObject.firstMsgDate?.time!!)
                    var diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS).toFloat()
                    if (diffInDays == 0f) {
                        diffInDays = 1f // Avoid zero-division
                    }
                    val userobjectMsgFreq: Int = Math.round(userObject.usrMsgCount / diffInDays)

                    val wdUserBmp = wdUser.generate()
                    if (wdUserBmp != null) {
                        resultUserList.add(UserListItem(
                            userName,
                            userObject.usrMsgCount,
                            userobjectMsgFreq,
                            placeWpBackground(activity, "wp_bg.png", wdUserBmp)))
                    }
                    barEntries.add(BarEntry(userObject.usrMsgCount.toFloat(), count))
                    count++
                }

                // generate bardataset
                barDataSet = BarDataSet(barEntries, "MessageCount")
                barDataSet?.colors = ColorTemplate.COLORFUL_COLORS.toMutableList()

                // generate bardata and return
                barData = BarData(chat?.getUserNameList(), barDataSet)

                //GENERATE HORIZONTAL BARCHART
                val topDates = sortAndGetTopNVal(chat?.chatDateCountMap!!, 5)
                count = 0
                for ((_, value) in topDates) {
                    hrzBarEntries.add(BarEntry(value.toFloat(), count))
                    count++
                }

                hrzBarDataSet = BarDataSet(hrzBarEntries, "Results")
                hrzBarDataSet?.setColors(ColorTemplate.LIBERTY_COLORS)
                hrzBarData = BarData(topDates.keys.toTypedArray(), hrzBarDataSet)

                // GENERATE PIECHART
                count = 0
                for ((_, value) in chat?.chatDayCountMap!!) {
                    pieEntries.add(Entry((value.toFloat() * 100) / chat?.chatTotalMsgCount!!, count))
                    count++
                }

                val pieDataSet = PieDataSet(pieEntries, "Results")
                pieDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS)
                pieData = PieData(chat?.chatDayCountMap?.keys?.toTypedArray(), pieDataSet)
                pieData?.setValueFormatter(PercentFormatter())
                return true
            }
        }
        return false
    }

    override fun onProgressUpdate(vararg values: Int?) {
        pd.progress = values[0] as Int
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: Boolean) {
        if (result) {
            activity.chatTitleTv?.text = chatTitle
            activity.barChart?.isEnabled = true
            activity.barChart?.data = barData
            activity.barChart?.xAxis?.setLabelsToSkip(0)
            activity.barChart?.invalidate()
            activity.barChart?.visibility = View.VISIBLE
            // set data and enable horizontal barchart
            activity.hrzBarChart?.isEnabled = true
            activity.hrzBarChart?.data = hrzBarData
            activity.hrzBarChart?.invalidate()
            activity.hrzBarChart?.visibility = View.VISIBLE
            // set data and enable piechart
            activity.pieChart?.isEnabled = true
            activity.pieChart?.data = pieData
            activity.pieChart?.visibility = View.VISIBLE

            activity.chatMsgCountTv?.text = "Total Message Count: " + chatMsgCount.toString()
            activity.chatMsgFreqTv?.text = "Message Sending Frequency: " + chatMsgFreq.toString() + " Msg/Day"
            activity.chatWdImgView?.setImageBitmap(chat?.chatCommonWordCloud)
            val usrListAdapter = UserListAdapter(resultUserList, activity)
            activity.chatUsrListRecyclerView?.adapter = usrListAdapter

            // listening to search query text change
            activity.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    // filter recycler view when query submitted
                    usrListAdapter.filter.filter(query)
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    // filter recycler view when text is changed
                    usrListAdapter.filter.filter(query)
                    return false
                }
            })
        }
        pd.dismiss()
        activity.adMng?.showRewardedVideoAd()
        super.onPostExecute(result)
    }

    private fun placeWpBackground(activity: ResultActivity ,baseImgFileName: String, textBmp: Bitmap): Bitmap {
        var refBmpInp: InputStream? = null
        var refBmp: Bitmap? = null
        try {
            refBmpInp = activity.assets.open(baseImgFileName)
            refBmp = BitmapFactory.decodeStream(refBmpInp)

            val resultBmp = Bitmap.createBitmap(textBmp.width, textBmp.height, Bitmap.Config.ARGB_8888)
            val resultBmpCnv = Canvas(resultBmp)

            if (textBmp.height > refBmp?.height!!) {
                val multiplyNum = textBmp.height / refBmp.height
                val remainingHeight = textBmp.height - (refBmp.height * multiplyNum)

                var tempTop = 0f
                for (i in 0 until multiplyNum) {
                    resultBmpCnv.drawBitmap(refBmp, 0f, tempTop, null)
                    tempTop += refBmp.height
                }
                val remainingBmp = Bitmap.createBitmap(refBmp, 0,0, refBmp.width, remainingHeight)
                resultBmpCnv.drawBitmap(remainingBmp, 0f, tempTop, null)
            } else {
                val remainingBmp = Bitmap.createBitmap(refBmp, 0,0, refBmp.width, textBmp.height)
                resultBmpCnv.drawBitmap(remainingBmp, 0f, 0f, null)
            }

            resultBmpCnv.drawBitmap(textBmp, 0f, 0f, null)

            return resultBmp
        } catch (e: Exception) {
            // empty handler
            e.printStackTrace()
        } finally {
            refBmp?.recycle()
            refBmpInp?.close()
        }
        return textBmp
    }

    private fun getNameFromUri(cntresolver: ContentResolver, uri: Uri): String {
        val cursor = cntresolver.query(uri, null, null, null, null)
        if (cursor != null) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            val name = cursor.getString(index)
            cursor.close()
            return name.removeSuffix(".txt")
        }
        return "WhatsApp Chat"
    }

    private fun getDateInterval(dateStart: Date, dateEnd: Date, localLang: Locale): String {
        val retStr = StringBuilder("")
        cal.time = dateStart
        retStr.append("(").append(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, localLang))
            .append("/").append(cal.get(Calendar.YEAR))
        retStr.append(" - ")
        cal.time = dateEnd
        retStr.append(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, localLang))
            .append("/").append(cal.get(Calendar.YEAR)).append(")")
        return retStr.toString()
    }

    // Special thanx to Kotlin.. you are life saver kotlin :)
    private fun sortAndGetTopNVal(unsortedMap: HashMap<Date, Int>, n: Int): HashMap<String, Int> {
        val topNPair = unsortedMap.toList().sortedByDescending { (_, value) -> value}.take(n).toMap()
        val retMap = hashMapOf<String, Int>()
        for ((key, value) in topNPair) {
            cal.time = key
            val dayName = cal.get(Calendar.DAY_OF_MONTH).toString()
            val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            val yearName = cal.get(Calendar.YEAR).toString()
            retMap.put("$dayName $monthName $yearName", value)
        }
        return retMap
    }
}