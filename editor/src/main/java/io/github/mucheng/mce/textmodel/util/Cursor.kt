package io.github.mucheng.mce.textmodel.util

import io.github.mucheng.mce.textmodel.base.ICursor
import io.github.mucheng.mce.textmodel.base.IIndexer
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.position.CharPosition

/**
 * The cursor basic implementation.
 * Copy from https://github.com/Rosemoe/sora-editor
 * @see ICursor
 * */
@Suppress("unused")
open class Cursor(textModel: TextModel) : ICursor {

    private val textModel: TextModel

    private val indexer: IIndexer

    private var leftSelection: CharPosition

    private var rightSelection: CharPosition

    init {
        this.textModel = textModel
        this.indexer = textModel.getIndexer()
        this.leftSelection = CharPosition.createNoIndex()
        this.rightSelection = CharPosition.createNoIndex()
    }

    open fun getTextModel(): TextModel {
        return textModel
    }

    override fun set(line: Int, column: Int) {
        setLeft(line, column)
        setRight(line, column)
    }

    override fun setLeft(line: Int, column: Int) {
        leftSelection.line = line
        leftSelection.column = column
    }

    override fun setRight(line: Int, column: Int) {
        rightSelection.line = line
        rightSelection.column = column
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

    override fun getRight(): CharPosition {
        return rightSelection.copy()
    }

    override fun getRightLine(): Int {
        return rightSelection.line
    }

    override fun getRightColumn(): Int {
        return rightSelection.column
    }

    override fun getIndexer(): IIndexer {
        return indexer
    }

    override fun isSelected(): Boolean {
        return this.leftSelection.index != this.rightSelection.index
    }

    override fun moveToLeft() {
        return moveToLeft(1)
    }

    override fun moveToLeft(count: Int) {
        repeat(count) {
            if (0 < getLeftColumn()) {
                set(getLeftLine(), getLeftColumn() - 1)
            } else if (getLeftLine() - 1 >= 1) {
                set(getLeftLine() - 1, textModel.getTextRowSize(getLeftLine() - 1))
            }
        }
    }

    override fun moveToTop() {
        return moveToTop(1)
    }

    override fun moveToTop(count: Int) {
        repeat(count) {
            if (getLeftLine() > 1 && getLeftColumn() == 0) {
                set(getLeftLine() - 1, getLeftColumn())
            } else if (getLeftLine() > 1) {
                val topTextLineModel = textModel.getTextRow(getLeftLine() - 1)
                var column = getLeftColumn()
                if (getLeftColumn() > topTextLineModel.length) {
                    column = topTextLineModel.length
                }
                set(getLeftLine() - 1, column)
            }
        }
    }

    override fun moveToRight() {
        return moveToRight(1)
    }

    override fun moveToRight(count: Int) {
        repeat(count) {
            val lineCount = textModel.lastLine
            val textLineModel = textModel.getTextRow(getLeftLine())
            if (getLeftColumn() < textLineModel.length) {
                set(getLeftLine(), getLeftColumn() + 1)
            } else if (getLeftLine() + 1 <= lineCount) {
                set(getLeftLine() + 1, 0)
            }
        }
    }

    override fun moveToBottom() {
        return moveToBottom(1)
    }

    override fun moveToBottom(count: Int) {
        repeat(count) {
            val lineCount = textModel.lastLine

            if (getLeftLine() < lineCount && getLeftColumn() == 0) {
                set(getLeftLine() + 1, getLeftColumn())
            } else if (getLeftLine() < lineCount) {
                val bottomTextLineModel = textModel.getTextRow(getLeftLine() + 1)
                var column = getLeftColumn()
                if (column > bottomTextLineModel.length) {
                    column = bottomTextLineModel.length
                }
                set(getLeftLine() + 1, column)
            }
        }
    }

    override fun moveToStart() {
        set(1, 0)
    }

    override fun moveToEnd() {
        set(textModel.lastLine, textModel.getTextRowSize(textModel.lastLine))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ICursor) return false

        if (leftSelection != other.getLeft()) return false
        if (rightSelection != other.getRight()) return false
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