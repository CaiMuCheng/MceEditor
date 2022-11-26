package io.github.mucheng.mce.textmodel.iterator

import io.github.mucheng.mce.textmodel.model.TextModel
import kotlin.collections.CharIterator

open class CharIterator(open val textModel: TextModel) : CharIterator() {

    private var index: Int = -1

    override fun hasNext(): Boolean {
        return index + 1 < textModel.length
    }

    override fun nextChar(): Char {
        ++index
        return textModel[index]
    }

}