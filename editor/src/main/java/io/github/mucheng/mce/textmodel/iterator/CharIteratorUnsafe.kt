package io.github.mucheng.mce.textmodel.iterator

import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.annotations.UnsafeApi

@UnsafeApi
class CharIteratorUnsafe(textModel: TextModel) : CharIterator(textModel) {

    private var index: Int = -1

    override fun hasNext(): Boolean {
        return index + 1 < textModel.length
    }

    override fun nextChar(): Char {
        ++index
        return textModel.getUnsafe(index)
    }

}