package org.teamfairy.sopt.teamkerbell.activities.items.role.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder.FileViewHolder
import java.io.File

/**
 * Created by lumiere on 2018-06-03.
 */
class FileListAdapter(var dataList: ArrayList<File>) : RecyclerView.Adapter<FileViewHolder>() {


    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.tvTitle.text=dataList.get(position).name

        val len =dataList.get(position).length().toFloat()
        var size = ""
        size = when {
            len>1048576 -> String.format("%.2f",len/1048576)+" Mb" //1024*1024 = 1048576
            len>1024 -> String.format("%.2f",len/1024)+" kb"
            else -> len.toString()+" bytes"
        }
        holder.subTitle.text=size

        holder.btnMinus.setOnClickListener {
            dataList.removeAt(position)
            notifyDataSetChanged()
        }
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FileViewHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_file, parent, false)
        return FileViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;

}