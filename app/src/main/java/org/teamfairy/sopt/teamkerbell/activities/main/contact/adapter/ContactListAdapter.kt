package org.teamfairy.sopt.teamkerbell.activities.main.contact.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.main.contact.viewholder.ContactViewHolder
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER


/**
 * Created by lumiere on 2017-12-30.
 */
class ContactListAdapter(var dataList: ArrayList<User>, var mContext: Context) : RecyclerView.Adapter<ContactViewHolder>() {


    private var onClickListener: View.OnClickListener? = null

    fun setOnItemClickListener(l: View.OnClickListener) {
        onClickListener = l
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val user = dataList[position]

        holder.tvName.text = user.name
        holder.tvEmail.text = user.id
        holder.tvPhone.text = user.phone

        if(NetworkUtils.getBitmapList(user.photo, holder.ivProfile,mContext, "user${user.u_idx}"))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default)

        holder.layoutEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:${user.id}")
            mContext.startActivity(Intent.createChooser(emailIntent, "메일 전송하기"))
        }
        holder.layoutPhone.setOnClickListener {
            val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phone}"))
            mContext.startActivity(i)
        }

        holder.layoutInfo.setOnClickListener {
            if (holder.layoutContactInfo.visibility != View.GONE)
                holder.layoutContactInfo.visibility = View.GONE
            else
                holder.layoutContactInfo.visibility = View.VISIBLE
        }

    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ContactViewHolder {
        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_contact, parent, false)
        mainView.setOnClickListener(onClickListener)
        return ContactViewHolder(mainView)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int = dataList.size;

    abstract class OnLongClickListenerByPosition(var position: Int) : View.OnLongClickListener
}