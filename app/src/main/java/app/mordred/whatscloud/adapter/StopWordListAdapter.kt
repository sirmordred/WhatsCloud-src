package app.mordred.whatscloud.adapter

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.mordred.whatscloud.R
import kotlinx.android.synthetic.main.stopword_list_item.view.*

class StopWordListAdapter(val items : MutableList<String>?, val context: Context) : RecyclerView.Adapter<StopWordListAdapter.ViewHolder>() {

    private var shPref: SharedPreferences? = null

    init {
        shPref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val viewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.stopword_list_item, p0, false))
        viewHolder.tvDelItem.setOnClickListener {
            // TODO apply also to sharedpreferences as StringSet
            items?.removeAt(viewHolder.adapterPosition)
            notifyDataSetChanged()
        }

        return viewHolder
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.tvAnimalType?.text = items?.get(p1)
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items?.size!!
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val tvAnimalType = view.tv_stopword
        val tvDelItem = view.delBtn
    }
}
