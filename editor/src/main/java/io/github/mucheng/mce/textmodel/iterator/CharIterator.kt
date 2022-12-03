package io.github.mucheng.mce.textmodel.iterator

import kotlin.collections.CharIterator

open class CharIterator<T : CharSequence>(textRow: T) : CharIterator() {

    private val textRow: T

    private var index: Int

    init {
        this.textRow = textRow
        this.index = -1
    }

    override fun hasNext(): Boolean {
        return index + 1 < textRow.length
    }

    override fun nextChar(): Char {
        return textRow[index++]
    }

}