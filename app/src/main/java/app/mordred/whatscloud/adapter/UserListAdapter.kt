package app.mordred.whatscloud.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.mordred.whatscloud.R
import app.mordred.whatscloud.model.UserListItem
import kotlinx.android.synthetic.main.user_list_item.view.*

class UserListAdapter(val items : ArrayList<UserListItem>, val context: Context) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_list_item, p0, false))
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.tvUserName?.text = items[p1].usrName
        p0.tvUserMsgCount?.text =  "Total Message Count: " + items[p1].usrMsgCount.toString()
        p0.tvUserMsgFreq?.text = "Message Sending Frequency: " + items[p1].usrMsgFreq.toString() + " Msg/Day"
        p0.imgvUserWordcloud?.setImageBitmap(items[p1].usrWordCloud)
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val tvUserName = view.tv_username
        val tvUserMsgCount = view.tv_user_msgcount
        val tvUserMsgFreq = view.tv_user_msgfreq
        val imgvUserWordcloud = view.imgv_user_wordcloud
    }
}