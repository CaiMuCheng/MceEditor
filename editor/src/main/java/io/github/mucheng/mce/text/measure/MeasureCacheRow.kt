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

package io.github.mucheng.mce.text.measure

import io.github.mucheng.mce.annotations.UnsupportedUserUsage
import io.github.mucheng.mce.textmodel.model.TextRow
import io.github.mucheng.mce.widget.renderer.EditorRenderer

class MeasureCacheRow(textRow: TextRow, editorRenderer: EditorRenderer) : IMeasureCacheRow {

    private var textRow: TextRow

    private var editorRenderer: EditorRenderer

    private var cache: FloatArray?

    private val cachedCharWidth: FloatArray

    private var length: Int

    init {
        this.textRow = textRow
        this.editorRenderer = editorRenderer
        this.cache = FloatArray(textRow.length)
        this.cachedCharWidth = FloatArray(1)
        this.length = textRow.length
        buildMeasureCache()
    }

    /**
     * This method will measure all rows and cache them.
     * */
    @UnsupportedUserUsage
    private fun buildMeasureCache() {
        if (cache != null) {
            val codePaint = editorRenderer.getCodePaint()
            codePaint.myGetTextWidths(textRow, 0, length, cache!!)
        }
    }

    override fun getMeasureCacheLength(): Int {
        return this.length
    }

    private fun ensureCapacity(capacity: Int) {
        if (cache != null) {
            if (cache!!.size < capacity) {
                val targetCapacity: Int = if (cache!!.size * 2 < capacity) {
                    capacity + 2
                } else {
                    cache!!.size * 2
                }
                // 进行扩容
                // 复制内容到此数组中, 底层为 System.arraycopy
                cache = cache!!.copyInto(FloatArray(targetCapacity))
            }
        }
    }

    override fun getMeasureCache(): FloatArray {
        return cache ?: FloatArray(0)
    }

    override fun setTextRow(textRow: TextRow) {
        this.textRow = textRow
        length = textRow.length
        ensureCapacity(length)
        buildMeasureCache()
    }

    override fun getTextRow(): TextRow {
        return textRow
    }

    private fun measureChar(charSequence: CharSequence, index: Int, widths: FloatArray): Float {
        val codePaint = editorRenderer.getCodePaint()
        codePaint.getTextWidths(charSequence, index, index + 1, widths)
        return widths[0]
    }

    override fun afterInsert(startIndex: Int, endIndex: Int, charSequence: CharSequence) {
        if (cache != null) {
            val len = endIndex - startIndex
            ensureCapacity(length + len)

            // Use Fast Native method to array copy.
            System.arraycopy(cache!!, startIndex, cache!!, startIndex + len, length - startIndex)
            var workIndex = 0
            var offset = startIndex
            while (workIndex < charSequence.length) {
                cache!![offset++] = measureChar(charSequence, workIndex, cachedCharWidth)
                ++workIndex
            }
            length += len
        }
    }

    override fun afterDelete(startIndex: Int, endIndex: Int) {
        if (cache != null) {
            val len = endIndex - startIndex
            if (len > 0) {
                System.arraycopy(cache!!, startIndex + len, cache!!, startIndex, length - endIndex)
            }
            length -= len
        }
    }

    override fun destroy() {
        cache = null
        cachedCharWidth[0] = 0f
    }

    override fun toString(): String {
        return "MeasureCacheRow(textRow=$textRow, editorRenderer=$editorRenderer, cache=${cache?.contentToString()}, cachedCharWidth=${cachedCharWidth.contentToString()}, length=$length)"
    }

}