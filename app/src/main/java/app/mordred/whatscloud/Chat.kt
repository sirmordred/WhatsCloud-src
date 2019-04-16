package app.mordred.whatscloud

import android.graphics.Bitmap
import app.mordred.whatscloud.view.ResultActivity
import com.mordred.wordcloud.WordFrequency
import java.util.*

class Chat(activity: ResultActivity) {
    var commonWordFreq = WordFrequency()
    var chatCommonWordCloud: Bitmap? = null
    var userMessageMap: HashMap<String, User> = HashMap()
    var chatFirstMsgDate: Date? = null
    var chatLastMsgDate: Date? = null
    var chatTotalMsgCount: Int = 0
    var activity: ResultActivity? = null

    val chatDateCountMap: HashMap<Date, Int> = HashMap()
    val chatDayCountMap: HashMap<String, Int> = HashMap()
    val cal: Calendar = Calendar.getInstance()

    init {
        this.activity = activity

        commonWordFreq.setDefaultStopWords(this.activity, this.activity?.defLang)
        commonWordFreq.setCustomStopWords(this.activity?.customStopWordList?.toHashSet())
    }

    fun add(msg: Message) {
        chatTotalMsgCount++
        if (chatFirstMsgDate == null) {
            chatFirstMsgDate = msg.messageDate
        }
        chatLastMsgDate = msg.messageDate
        commonWordFreq.insertWordSemiNormalized(msg.messageText) // add msg to common word freq generator
        val usr = userMessageMap[msg.messageOwner]
        if (usr != null) {
            // add to usermessage
            usr.addMsg(msg)
        } else {
            // add new usermessagelist
            val userWordFreq = WordFrequency()
            userWordFreq.setDefaultStopWords(activity, activity?.defLang)
            userWordFreq.setCustomStopWords(this.activity?.customStopWordList?.toHashSet())
            val tempUser = User(userWordFreq)
            tempUser.addMsg(msg)
            userMessageMap[msg.messageOwner] = tempUser
        }
        addToDateCountMap(chatDateCountMap, msg.messageDate)
        addToDayCountMap(chatDayCountMap, msg.messageDate)
    }

    fun getUserNameList(): MutableList<String> {
        return userMessageMap.keys.toMutableList()
    }

    fun getUserSize(): Int {
        return userMessageMap.size
    }

    fun addToDateCountMap(givenHm: HashMap<Date,Int>, givenDate: Date) {
        val dateCount = givenHm[givenDate]
        if (dateCount != null) {
            givenHm[givenDate] = dateCount + 1
        } else {
            givenHm[givenDate] = 1
        }
    }

    fun addToDayCountMap(givenHm: HashMap<String,Int>, givenDate: Date) {
        cal.time = givenDate
        val nameOfDay = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        val dateCount = givenHm[nameOfDay]
        if (dateCount != null) {
            givenHm[nameOfDay] = dateCount + 1
        } else {
            givenHm[nameOfDay] = 1
        }
    }
}