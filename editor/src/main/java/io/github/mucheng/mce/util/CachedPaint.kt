/*
 * An experimental code editor library on Android.
 * https://github.com/CaiMuCheng/MceEditor
 * Copyright (c) 2022 CaiMuCheng - All rights reserved
 *
 * This library is free software. You can redistribute it or
 * modify it under the terms of the Mozilla Public
 * License Version 2.0 by the Mozilla.
 *
 * You can use it for commercial purposes, but you must
 * know the copyright's owner is author and mark the copyright
 * with author in your project.
 *
 * Do not without the author, the license, the repository link.
 */

package io.github.mucheng.mce.util

import android.graphics.Typeface
import android.text.TextPaint

@Suppress("unused", "JoinDeclarationAndAssignment")
class CachedPaint : TextPaint() {

    private var emptyWidth: Float

    private var spaceWidth: Float

    private var tabWidth: Float

    private val charCache = FloatArray(1)

    init {
        emptyWidth = measureText("\u0000")
        spaceWidth = measureText(" ")
        tabWidth = measureText("\t")
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateAttributes() {
        spaceWidth = measureText(" ")
        tabWidth = measureText("\t")
    }

    fun setTypefaceWrapper(typeface: Typeface?) {
        super.setTypeface(typeface)
        updateAttributes()
    }

    override fun setTextSize(textSize: Float) {
        super.setTextSize(textSize)
        updateAttributes()
    }

    override fun setFontFeatureSettings(settings: String?) {
        super.setFontFeatureSettings(settings)
        updateAttributes()
    }

    override fun setLetterSpacing(letterSpacing: Float) {
        super.setLetterSpacing(letterSpacing)
        updateAttributes()
    }

    fun getEmptyWidth(): Float {
        return emptyWidth
    }

    fun getSpaceWidth(): Float {
        return spaceWidth
    }

    fun getTabWidth(): Float {
        return tabWidth
    }

    fun myGetTextWidths(text: CharSequence, start: Int, end: Int, widths: FloatArray) {
        var index = start
        while (index < end) {
            when (text[index]) {
                '\u0000' -> {
                    widths[index] = emptyWidth
                }

                ' ' -> {
                    widths[index] = spaceWidth
                }

                '\t' -> {
                    widths[index] = tabWidth
                }

                else -> {
                    super.getTextWidths(text, index, index + 1, charCache)
                    widths[index] = charCache[0]
                }
            }
            ++index
        }
    }

}