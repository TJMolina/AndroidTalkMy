package com.tjm.talkmy.domain.models

import android.widget.EditText
import androidx.fragment.app.FragmentActivity

sealed class FunctionName {
    class GetTask(val id: String?, val editText: EditText) : FunctionName()
    class SaveTask(val text: String) : FunctionName()
    class ReadNextTask(val editText: EditText, val play: () -> Unit) : FunctionName()
}