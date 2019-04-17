package app.mordred.whatscloud.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import app.mordred.whatscloud.R
import app.mordred.whatscloud.model.UserListItem
import com.github.mikephil.charting.components.XAxis
import kotlinx.android.synthetic.main.user_list_item.view.*

class UserListAdapter(private val items : ArrayList<UserListItem>, private val context: Context)
    :RecyclerView.Adapter<UserListAdapter.ViewHolder>(), Filterable {

    var itemsFiltered: ArrayList<UserListItem>? = null

    init {
        itemsFiltered = items
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_list_item, p0, false))
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.tvUserName?.text = itemsFiltered?.get(p1)?.usrName
        p0.tvUserMsgCount?.text =  "Total Message Count: " + itemsFiltered?.get(p1)?.usrMsgCount.toString()
        p0.tvUserMsgFreq?.text = "Message Sending Frequency: " + itemsFiltered?.get(p1)?.usrMsgFreq.toString() + " Msg/Day"
        p0.imgvUserWordcloud?.setImageBitmap(itemsFiltered?.get(p1)?.usrWordCloud)
        p0.hrzChartUserDateStat.data = itemsFiltered?.get(p1)?.usrHrzBarData
        p0.hrzChartUserDateStat.invalidate()
        p0.pieChartUserDayStat.data = itemsFiltered?.get(p1)?.usrPieData
    }

    override fun getItemCount(): Int {
        return itemsFiltered?.size!!
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName = view.tv_username
        val tvUserMsgCount = view.tv_user_msgcount
        val tvUserMsgFreq = view.tv_user_msgfreq
        val imgvUserWordcloud = view.imgv_user_wordcloud
        val hrzChartUserDateStat = view.usrHrzBarChart
        val pieChartUserDayStat = view.usrPieChart

        init {
            val xAxis = hrzChartUserDateStat.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.isEnabled = true
            xAxis.setDrawAxisLine(false)

            pieChartUserDayStat.setUsePercentValues(true)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                itemsFiltered = if (charString.isEmpty()) {
                    items
                } else {
                    val filteredList = ArrayList<UserListItem>()
                    for (item in items) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (item.usrName.toLowerCase().contains(charString.toLowerCase()) ||
                            item.usrName.contains(charSequence!!)) {
                            filteredList.add(item)
                        }
                    }

                    filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = itemsFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                itemsFiltered = filterResults?.values as ArrayList<UserListItem>
                notifyDataSetChanged()
            }
        }
    }
}