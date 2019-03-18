package app.mordred.whatscloud.model

import android.graphics.Bitmap

data class UserListItem(val usrName: String, val usrMsgCount: Int, val usrMsgFreq: Float, val usrWordCloud: Bitmap)