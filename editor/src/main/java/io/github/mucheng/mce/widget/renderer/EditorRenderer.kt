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
import android.graphics.Paint
import android.graphics.RectF
import io.github.mucheng.mce.measure.IMeasureCacheRow
import io.github.mucheng.mce.util.CachedPaint
import io.github.mucheng.mce.util.DrawUtil
import io.github.mucheng.mce.widget.CodeEditor

@Suppress("OPT_IN_USAGE")
class EditorRenderer(editor: CodeEditor) {


    private val editor: CodeEditor

    private val buildMeasureCacheBusyPaint: CachedPaint

    private val lineNumberPaint: CachedPaint

    private val codePaint: CachedPaint

    private val tabPaint: Paint

    private val whitespacePaint: Paint

    private val cursorPaint: Paint

    private var lineNumberWidth: Float = 0f

    private val tabRectCache: RectF

    private val cursorRectCache: RectF

    init {
        this.editor = editor
        this.buildMeasureCacheBusyPaint = CachedPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        this.lineNumberPaint = CachedPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        this.codePaint = CachedPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        this.tabPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        this.whitespacePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        this.cursorPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        this.tabRectCache = RectF()
        this.cursorRectCache = RectF()
    }

    fun getLineNumberPaint(): CachedPaint {
        return lineNumberPaint
    }

    fun getCodePaint(): CachedPaint {
        return codePaint
    }

    fun update() {
        buildMeasureCacheBusyPaint.textSize = editor.getTextSizePx()
        buildMeasureCacheBusyPaint.typeface = editor.getTypeface()

        lineNumberPaint.textSize = editor.getTextSizePx()
        lineNumberPaint.typeface = editor.getTypeface()
        lineNumberPaint.updateAttributes()

        codePaint.textSize = editor.getTextSizePx()
        codePaint.typeface = editor.getTypeface()
        codePaint.updateAttributes()

        tabPaint.textSize = editor.getTextSizePx()
        tabPaint.typeface = editor.getTypeface()

        whitespacePaint.textSize = editor.getTextSizePx()
        whitespacePaint.typeface = editor.getTypeface()
    }

    fun onDraw(canvas: Canvas) {
        try {
            if (editor.isMeasureCacheBusy()) {
                return drawMeasureCacheBusyPlaceholder(canvas)
            }
            drawLineNumber(canvas)
            drawCode(canvas)
            drawCursor(canvas)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            onDraw(canvas)
        }
    }

    private fun drawMeasureCacheBusyPlaceholder(canvas: Canvas) {
        val text = "Building measure cache...."
        val x = editor.width / 2f - buildMeasureCacheBusyPaint.measureText(text) / 2f
        val y = editor.height / 2f
        canvas.drawText(text, x, y, buildMeasureCacheBusyPaint)
    }

    private fun drawLineNumber(canvas: Canvas) {
        var workLine = editor.getLayout().getStartVisibleRow()
        val targetLine = editor.getLayout().getEndVisibleRow()
        val spacing = editor.getLineNumberSpacingPx()
        while (workLine <= targetLine) {
            val line = workLine.toString()
            val y = editor.getRowBaseline(workLine).toFloat()
            DrawUtil.drawTextRun(
                canvas,
                line,
                0,
                line.length,
                spacing,
                y,
                false,
                lineNumberPaint
            )
            ++workLine
        }
        lineNumberWidth = lineNumberPaint.measureText(editor.getText().lastLine.toString())
        lineNumberWidth += spacing * 2
    }

