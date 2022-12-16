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
import kotlin.math.max
import kotlin.math.min

open class TextModelLayout(editor: CodeEditor) : Layout {

    private val editor: CodeEditor

    private var isVisibleRowEnabled: Boolean

    private var isQuick: Boolean

    init {
        this.editor = editor
        this.isVisibleRowEnabled = true
        this.isQuick = true
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
                (editor.getOffsetY() + getLayoutHeight()) / editor.getRowHeight() + 1
            )
        )
    }

    override fun getStartVisibleColumn(line: Int): Int {
        return 0
    }

    @Suppress("OPT_IN_USAGE")
    override fun getEndVisibleColumn(line: Int): Int {
        if (!isVisibleRowEnabled) {
            val textModel = editor.getText()
            return textModel.getTextRowSize(line)
        }
        val textModel = editor.getText()
        val textRow = textModel.getTextRow(line)

        if (textRow.isEmpty() || editor.isMeasureCacheBusy()) {
            return 0
        }

        val measureCacheRow = editor.getMeasureCache().getMeasureCacheRow(line)
        val offsetX = editor.getOffsetX() + editor.width
        var widths = editor.getEditorRenderer().getLineNumberWidth()
        val size = textRow.length
        var index = getStartVisibleColumn(line)

        while (widths < offsetX && index < size) {
            widths += measureCacheRow[index]
            ++index
        }
        index = min(size, index)
        return index
    }

    override fun getRowCount(): Int {
        return editor.getText().lastLine
    }

    @Suppress("OPT_IN_USAGE")
    override fun getMaxOffset(): Float {
        if (isQuick && !editor.isMeasureCacheBusy()) {
            val measureCache = editor.getMeasureCache()
            var offset = 0f
            var workLine = getStartVisibleRow()
            val endLine = getEndVisibleRow()
            while (workLine <= endLine) {
                val measureCacheRow = measureCache.getMeasureCacheRow(workLine)
                offset = max(offset, measureCacheRow.getOffset())
                ++workLine
            }
            return offset
        }
        return editor.getMeasureCache().getMaxOffset()
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
        if (editor.isMeasureCacheBusy()) {
            return 0
        }
        val textRow = editor.getText().getTextRow(line)
        val measureCacheRow = editor.getMeasureCache().getMeasureCacheRow(line)
        var widths = editor.getEditorRenderer().getLineNumberWidth()
        var workIndex = 0
        while (workIndex < textRow.length) {
            widths += measureCacheRow[workIndex]

            if (widths > offsetX) {
                break
            }
            ++workIndex
        }
        return min(max(0, workIndex), editor.getText().getTextRowSize(line))
    }

    override fun destroy() {}

    override fun setVisibleRowEnabled(isVisibleRowEnabled: Boolean) {
        this.isVisibleRowEnabled = isVisibleRowEnabled
    }

    override fun isVisibleRowEnabled(): Boolean {
        return isVisibleRowEnabled
    }

    @Suppress("OPT_IN_USAGE")
    override fun setQuick(isQuick: Boolean) {
        this.isQuick = isQuick
        editor.getMeasureCache().setMaxOffsetEnabled(isQuick)
    }

    override fun isQuick(): Boolean {
        return this.isQuick
    }

}