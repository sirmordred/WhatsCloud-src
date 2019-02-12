package app.mordred.whatscloud

import android.graphics.Bitmap

class Chat(chatTitle: String) {
    var chatTitle: String = ""
    var chatMsgList: MutableList<Message> = mutableListOf()
    var charUsrMsgMap = UserMessage()
    var chatBmp: Bitmap? = null
    var chatUsrBmpList: MutableMap<String, Bitmap> = mutableMapOf()

    init {
        this.chatTitle = chatTitle
    }

    fun getUserNameList(): MutableList<String> {
        return charUsrMsgMap.keys.toMutableList()
    }

    fun getUserMsgList(): MutableCollection<MutableList<Message>> {
        return charUsrMsgMap.values
    }
}