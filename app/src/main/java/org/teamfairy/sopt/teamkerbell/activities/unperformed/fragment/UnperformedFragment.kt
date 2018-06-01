package org.teamfairy.sopt.teamkerbell.activities.unperformed.fragment

import android.content.Context
import android.os.Bundle
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_unperformed.view.*

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.unperformed.adapter.UnperformedListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.RoomInfoInterface
import org.teamfairy.sopt.teamkerbell.utils.Utils
import kotlin.properties.Delegates


class UnperformedFragment : Fragment(),View.OnClickListener {
    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val msg = Message()
        msg.what= type
        msg.obj=dataList[pos]
        onClickData(msg)
    }

    var mListener: OnFragmentInteractionListener? = null

    var type : Int = Utils.TAB_UNPERFORMED_NOTICE
    var tvEmpty : TextView? =null

    private var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: UnperformedListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_unperformed, container, false)


        tvEmpty=v.findViewById(R.id.tv_empty)
        tvEmpty!!.visibility=if(dataList.size==0) View.VISIBLE else View.GONE

        when(type){
            Utils.TAB_UNPERFORMED_NOTICE->{
                v.tv_item_name.text=getString(R.string.action_notice)
                tvEmpty!!.text=("미확인된 ${getString(R.string.action_notice)}가 없습니다.")
            }
            Utils.TAB_UNPERFORMED_SIGNAL->{
                v.tv_item_name.text=getString(R.string.action_signal)
                tvEmpty!!.text=("미확인된 ${getString(R.string.action_signal)}가 없습니다.")
            }
            Utils.TAB_UNPERFORMED_VOTE->{
                v.tv_item_name.text=getString(R.string.action_vote)
                tvEmpty!!.text=("미확인된 ${getString(R.string.action_vote)}가 없습니다.")

            }
        }

        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        adapter = UnperformedListAdapter(dataList)
        adapter?.setOnItemClick(this)
        recyclerView.adapter = adapter




        return v
    }


    fun updateDataList(datas: ArrayList<*>){
        dataList.clear()
        datas.forEach {
            when(type){
                Utils.TAB_UNPERFORMED_NOTICE->{
                    (it as Notice).setGroupInfo(activity.applicationContext)
                    (it as Notice).setPhotoInfo(activity.applicationContext)
                }
                Utils.TAB_UNPERFORMED_SIGNAL->{
                    (it as Signal).setGroupInfo(activity.applicationContext)
                    (it as Signal).setPhotoInfo(activity.applicationContext)
                }
                Utils.TAB_UNPERFORMED_VOTE->{
                    (it as Vote).setGroupInfo(activity.applicationContext)
                    (it as Vote).setPhotoInfo(activity.applicationContext)
                }
            }
            dataList.add(it as ListDataInterface)
        }

        adapter?.notifyDataSetChanged()
        tvEmpty?.visibility=if(dataList.size==0) View.VISIBLE else View.GONE
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener=context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener=null
    }




    private fun onClickData(msg : Message) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(msg)
        }
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(msg : Message)
    }

}
