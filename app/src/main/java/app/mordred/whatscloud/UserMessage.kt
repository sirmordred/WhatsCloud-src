package app.mordred.whatscloud

class UserMessage: HashMap<String, MutableList<Message>>() {
    fun add(msg: Message) {
        if (super.containsKey(msg.messageOwner)) {
            // add to usermessage
            super.get(msg.messageOwner)?.add(msg)
        } else {
            // add new usermessagelist
            val tempMsgList: MutableList<Message> = mutableListOf()
            tempMsgList.add(msg)
            super.put(msg.messageOwner, tempMsgList)
        }
    }
}