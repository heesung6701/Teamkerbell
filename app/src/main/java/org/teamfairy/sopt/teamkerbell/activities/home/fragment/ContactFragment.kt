package org.teamfairy.sopt.teamkerbell.activities.home.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.home.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.activities.home.interfaces.HasGroupFragment.Companion.ARG_GROUP
import org.teamfairy.sopt.teamkerbell.model.data.Team
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 * Use the [ContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactFragment : Fragment() ,HasGroupFragment{

    override var group: Team by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        group = arguments.getParcelable(ARG_GROUP)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_contact, container, false)
    }


    override fun changeGroup(g: Team) {
        group=g
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ContactFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(group : Team): ContactFragment {
            val fragment = ContactFragment()
            val args = Bundle()
            args.putParcelable(ARG_GROUP, group)
            fragment.arguments = args
            return fragment
        }
    }


}// Required empty public constructor
