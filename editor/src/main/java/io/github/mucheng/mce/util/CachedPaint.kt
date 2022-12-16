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
import io.github.mucheng.mce.widget.CodeEditor

@Suppress("unused", "JoinDeclarationAndAssignment")
class CachedPaint(editor: CodeEditor) : TextPaint() {

    private val editor: CodeEditor

    private var emptyWidth: Float

    private var spaceWidth: Float

    private var tabWidth: Float

    private val charCache = FloatArray(1)

    init {
        this.editor = editor
        emptyWidth = measureText("\u0000")
        spaceWidth = measureText(" ")
        tabWidth = measureText("\t")
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateAttributes() {
        emptyWidth = measureText("\u0000")
        spaceWidth = measureText(" ")
        tabWidth = measureText("\t")
    }

    fun setTypefaceWrapper(typeface: Typeface?) {
        super.setTypeface(typeface)
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
                    widths[index] = if (editor.isMeasureTabUseWhitespace()) {
                        spaceWidth * editor.getTabWidth()
                    } else {
                        tabWidth * editor.getTabWidth()
                    }
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