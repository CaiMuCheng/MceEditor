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

import io.github.mucheng.mce.annotations.UnsupportedUserUsage
import io.github.mucheng.mce.textmodel.listener.ITextModelListener
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.widget.renderer.EditorRenderer

@Suppress("LeakingThis")
open class MeasureCache(textModel: TextModel, editorRenderer: EditorRenderer) : IMeasureCache,
    ITextModelListener {

    private var textModel: TextModel

    private var editorRenderer: EditorRenderer

    private val cache: MutableList<MeasureCacheRow> = ArrayList()

    init {
        this.textModel = textModel
        this.editorRenderer = editorRenderer
        buildMeasureCache()
        textModel.addListener(this)
    }

    /**
     * This method will measure all rows and cache them.
     * */
    @UnsupportedUserUsage
    override fun buildMeasureCache() {
        synchronized(this::class.java) {
            cache.clear()
            var workLine = 1
            while (workLine <= textModel.lastLine) {
                cache.add(MeasureCacheRow(textModel.getTextRow(workLine), editorRenderer))
                ++workLine
            }
        }
    }

    override fun getMeasureCacheRow(line: Int): MeasureCacheRow? {
        return cache[line - 1]
    }

    override fun getMeasureCache(): List<IMeasureCacheRow> {
        return cache
    }

    override fun setTextModel(textModel: TextModel) {
        synchronized(this::class.java) {
            this.textModel.removeListener(this) // remove old listener
            this.textModel = textModel
            buildMeasureCache()
            textModel.addListener(this) // add new listener
        }
    }

    override fun getTextModel(): TextModel {
        return textModel
    }

    override fun destroy() {
        textModel.removeListener(this)
        cache.clear()
    }

    override fun afterInsert(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        if (startLine == endLine) {
            cache[startLine - 1].afterInsert(startColumn, endColumn, charSequence)
        } else {
            var workLine = startLine + 1
            while (workLine <= endLine) {
                cache.add(
                    workLine - 1,
                    MeasureCacheRow(textModel.getTextRow(workLine), editorRenderer)
                )
                ++workLine
            }

            val startTextRow = textModel.getTextRow(startLine)
            cache[startLine - 1].afterInsert(
                startColumn,
                startTextRow.length,
                startTextRow.subSequence(startColumn, startTextRow.length)
            )
        }
    }

    override fun afterDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        if (startLine == endLine) {
            cache[startLine - 1].afterDelete(startColumn, endColumn)
        } else {
            val insertedCacheRow = cache.removeAt(endLine - 1)
            cache[startLine - 1].append(insertedCacheRow.getMeasureCache())
            val workLine = startLine + 1
            insertedCacheRow.destroy()
            if (workLine < endLine) {
                var modCount = 0
                val size = endLine - workLine
                while (modCount < size) {
                    val element = cache.removeAt(workLine - 1)
                    element.destroy()
                    ++modCount
                }
            }
        }
    }

}