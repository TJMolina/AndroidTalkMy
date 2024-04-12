package com.tjm.talkmy.domain.models

import android.widget.EditText
import com.tjm.talkmy.ui.taskEdit.managers.WebViewManager

sealed class FunctionName {
    class GetTask(val id: String?) : FunctionName()
    class SaveTask(val text: String, val readNextTask: ()->Unit = {}) : FunctionName()
    class ReadNextTask(val editText: WebViewManager, val play: () -> Unit) : FunctionName()
    class ClickParagraph(val function: () -> Unit) : FunctionName()
}