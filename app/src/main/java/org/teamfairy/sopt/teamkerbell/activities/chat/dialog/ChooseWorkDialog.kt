package org.teamfairy.sopt.teamkerbell.activities.chat.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.Window.FEATURE_NO_TITLE
import android.widget.Button
import org.teamfairy.sopt.teamkerbell.R
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-20.
 */
class ChooseWorkDialog(context: Context?) : Dialog(context) {

    var btnCopy: Button by Delegates.notNull()
    var btnDelete: Button by Delegates.notNull()
    var btnShare: Button by Delegates.notNull()
    var btnSignal: Button by Delegates.notNull()
    var btnNotice: Button by Delegates.notNull()
    var btnPick: Button by Delegates.notNull()
    var btnSearch: Button by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
         window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        setContentView(R.layout.dialog_choose_work)

        btnCopy = findViewById(R.id.btn_copy)
        btnDelete = findViewById(R.id.btn_delete)
        btnShare = findViewById(R.id.btn_share)
        btnSignal = findViewById(R.id.btn_signal)
        btnNotice = findViewById(R.id.btn_notice)
        btnPick = findViewById(R.id.btn_pick)
        btnSearch = findViewById(R.id.btn_search)
    }
    fun setOnClickListener(l: View.OnClickListener) {
        btnCopy.setOnClickListener(l)
        btnDelete.setOnClickListener(l)
        btnShare.setOnClickListener(l)
        btnSignal.setOnClickListener(l)
        btnNotice.setOnClickListener(l)
        btnPick.setOnClickListener(l)
        btnSearch.setOnClickListener(l)
    }
}