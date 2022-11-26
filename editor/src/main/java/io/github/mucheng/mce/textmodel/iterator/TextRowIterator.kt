package io.github.mucheng.mce.textmodel.iterator

import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.model.TextRow

open class TextRowIterator(open val textModel: TextModel) : Iterator<TextRow> {

    private var line = 0

    override fun hasNext(): Boolean {
        return line + 1 <= textModel.lastLine
    }

    override fun next(): TextRow {
        ++line
        return textModel.getTextRow(line)
    }

}