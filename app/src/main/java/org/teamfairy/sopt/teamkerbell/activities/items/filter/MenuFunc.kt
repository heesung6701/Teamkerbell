package org.teamfairy.sopt.teamkerbell.activities.items.filter

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.MenuActionInterface
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-19.
 */
class MenuFunc(activity: Activity, mode: MENU_OPT) {

    enum class MENU_OPT {
        EDIT_ONLY, DELETE_ONLY, SHOW_ALL
    }

    constructor(activity: Activity) : this(activity, MENU_OPT.SHOW_ALL)

    private var btnMenu: ImageButton by Delegates.notNull()
    private var menuLayout: LinearLayout by Delegates.notNull()
    private var menuEdit: TextView by Delegates.notNull()
    private var menuDelete: TextView by Delegates.notNull()

    private val mActivity: WeakReference<Activity> = WeakReference<Activity>(activity)

    init {
        if (mActivity.get() != null) {
            val activity = mActivity.get()!!
            menuLayout = activity.findViewById(R.id.layout_menu)
            menuEdit = activity.findViewById(R.id.tv_menu_edit)
            menuDelete = activity.findViewById(R.id.tv_menu_delete)

            btnMenu = activity.findViewById(R.id.btn_more)
            btnMenu.visibility = View.VISIBLE

            when (mode) {
                MENU_OPT.DELETE_ONLY -> {
                    btnMenu.setImageDrawable(ContextCompat.getDrawable(activity.applicationContext, R.drawable.ic_delete))
                    btnMenu.setOnClickListener {
                        (activity as MenuActionInterface).menuDelete()
                        closeMenu(activity.applicationContext)
                    }
//                    setDelete(activity)
                }
                MENU_OPT.EDIT_ONLY -> {
                    btnMenu.setImageDrawable(ContextCompat.getDrawable(activity.applicationContext, R.drawable.ic_edit))
                    btnMenu.setOnClickListener {
                        (activity as MenuActionInterface).menuEdit()
                        closeMenu(activity.applicationContext)
                    }
//                    setEdit(activity)
                }
                MENU_OPT.SHOW_ALL -> {
                    setDelete(activity)
                    setEdit(activity)
                    btnMenu.setOnClickListener {
                        if (menuLayout.visibility == View.VISIBLE)
                            closeMenu(activity.applicationContext)
                        else
                            openMenu(activity.applicationContext)
                    }
                }
            }
        }
    }

    private fun setEdit(activity: Activity) {
        menuEdit.visibility = View.VISIBLE
        menuEdit.setOnClickListener {
            (activity as MenuActionInterface).menuEdit()
            closeMenu(activity.applicationContext)
        }
    }
    private fun setDelete(activity: Activity) {
        menuDelete.visibility = View.VISIBLE
        menuDelete.setOnClickListener {
            (activity as MenuActionInterface).menuDelete()
            closeMenu(activity.applicationContext)
        }
    }

    private fun openMenu(applicationContext: Context) {
        btnMenu.setColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColor))
        menuLayout.visibility = View.VISIBLE
    }

    private fun closeMenu(applicationContext: Context) {
        btnMenu.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black))
        menuLayout.visibility = View.GONE
    }
}