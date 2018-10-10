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
class  ConfirmDeleteDialog(context: Context, var content: String) : Dialog(context){

    constructor(context: Context) : this(context,context.getString(R.string.txt_confirm_delete))

    private var btnYes : Button by Delegates.notNull()
    private var btnNo : Button by Delegates.notNull()
    private var tvContent : TextView by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_confirm_delete);

        tvContent = findViewById(R.id.dialog_tv_content)
        tvContent.text= content
        btnYes=findViewById(R.id.btn_confirm_yes)
        btnNo=findViewById(R.id.btn_confirm_no)
        btnNo.setOnClickListener {
            this.dismiss()
        }
    }

    fun setOnClickListenerYes(l : View.OnClickListener){
        btnYes.setOnClickListener(l)
    }


}