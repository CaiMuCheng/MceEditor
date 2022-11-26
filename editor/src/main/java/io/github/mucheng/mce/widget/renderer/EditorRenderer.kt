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

package io.github.mucheng.mce.widget.renderer

import android.graphics.Canvas
import android.graphics.Color
import io.github.mucheng.mce.util.CachedPaint
import io.github.mucheng.mce.util.DrawUtil
import io.github.mucheng.mce.widget.CodeEditor

class EditorRenderer(editor: CodeEditor) {

    private val editor: CodeEditor

    private val lineNumberPaint: CachedPaint

    private val codePaint: CachedPaint

    private var lineNumberWidth: Float = 0f

    init {
        this.editor = editor
        this.lineNumberPaint = CachedPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        this.codePaint = CachedPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
    }

    fun getLineNumberPaint(): CachedPaint {
        return lineNumberPaint
    }

    fun getCodePaint(): CachedPaint {
        return codePaint
    }

    fun update() {
        lineNumberPaint.textSize = editor.getTextSizePx()
        lineNumberPaint.typeface = editor.getTypeface()
        codePaint.textSize = editor.getTextSizePx()
        codePaint.typeface = editor.getTypeface()
    }

    fun onDraw(canvas: Canvas) {
        drawLineNumber(canvas)
        drawCode(canvas)
    }

    private fun drawLineNumber(canvas: Canvas) {
        var workLine = editor.getLayout().getStartVisibleRow()
        val targetLine = editor.getLayout().getEndVisibleRow()
        while (workLine <= targetLine) {
            val line = workLine.toString()
            val y = (workLine * editor.getRowHeight()).toFloat()
            DrawUtil.drawTextRun(
                canvas,
                line,
                0,
                line.length,
                0f,
                y,
                false,
                lineNumberPaint
            )
            ++workLine
        }
        lineNumberWidth = lineNumberPaint.measureText(editor.getText().lastLine.toString())
    }

    private fun drawCode(canvas: Canvas) {
        val textModel = editor.getText()
        val layout = editor.getLayout()
        var workLine = editor.getLayout().getStartVisibleRow()
        val targetLine = editor.getLayout().getEndVisibleRow()
        while (workLine <= targetLine) {
            val textRow = textModel.getTextRow(workLine)
            val y = (workLine * editor.getRowHeight()).toFloat()
            DrawUtil.drawTextRun(
                canvas,
                textRow,
                layout.getStartVisibleColumn(workLine),
                layout.getEndVisibleColumn(workLine),
                lineNumberWidth,
                y,
                false,
                lineNumberPaint
            )
            ++workLine
        }
    }

}