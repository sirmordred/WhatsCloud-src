package app.mordred.whatscloud

import java.util.*

class Message(mDate: Date, mOwner: String, mText: String) {
    var messageText: String = ""
    var messageDate: Date = Date(1102809600000)
    var messageOwner: String = ""


    init {
        messageText = mText
        messageDate = mDate
        messageOwner = mOwner
    }
}