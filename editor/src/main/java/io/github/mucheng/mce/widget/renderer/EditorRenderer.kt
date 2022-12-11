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
import io.github.mucheng.mce.measure.exception.MeasureCacheColumnOutBoundsException
import io.github.mucheng.mce.measure.exception.MeasureCacheLineOutOfBoundsException
import io.github.mucheng.mce.util.CachedPaint
import io.github.mucheng.mce.util.ContextUtil
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

    private var whitespaceRadius: Float

    private var tabStrokeWidth: Float

    private var cacheLine: Int

    private var cacheLineWidth: Float

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
            isAntiAlias = true
        }
        this.whitespacePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL_AND_STROKE
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
        this.whitespaceRadius = ContextUtil.dip2px(editor.context, 1f)
        this.tabStrokeWidth = ContextUtil.dip2px(editor.context, 1f)
        this.cacheLine = -1
        this.cacheLineWidth = 0f
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

        this.cacheLine = -1
    }

    fun onDraw(canvas: Canvas) {
        try {
            if (editor.isMeasureCacheBusy()) {
                return drawMeasureCacheBusyPlaceholder(canvas)
            }
            drawLineNumber(canvas)
            drawCode(canvas)
            drawCursor(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
            if (
                e is NullPointerException ||
                e is MeasureCacheColumnOutBoundsException ||
                e is MeasureCacheLineOutOfBoundsException
            ) {
                onDraw(canvas)
            }
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
        if (cacheLine != editor.getLayout().getRowCount()) {
            cacheLine = editor.getLayout().getRowCount()
            cacheLineWidth = lineNumberPaint.measureText(editor.getText().lastLine.toString())
        }
        lineNumberWidth = cacheLineWidth
        lineNumberWidth += spacing * 2
    }

    private fun drawCode(canvas: Canvas) {
        val textModel = editor.getText()
        val layout = editor.getLayout()
        var workLine = editor.getLayout().getStartVisibleRow()
        val targetLine = editor.getLayout().getEndVisibleRow()
        val measureCache = editor.getMeasureCache()
        while (workLine <= targetLine) {
            if (editor.isMeasureCacheBusy()) {
                return
            }
            val measureCacheRow: IMeasureCacheRow =
                measureCache.getMeasureCacheRow(workLine)
            val textRow = textModel.getTextRow(workLine)
            val y = (editor.getRowBaseline(workLine)).toFloat()
            var offsetWidth = lineNumberWidth
            var charOffsetWidth = lineNumberWidth
            val startIndex = layout.getStartVisibleColumn(workLine)
            val endIndex = layout.getEndVisibleColumn(workLine)
            var workIndex = startIndex
            var lastCharStart = workIndex
            var lastCharIsSpace = false
            while (workIndex < endIndex) {
                if (editor.isMeasureCacheBusy()) {
                    return
                }
                if (textRow[workIndex] == '\t') {
                    val tabWidth = if (editor.isMeasureTabUseWhitespace()) {
                        lineNumberPaint.getSpaceWidth()
                    } else {
                        measureCacheRow[workIndex]
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
                    val whitespaceWidth = measureCacheRow[workIndex]
                    val cx = offsetWidth + whitespaceWidth / 2f
                    val cy =
                        (workLine - 1) * editor.getRowHeight() / 2f + workLine * editor.getRowHeight() / 2f
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
                    drawWhitespace(canvas, cx, cy, whitespaceRadius)
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
                offsetWidth += measureCacheRow[workIndex]
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
        tabPaint.strokeWidth = tabStrokeWidth
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
        if (cursor.isSelected() || (cursor.getLeftLine() < workLine || cursor.getLeftLine() > targetLine) || editor.isMeasureCacheBusy()) {
            return
        }

        val measureCache = editor.getMeasureCache()
        val measureCacheRow = measureCache.getMeasureCacheRow(cursor.getLeftLine())
        val len = cursor.getLeftColumn()
        val textRow = editor.getText().getTextRow(cursor.getLeftLine())

        var left = lineNumberWidth
        var index = 0
        while (index < len) {
            if (textRow[index] == '\t') {
                left += if (editor.isMeasureTabUseWhitespace()) {
                    lineNumberPaint.getSpaceWidth() * editor.getTabWidth()
                } else {
                    measureCacheRow[index] * editor.getTabWidth()
                }
                ++index
                continue
            }
            left += measureCacheRow[index]
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