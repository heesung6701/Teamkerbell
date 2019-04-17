package org.teamfairy.sopt.teamkerbell.utils

import android.text.InputFilter
import android.widget.EditText

class EditTextFilter {
    companion object {

        fun setFilter(edt: EditText) {

            val filterKor = InputFilter { source, start, end, dest, dstart, dend ->
                var r: CharSequence? = null
                for (i in start until end) {
                    r = if (!Character.isLetterOrDigit(source[i])) {
                        ""
                    } else
                        null
                }
                r
            }

            edt.filters = arrayOf(filterKor)
        }
    }
}