package org.teamfairy.sopt.teamkerbell.activities.main.contact


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_contact.view.*

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.main.contact.adapter.ContactListAdapter
import org.teamfairy.sopt.teamkerbell.activities.main.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 * Use the [ContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactFragment : Fragment() ,HasGroupFragment, SwipeRefreshLayout.OnRefreshListener{

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()

    override fun onRefresh() {
        getUserList()
        mSwipeRefreshLayout.isRefreshing = false
    }

    override var group: Team by Delegates.notNull()


    var adapter: ContactListAdapter by Delegates.notNull()
    var dataList: ArrayList<User> = arrayListOf<User>()
    var dataListOrigin: ArrayList<User> = arrayListOf<User>()
    var recyclerView: RecyclerView by Delegates.notNull()

    var txtSearch : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v= inflater!!.inflate(R.layout.fragment_contact, container, false)
        val u = LoginToken.getUser(activity.applicationContext)
        v.tv_user_name.text= u.name
        if(NetworkUtils.getBitmapList(u.photo, v.tv_user_profile,activity.applicationContext, "$INTENT_USER/${u.u_idx}"))
            v.tv_user_profile.setImageResource(R.drawable.icon_profile_default_png)

        recyclerView = v.findViewById(R.id.recyclerView)
        adapter = ContactListAdapter(dataList,activity.applicationContext)
        recyclerView.layoutManager=LinearLayoutManager(activity)
        recyclerView.adapter=adapter


        mSwipeRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)


        v.edt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                txtSearch = p0.toString().trim()
                getUserList()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        return v
    }


    override fun onResume() {
        super.onResume()
        getUserList()
    }
    fun getUserList(){
        DatabaseHelpUtils.getUserListFromRealm(activity.applicationContext,dataListOrigin,adapter as RecyclerView.Adapter<*>,group,true)
        updateUserList()

    }
    fun updateUserList(){
        dataList.clear()
        dataListOrigin.forEach {
            if(it.name!!.contains(txtSearch))
                dataList.add(it)
        }
        adapter.notifyDataSetChanged()
    }

    override fun changeGroup(g: Team) {
        group=g
    }



}// Required empty public constructor
