package app.mordred.whatscloud

import app.mordred.whatscloud.util.Util
import com.mordred.wordcloud.WordFrequency
import java.util.*

class User(usrMsgWordFrequency: WordFrequency) {
    var usrMsgWordFreq: WordFrequency? = null
    var usrMsgCount: Int = 0
    var usrWordCount: Int = 0
    var firstMsgDate: Date? = null
    var lastMsgDate: Date? = null
    val usrDateCountMap: HashMap<Date, Int> = HashMap()
    val usrDayCountMap: HashMap<String, Int> = HashMap()

    init {
        usrMsgWordFreq = usrMsgWordFrequency
    }

    fun addMsg(msg: Message) {
        usrMsgCount++
        if (firstMsgDate == null) {
            firstMsgDate = msg.messageDate
        }
        lastMsgDate = msg.messageDate
        val wordCnt = Util.insertWordToWordFreq(msg.messageText, usrMsgWordFreq!!)
        usrWordCount += wordCnt
        Util.addToDateCountMap(usrDateCountMap, msg.messageDate)
        Util.addToDayCountMap(usrDayCountMap, msg.messageDate)
    }
}