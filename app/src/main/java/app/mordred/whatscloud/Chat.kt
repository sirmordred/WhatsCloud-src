package app.mordred.whatscloud

import android.graphics.Bitmap
import app.mordred.whatscloud.view.ResultActivity
import com.mordred.wordcloud.WordFrequency
import java.util.*

class Chat(chatTitle: String, activity: ResultActivity, defStopWordLang: String) {
    var chatTitle: String = ""
    var commonWordFreq = WordFrequency()
    var chatCommonWordCloud: Bitmap? = null
    var userMessageMap: HashMap<String, User> = HashMap()
    var chatFirstMsgDate: Date? = null
    var chatLastMsgDate: Date? = null
    var chatTotalMsgCount: Int = 0
    var activity: ResultActivity? = null
    var defStopWordLang: String? = null
    var chatDateInterval = ""

    init {
        this.chatTitle = chatTitle
        this.activity = activity
        this.defStopWordLang = defStopWordLang

        commonWordFreq.setDefaultStopWords(this.activity, this.defStopWordLang)
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
            userWordFreq.setDefaultStopWords(activity, defStopWordLang)
            userWordFreq.setCustomStopWords(this.activity?.customStopWordList?.toHashSet())
            val tempUser = User(userWordFreq)
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