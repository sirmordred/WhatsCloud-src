package app.mordred.whatscloud

import com.mordred.wordcloud.WordFrequency

class User {
    var usrMsgWordFreq: WordFrequency = WordFrequency()
    var usrMsgList: MutableList<Message> = mutableListOf()

    fun addMsg(msg: Message) {
        usrMsgList.add(msg)
        usrMsgWordFreq.insertWord(msg.messageText)
    }
}