package app.mordred.whatscloud

import com.mordred.wordcloud.WordFrequency

class User {
    //var usrMsgWordFreq: WordFrequency = WordFrequency()
    var usrMsgCount: Int = 0

    fun addMsg(msg: Message) {
        usrMsgCount++
        //usrMsgWordFreq.insertWord(msg.messageText)
    }
}