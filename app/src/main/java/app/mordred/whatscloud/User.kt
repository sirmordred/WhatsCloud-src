package app.mordred.whatscloud

import com.mordred.wordcloud.WordFrequency
import java.util.*

class User(usrMsgWordFrequency: WordFrequency) {
    var usrMsgWordFreq: WordFrequency? = null
    var usrMsgCount: Int = 0
    var firstMsgDate: Date? = null
    var lastMsgDate: Date? = null

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
    }
}