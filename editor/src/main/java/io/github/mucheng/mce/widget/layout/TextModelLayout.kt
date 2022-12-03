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

package io.github.mucheng.mce.widget.layout

import io.github.mucheng.mce.widget.CodeEditor
import java.lang.Integer.max
import java.lang.Integer.min

class TextModelLayout(editor: CodeEditor) : Layout {

    private val editor: CodeEditor

    init {
        this.editor = editor
    }

    override fun getLayoutWidth(): Int {
        return editor.width
    }

    override fun getLayoutHeight(): Int {
        return editor.height
    }

    override fun getStartVisibleRow(): Int {
        return max(1, editor.getOffsetY() / editor.getRowHeight())
    }

    override fun getEndVisibleRow(): Int {
        return max(
            1,
            min(
                getRowCount(),
                (editor.getOffsetY() + getLayoutHeight()) / editor.getRowHeight()
            )
        )
    }

    override fun getStartVisibleColumn(line: Int): Int {
        return 0
    }

    override fun getEndVisibleColumn(line: Int): Int {
        val textModel = editor.getText()
        return textModel.getTextRowSize(line)
    }

    override fun getRowCount(): Int {
        return editor.getText().lastLine
    }

    override fun getOffsetLine(offsetY: Float): Int {
        return max(
            1, min(
                (offsetY / editor.getRowHeight()).toInt() + 1,
                editor.getText().lastLine
            )
        )
    }

    @Suppress("OPT_IN_USAGE")
    override fun getOffsetColumn(line: Int, offsetX: Float): Int {
        val measureCacheRow = editor.getMeasureCache().getMeasureCacheRow(line)
        val measureCache = measureCacheRow.getMeasureCache()
        val textRow = editor.getText().getTextRow(line)
        var widths = editor.getEditorRenderer().getLineNumberWidth()
        var workIndex = 0
        while (workIndex < textRow.length) {
            val char = textRow[workIndex]
            widths += if (isTabChar(char)) {
                if (editor.isMeasureTabUseWhitespace()) {
                    editor.getEditorRenderer().getLineNumberPaint()
                        .getSpaceWidth() * editor.getTabWidth()
                } else {
                    measureCache[workIndex] * editor.getTabWidth()
                }
            } else {
                measureCache[workIndex]
            }

            if (widths > offsetX) {
                break
            }
            ++workIndex
        }
        return min(max(0, workIndex), editor.getText().getTextRowSize(line))
    }

    private fun isTabChar(char: Char): Boolean {
        return char == '\t'
    }

    override fun destroy() {}

}