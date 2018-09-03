package org.teamfairy.sopt.teamkerbell.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R
import kotlin.properties.Delegates


/**
 * Created by lumiere on 2018-05-20.
 */
class  ConfirmDialog(context: Context, var content: String) : Dialog(context){

    constructor(context: Context) : this(context,context.getString(R.string.txt_unperformed_yet))

    private var btnConfirm : Button by Delegates.notNull()
    private var tvContent : TextView by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_confirm);



        tvContent = findViewById(R.id.dialog_tv_content)
        tvContent.text= content
        btnConfirm=findViewById(R.id.btn_confirm)
        btnConfirm.setOnClickListener {
            this.dismiss()
        }
    }

    fun setOnClickListenerYes(l : View.OnClickListener){
        btnConfirm.setOnClickListener(l)
    }


}