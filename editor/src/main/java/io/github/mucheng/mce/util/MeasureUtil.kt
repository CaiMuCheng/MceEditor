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

import io.github.mucheng.mce.widget.CodeEditor

class MeasureUtil(editor: CodeEditor) {

    private val editor: CodeEditor

    init {
        this.editor = editor
    }

    @Suppress("OPT_IN_USAGE")
    fun measureChar(
        line: Int,
        column: Int
    ): Float {
        val textRow = editor.getText().getTextRow(line)
        val measureCache = editor.getMeasureCache()
        val measureCacheRow = measureCache.getMeasureCacheRow(line)
        val cache = measureCacheRow.getMeasureCache()
        return if (isTabChar(textRow[column])) {
            if (editor.isMeasureTabUseWhitespace()) {
                editor.getEditorRenderer().getLineNumberPaint()
                    .getSpaceWidth() * editor.getTabWidth()
            } else {
                cache[column] * editor.getTabWidth()
            }
        } else {
            cache[column]
        }
    }

    private fun isTabChar(char: Char): Boolean {
        return char == '\t'
    }

}