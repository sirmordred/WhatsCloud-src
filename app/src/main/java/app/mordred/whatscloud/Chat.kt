package app.mordred.whatscloud

import android.graphics.Bitmap
import com.mordred.wordcloud.WordFrequency

class Chat(chatTitle: String): HashMap<String, User>() {
    var chatTitle: String = ""
    var commonWordFreq = WordFrequency()
    var chatCommonWordCloud: Bitmap? = null
    var chatUserWordCloudList: MutableMap<String, Bitmap> = mutableMapOf()

    init {
        this.chatTitle = chatTitle
    }

    fun add(msg: Message) {
        commonWordFreq.insertWord(msg.messageText) // add msg to common word freq generator
        if (super.containsKey(msg.messageOwner)) {
            // add to usermessage
            super.get(msg.messageOwner)?.addMsg(msg)
        } else {
            // add new usermessagelist
            val tempUser = User()
            tempUser.addMsg(msg)
            super.put(msg.messageOwner, tempUser)
        }
    }

    fun getUserNameList(): MutableList<String> {
        return this.keys.toMutableList()
    }
}