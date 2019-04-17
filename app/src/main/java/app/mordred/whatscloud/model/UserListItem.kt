package app.mordred.whatscloud.model

import android.graphics.Bitmap
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.PieData

data class UserListItem(val usrName: String, val usrMsgCount: Int, val usrMsgFreq: Int, val usrWordCloud: Bitmap,
                        val usrHrzBarData: BarData, val usrPieData: PieData)