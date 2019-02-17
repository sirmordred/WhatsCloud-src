package app.mordred.whatscloud

import com.mordred.wordcloud.WordFrequency
import java.util.*

class User {
    //var usrMsgWordFreq: WordFrequency = WordFrequency()
    var usrMsgCount: Int = 0
    var firstMsgDate: Date? = null
    var lastMsgDate: Date? = null

    fun addMsg(msg: Message) {
        usrMsgCount++
        if (firstMsgDate == null) {
            firstMsgDate = msg.messageDate
        }
        lastMsgDate = msg.messageDate
        //usrMsgWordFreq.insertWord(msg.messageText)
    }
}