package com.tjm.talkmy.domain.models

import android.widget.EditText

sealed class FunctionName {
    class GetTask(val id: String?) : FunctionName()
    class SaveTask(val text: String, val readNextTask: ()->Unit = {}) : FunctionName()
    class ReadNextTask(val editText: EditText, val play: () -> Unit) : FunctionName()
    class ClickParagraph(val function: () -> Unit) : FunctionName()
}