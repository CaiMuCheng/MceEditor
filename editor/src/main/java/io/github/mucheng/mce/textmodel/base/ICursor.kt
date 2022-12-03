package io.github.mucheng.mce.textmodel.base

import io.github.mucheng.mce.textmodel.position.CharPosition

/**
 * The cursor interface.
 * */
interface ICursor {

    fun set(line: Int, column: Int)

    fun setLeft(line: Int, column: Int)

    fun setRight(line: Int, column: Int)

    fun getLeftLine(): Int

    fun getLeft(): CharPosition

    fun getLeftColumn(): Int

    fun getRight(): CharPosition

    fun getRightLine(): Int

    fun getRightColumn(): Int

    fun getIndexer(): IIndexer

    fun isSelected(): Boolean

    fun moveToLeft()

    fun moveToLeft(count: Int)

    fun moveToTop()

    fun moveToTop(count: Int)

    fun moveToRight()

    fun moveToRight(count: Int)

    fun moveToBottom()

    fun moveToBottom(count: Int)

    fun moveToStart()

    fun moveToEnd()

}