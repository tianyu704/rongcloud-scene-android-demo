/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.utils

import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import com.vdurmont.emoji.EmojiParser

/**
 * @author gusd
 * @Date 2021/07/01
 */
class MaxLengthWithEmojiFilter(val maxLength: Int, val editText: EditText) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val lengthWithEmoji = getLengthWithEmoji(editText.text)
        if (lengthWithEmoji >= maxLength) {
            return ""
        }
        return source ?: ""
    }

    private fun getLengthWithEmoji(source: CharSequence?): Int {
        return source?.let {
            val emojiCount = EmojiParser.extractEmojis(it.toString()).size
            val noEmojiStringLength = EmojiParser.removeAllEmojis(it.toString()).length
            return emojiCount + noEmojiStringLength
        } ?: 0

    }
}