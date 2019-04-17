package org.teamfairy.sopt.teamkerbell.model.assist

import android.view.View
import android.widget.EditText

/**
 * Created by lumiere on 2018-06-03.
 */
data class ExampleEdit(
    var view: View,
    var edtText: EditText,
    var id: Int?
) {
    constructor(view: View, edtText: EditText) : this(view, edtText, -1)
}