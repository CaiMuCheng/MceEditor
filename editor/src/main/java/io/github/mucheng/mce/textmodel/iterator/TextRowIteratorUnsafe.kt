package io.github.mucheng.mce.textmodel.iterator

import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.annotations.UnsafeApi
import io.github.mucheng.mce.textmodel.model.TextRow

@UnsafeApi
class TextRowIteratorUnsafe(textModel: TextModel) : TextRowIterator(textModel) {

    private var line = 0

    override fun hasNext(): Boolean {
        return line + 1 <= textModel.lastLine
    }

    override fun next(): TextRow {
        ++line
        return textModel.getTextRowUnsafe(line)
    }

}