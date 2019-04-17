package app.mordred.whatscloud

import com.mordred.wordcloud.WordFrequency
import java.util.*

class User(usrMsgWordFrequency: WordFrequency) {
    var usrMsgWordFreq: WordFrequency? = null
    var usrMsgCount: Int = 0
    var firstMsgDate: Date? = null
    var lastMsgDate: Date? = null
    val usrDateCountMap: HashMap<Date, Int> = HashMap()
    val usrDayCountMap: HashMap<String, Int> = HashMap()
    val cal: Calendar = Calendar.getInstance()

    init {
        usrMsgWordFreq = usrMsgWordFrequency
    }

    fun addMsg(msg: Message) {
        usrMsgCount++
        if (firstMsgDate == null) {
            firstMsgDate = msg.messageDate
        }
        lastMsgDate = msg.messageDate
        usrMsgWordFreq?.insertWordSemiNormalized(msg.messageText)
        addToDateCountMap(usrDateCountMap, msg.messageDate)
        addToDayCountMap(usrDayCountMap, msg.messageDate)
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