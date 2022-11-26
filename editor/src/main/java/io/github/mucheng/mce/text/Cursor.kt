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

package io.github.mucheng.mce.text

import io.github.mucheng.mce.textmodel.event.TextModelEvent
import io.github.mucheng.mce.textmodel.indexer.CachedIndexer
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.position.CharPosition

/**
 * The cursor basic implementation.
 * Copy from https://github.com/Rosemoe/sora-editor
 * @see ICursor
 * */
@Suppress("unused")
open class Cursor(textModel: TextModel) : ICursor, TextModelEvent {

    private val textModel: TextModel

    private val indexer: CachedIndexer

    private var leftSelection: CharPosition

    private var rightSelection: CharPosition

    private var cache0: CharPosition? = null

    private var cache1: CharPosition? = null

    private var cache2: CharPosition? = null

    init {
        this.textModel = textModel
        this.indexer = CachedIndexer(textModel)
        this.leftSelection = CharPosition.createZero()
        this.rightSelection = CharPosition.createZero()
    }

    open fun getTextModel(): TextModel {
        return textModel
    }

    override fun set(line: Int, column: Int) {
        setLeft(line, column)
        setRight(line, column)
    }

    override fun set(index: Int) {
        setLeft(index)
        setRight(index)
    }

    override fun setLeft(line: Int, column: Int) {
        leftSelection = indexer.getCharPosition(line, column)
    }

    override fun setLeft(index: Int) {
        leftSelection = indexer.getCharPosition(index)
    }

    override fun setRight(line: Int, column: Int) {
        rightSelection = indexer.getCharPosition(line, column)
    }

    override fun setRight(index: Int) {
        rightSelection = indexer.getCharPosition(index)
    }

    override fun getLeftLine(): Int {
        return leftSelection.line
    }

    override fun getLeft(): CharPosition {
        return leftSelection.copy()
    }

    override fun getLeftColumn(): Int {
        return leftSelection.column
    }

    override fun getLeftIndex(): Int {
        return leftSelection.index
    }

    override fun getRight(): CharPosition {
        return rightSelection.copy()
    }

    override fun getRightLine(): Int {
        return rightSelection.line
    }

    override fun getRightColumn(): Int {
        return rightSelection.column
    }

    override fun getRightIndex(): Int {
        return rightSelection.index
    }

    override fun getIndexer(): CachedIndexer {
        return indexer
    }

    override fun isSelected(): Boolean {
        return this.leftSelection.index != this.rightSelection.index
    }

    override fun beforeInsert(
        line: Int,
        column: Int,
        charSequence: CharSequence
    ) {
        cache0 = indexer.getCharPosition(line, column)
    }

    override fun beforeDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        cache1 = indexer.getCharPosition(startLine, startColumn)
        cache2 = indexer.getCharPosition(endLine, endColumn)
    }

    override fun afterInsert(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        indexer.afterInsert(startLine, startColumn, endLine, endColumn, charSequence)
        if (cache0 != null) {
            val startIndex = cache0!!.index
            if (getLeftIndex() >= startIndex) {
                leftSelection = indexer.getCharPosition(getLeftIndex() + charSequence.length)
            }
            if (getRightIndex() >= startIndex) {
                rightSelection = indexer.getCharPosition(getRightIndex() + charSequence.length)
            }
        }
    }

    override fun afterDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        indexer.afterDelete(startLine, startColumn, endLine, endColumn, charSequence)
        if (cache1 != null && cache2 != null) {
            val startIndex = cache1!!.index
            val endIndex = cache2!!.index
            val leftIndex = getLeftIndex()
            val rightIndex = getRightIndex()
            if (startIndex > rightIndex) {
                return
            }
            if (endIndex <= leftIndex) {
                leftSelection = indexer.getCharPosition(leftIndex - (endIndex - startIndex))
                rightSelection = indexer.getCharPosition(rightIndex - (endIndex - startIndex))
            } else if (endIndex < rightIndex) {
                if (startIndex <= leftIndex) {
                    leftSelection = indexer.getCharPosition(startIndex)
                    rightSelection = indexer.getCharPosition(rightIndex - (endIndex - leftIndex))
                } else {
                    rightSelection = indexer.getCharPosition(rightIndex - (endIndex - startIndex))
                }
            } else {
                if (startIndex <= leftIndex) {
                    leftSelection = indexer.getCharPosition(startIndex)
                    rightSelection = leftSelection.copy()
                } else {
                    rightSelection = indexer.getCharPosition(leftIndex + (rightIndex - startIndex))
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cursor

        if (leftSelection != other.leftSelection) return false
        if (rightSelection != other.rightSelection) return false

        return true
    }

    override fun hashCode(): Int {
        var result = leftSelection.hashCode()
        result = 31 * result + rightSelection.hashCode()
        return result
    }

    override fun toString(): String {
        return "Cursor(leftSelection=$leftSelection, rightSelection=$rightSelection)"
    }

}