    private fun drawCode(canvas: Canvas) {
        val textModel = editor.getText()
        val layout = editor.getLayout()
        var workLine = editor.getLayout().getStartVisibleRow()
        val targetLine = editor.getLayout().getEndVisibleRow()
        val measureCache = editor.getMeasureCache()
        while (workLine <= targetLine) {
            val measureCacheRow: IMeasureCacheRow =
                measureCache.getMeasureCacheRow(workLine) ?: return
            val cache = measureCacheRow.getMeasureCache()
            val textRow = textModel.getTextRow(workLine)
            val y = (editor.getRowBaseline(workLine)).toFloat()
            var offsetWidth = lineNumberWidth
            var charOffsetWidth = lineNumberWidth
            val startIndex = layout.getStartVisibleColumn(workLine)
            var workIndex = startIndex
            val endIndex = layout.getEndVisibleColumn(workLine)
            var lastCharIsSpace = false
            var lastCharStart = workIndex
            while (workIndex < endIndex) {
                if (textRow[workIndex] == '\t') {
                    val tabWidth = if (editor.isMeasureTabUseWhitespace()) {
                        lineNumberPaint.getSpaceWidth()
                    } else {
                        cache[workIndex]
                    }
                    val totalTabWidth = tabWidth * editor.getTabWidth()
                    val drawTableY =
                        (workLine - 1) * editor.getRowHeight() + editor.getRowHeight() / 2f
                    val smallTabWidth = tabWidth / 3f
                    if (!lastCharIsSpace) {
                        // draw the code
                        DrawUtil.drawTextRun(
                            canvas,
                            textRow,
                            lastCharStart,
                            workIndex,
                            charOffsetWidth,
                            y,
                            false,
                            codePaint
                        )
                    }
                    drawTab(
                        canvas,
                        offsetWidth + smallTabWidth,
                        offsetWidth + totalTabWidth - smallTabWidth,
                        drawTableY
                    )
                    lastCharIsSpace = true
                    offsetWidth += totalTabWidth
                    ++workIndex
                    continue
                }
                if (textRow[workIndex] == ' ') {
                    val whitespaceWidth = cache[workIndex]
                    val cx = offsetWidth + whitespaceWidth / 2f
                    val cy =
                        (workLine - 1) * editor.getRowHeight() / 2f + workLine * editor.getRowHeight() / 2f
                    val radius = 5f
                    if (!lastCharIsSpace) {
                        // draw the code
                        DrawUtil.drawTextRun(
                            canvas,
                            textRow,
                            lastCharStart,
                            workIndex,
                            charOffsetWidth,
                            y,
                            false,
                            codePaint
                        )
                    }
                    drawWhitespace(canvas, cx, cy, radius)
                    lastCharIsSpace = true
                    offsetWidth += whitespaceWidth
                    ++workIndex
                    continue
                }

                if (lastCharIsSpace) {
                    lastCharStart = workIndex
                    charOffsetWidth = offsetWidth
                }
                lastCharIsSpace = false
                offsetWidth += cache[workIndex]
                ++workIndex
            }
            if (!lastCharIsSpace && workIndex > startIndex) {
                // draw the code
                DrawUtil.drawTextRun(
                    canvas,
                    textRow,
                    lastCharStart,
                    workIndex,
                    charOffsetWidth,
                    y,
                    false,
                    codePaint
                )
            }
            ++workLine
        }
    }

    private fun drawTab(canvas: Canvas, startX: Float, endX: Float, y: Float) {
        tabRectCache.set(startX, y, endX, y)
        canvas.drawRect(tabRectCache, tabPaint)
    }

    @Suppress("SameParameterValue")
    private fun drawWhitespace(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.drawCircle(
            cx, cy, radius, whitespacePaint
        )
    }

    @Suppress("OPT_IN_USAGE")
    private fun drawCursor(canvas: Canvas) {
        val cursor = editor.getCursor()
        val workLine = editor.getLayout().getStartVisibleRow()
        val targetLine = editor.getLayout().getEndVisibleRow()
        if (cursor.isSelected() || (cursor.getLeftLine() < workLine || cursor.getLeftLine() > targetLine)) {
            return
        }

        val measureCache = editor.getMeasureCache()
        val measureCacheRow = measureCache.getMeasureCacheRow(cursor.getLeftLine()) ?: return
        val cache = measureCacheRow.getMeasureCache()
        val len = cursor.getLeftColumn()
        val textRow = editor.getText().getTextRow(cursor.getLeftLine())

        var left = lineNumberWidth
        var index = 0
        while (index < len) {
            if (textRow[index] == '\t') {
                left += if (editor.isMeasureTabUseWhitespace()) {
                    lineNumberPaint.getSpaceWidth() * editor.getTabWidth()
                } else {
                    cache[index] * editor.getTabWidth()
                }
                ++index
                continue
            }
            left += cache[index]
            ++index
        }

        val top = ((cursor.getLeftLine() - 1) * editor.getRowHeight()).toFloat()
        val bottom = top + editor.getRowHeight()

        cursorRectCache.set(left, top, left, bottom)
        canvas.drawRect(cursorRectCache, cursorPaint)
    }

    fun getLineNumberWidth(): Float {
        return lineNumberWidth
    }

}