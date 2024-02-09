package com.tjm.talkmy.ui.taskEdit.managers

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.EditText
import com.tjm.talkmy.domain.models.Sentence
import com.tjm.talkmy.ui.core.extensions.separateSentences
import javax.inject.Inject

 class SentencesManager @Inject constructor() {
     fun getSentences(dirtySentences:String):List<Sentence> {
         val Alltext = dirtySentences
         val auxSentences = Alltext.separateSentences()
         var start = 0
         return auxSentences.map {
             start = Alltext.indexOf(it, start)
             val devolver = Sentence(it, start)
             start += 1
             devolver
         }
     }
}