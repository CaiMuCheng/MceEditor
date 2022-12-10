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

package io.github.mucheng.mce.measure

import io.github.mucheng.mce.textmodel.model.MutableFloatArray
import io.github.mucheng.mce.textmodel.model.TextRow
import io.github.mucheng.mce.widget.renderer.EditorRenderer

class MeasureCacheRow(textRow: TextRow, editorRenderer: EditorRenderer) : IMeasureCacheRow {

    private var textRow: TextRow

    private val editorRenderer: EditorRenderer

    private val mutableFloatArray: MutableFloatArray

    private val length: Int
        get() {
            return mutableFloatArray.length
        }

    init {
        this.textRow = textRow
        this.editorRenderer = editorRenderer
        this.mutableFloatArray = MutableFloatArray(textRow.length)

        buildMeasureCache()
    }

    @Suppress("OPT_IN_USAGE")
    private fun buildMeasureCache() {
        mutableFloatArray.clear()
        mutableFloatArray.ensureCapacity(textRow.length)

        val paint = editorRenderer.getCodePaint()
        paint.myGetTextWidths(textRow, 0, textRow.length, mutableFloatArray.getUnsafeValue())
        mutableFloatArray.setLength(textRow.length)
    }

    @Suppress("OPT_IN_USAGE")
    override fun getMeasureCache(): FloatArray {
        return this.mutableFloatArray.getUnsafeValue()
    }

    override fun getMeasureCacheLength(): Int {
        return length
    }

    override fun append(floatArray: FloatArray) {
        // append the cached array
        this.mutableFloatArray.append(floatArray)
    }

    override fun setTextRow(textRow: TextRow) {
        this.textRow = textRow
        buildMeasureCache()
    }

    override fun getTextRow(): TextRow {
        return this.textRow
    }

    override fun afterInsert(startIndex: Int, endIndex: Int, charSequence: CharSequence) {
        val cache = FloatArray(endIndex - startIndex)
        val paint = editorRenderer.getCodePaint()
        paint.myGetTextWidths(charSequence, 0, charSequence.length, cache)
        mutableFloatArray.insert(startIndex, cache)
    }

    override fun afterDelete(startIndex: Int, endIndex: Int) {
        mutableFloatArray.delete(startIndex, endIndex)
    }

    override fun destroy() {
        this.mutableFloatArray.clear()
    }

    override fun toString(): String {
        return "MeasureCacheRow(textRow=$textRow, mutableFloatArray=${getMeasureCache().contentToString()}, length=$length)"
    }


}