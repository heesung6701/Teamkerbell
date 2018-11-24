package org.teamfairy.sopt.teamkerbell.activities.main.contact


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import io.realm.RealmObject.addChangeListener
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_contact.view.*

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.main.contact.adapter.ContactListAdapter
import org.teamfairy.sopt.teamkerbell.activities.main.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 * Use the [ContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactFragment : Fragment(), HasGroupFragment, SwipeRefreshLayout.OnRefreshListener {

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()

    override fun onRefresh() {
        getUserList()
        mSwipeRefreshLayout.isRefreshing = false
    }

    val TAG = this::class.java.simpleName

    override var group: Team by Delegates.notNull()


    var adapter: ContactListAdapter by Delegates.notNull()
    var dataList: ArrayList<User> = arrayListOf<User>()
    var dataListOrigin: ArrayList<User> = arrayListOf<User>()
    var recyclerView: RecyclerView by Delegates.notNull()

    var txtSearch: String = ""

    private var tvName: TextView by Delegates.notNull()
    private var ivPhoto: ImageView by Delegates.notNull()


    var isUpdateRs: HashMap<Int, IsUpdateR> = HashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_contact, container, false)

        activity?.let { activity ->
            tvName = v.findViewById(R.id.tv_user_name)
            ivPhoto = v.findViewById(R.id.iv_user_profile)


            recyclerView = v.findViewById(R.id.recyclerView)
            adapter = ContactListAdapter(dataList, activity.applicationContext)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = adapter

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
            v.layout_profile.setOnClickListener {
                val intent = Intent(activity.applicationContext, ProfileActivity::class.java)
                startActivity(intent)
            }
//            v.edt_search.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
//                when (actionId) {
//
//                    EditorInfo.IME_ACTION_SEARCH -> {
//                        txtSearch = edt_search.text.toString().trim()
//                        getUserList()
//                    }
//                    else -> {
//                        return@OnEditorActionListener false
//                    }
//                }
//
//                true
//            })

        }


        return v
    }


    override fun onResume() {
        super.onResume()
        getUserList()
        activity?.let { activity->
        setMyProfile(LoginToken.getUser(activity.applicationContext))
        }

        addChangeListener(IsUpdateR.WHAT_USER)
        addChangeListener(IsUpdateR.WHAT_JOINED_GROUP)
    }

    override fun onStop() {
        super.onStop()

        isUpdateRs.forEach {
            it.value.removeAllChangeListeners()
        }
    }

    private fun addChangeListener(what : Int) {
        val LOG_TAG = "$TAG /isUpdateR : User"

        Log.d(LOG_TAG, "add ChangeListener what : ${what}")

        activity?.let { activity ->
            val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
            val isUpdateR = realm.where(IsUpdateR::class.java).equalTo(IsUpdateR.ARG_WHAT, what).findFirst()
                    ?: IsUpdateR.create(realm, what)

            if (isUpdateR.isUpdate) {
                Log.d(LOG_TAG, "was true")
                getUserList()

                realm.beginTransaction()
                isUpdateR.isUpdate = false
                realm.commitTransaction()
                Log.d(LOG_TAG, "become false")
            }

            isUpdateR.addChangeListener<IsUpdateR> { t: IsUpdateR, _ ->
                if (t.isUpdate) {
                    Log.d(LOG_TAG, "is ${t.isUpdate} on addChangeListener")
                    getUserList()
                    realm.beginTransaction()
                    t.isUpdate = false
                    realm.commitTransaction()
                    Log.d(LOG_TAG, "is updated on addChangeListener")
                }
            }
            isUpdateRs[what] = isUpdateR
        }
    }


    private fun setMyProfile(u: User) {
        tvName.text = u.name
        activity?.let { activity->
            if (NetworkUtils.getBitmapList(u.photo, ivPhoto, activity.applicationContext, "user${u.u_idx}"))
                ivPhoto.setImageResource(R.drawable.icon_profile_default)
        }
    }

    fun getUserList() {
        activity?.let {activity->
            DatabaseHelpUtils.getUserListFromRealm(activity.applicationContext, dataListOrigin, adapter as RecyclerView.Adapter<*>, group, true)
        }
        updateUserList()
    }

    private fun updateUserList() {
        dataList.clear()
        dataListOrigin.forEach {
            if (it.name!!.contains(txtSearch))
                dataList.add(it)
        }
        adapter.notifyDataSetChanged()
    }

    override fun changeGroup(g: Team) {
        group = g
    }


}// Required empty public constructor
