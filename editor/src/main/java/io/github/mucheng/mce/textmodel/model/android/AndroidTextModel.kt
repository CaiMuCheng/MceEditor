package io.github.mucheng.mce.textmodel.model.android

import io.github.mucheng.mce.annotations.UnsafeApi
import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.model.TextRow

@Suppress("unused")
open class AndroidTextModel(capacity: Int, threadSafe: Boolean) : TextModel(capacity, threadSafe) {

    constructor(capacity: Int) : this(capacity, true)

    constructor(threadSafe: Boolean) : this(DEFAULT_CAPACITY, threadSafe)

    constructor() : this(DEFAULT_CAPACITY, true)

    @Suppress("OPT_IN_USAGE", "LeakingThis")
    constructor(charSequence: CharSequence) : this(DEFAULT_CAPACITY) {
        appendUnsafe(charSequence)
    }

    @Suppress("OPT_IN_USAGE", "LeakingThis")
    constructor(charSequence: CharSequence, threadSafe: Boolean) : this(
        DEFAULT_CAPACITY,
        threadSafe
    ) {
        appendUnsafe(charSequence)
    }

    override fun createTextRow(): TextRow {
        return AndroidTextRow()
    }

    override fun getTextRow(line: Int): AndroidTextRow {
        return super.getTextRow(line) as AndroidTextRow
    }

    @UnsafeApi
    override fun getTextRowUnsafe(line: Int): AndroidTextRow {
        return super.getTextRowUnsafe(line) as AndroidTextRow
    }

}