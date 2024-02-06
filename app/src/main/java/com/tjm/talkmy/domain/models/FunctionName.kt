package com.tjm.talkmy.domain.models

import android.widget.EditText

sealed class FunctionName {
    class GetTask(val id:String?, val editText: EditText): FunctionName()
    class SaveTask(val editText: EditText): FunctionName()
    class ReadNextTask(val editText: EditText, val play:()->Unit): FunctionName()
}