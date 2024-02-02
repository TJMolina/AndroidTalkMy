package com.tjm.talkmy.ui.core.extensions

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import com.orhanobut.logger.Logger

fun List<String>.spanAll(): SpannableStringBuilder {
    val sentences = this
    val spannableStringBuilder = SpannableStringBuilder()
    sentences.map { sentence->
        val spannableString = SpannableString(sentence)
        spannableString.setSpan({Logger.d("click")}, 0, sentence.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringBuilder.append(spannableString)
    }
    return spannableStringBuilder
}