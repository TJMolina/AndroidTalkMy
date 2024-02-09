package com.tjm.talkmy.domain.models

import android.widget.EditText
import androidx.fragment.app.FragmentActivity

sealed class FunctionName {
    class GetTask(val id: String?) : FunctionName()
    class SaveTask(val text: String, val id:String? = null, val fecha:String? = null) : FunctionName()
    class ReadNextTask(val editText: EditText, val play: () -> Unit) : FunctionName()
    class ClickParagraph(val function:()->Unit):FunctionName()
}