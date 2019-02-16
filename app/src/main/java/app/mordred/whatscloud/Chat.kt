package app.mordred.whatscloud

import android.graphics.Bitmap
import com.mordred.wordcloud.WordFrequency

class Chat(chatTitle: String) {
    var chatTitle: String = ""
    var commonWordFreq = WordFrequency()
    var chatCommonWordCloud: Bitmap? = null
    var chatUserWordCloudList: MutableMap<String, Bitmap> = mutableMapOf()
    var userMessageMap: HashMap<String, User> = HashMap()

    init {
        this.chatTitle = chatTitle
    }

    fun add(msg: Message) {
        commonWordFreq.insertWordNonNormalized(msg.messageText) // add msg to common word freq generator
        val usr = userMessageMap[msg.messageOwner]
        if (usr != null) {
            // add to usermessage
            usr.addMsg(msg)
        } else {
            // add new usermessagelist
            val tempUser = User()
            tempUser.addMsg(msg)
            userMessageMap[msg.messageOwner] = tempUser
        }
    }

    fun getUserNameList(): MutableList<String> {
        return userMessageMap.keys.toMutableList()
    }

    fun getUserSize(): Int {
        return userMessageMap.size
    }
}