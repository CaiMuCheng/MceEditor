package io.github.mucheng.mce.textmodel.model.android

import android.text.GetChars
import io.github.mucheng.mce.textmodel.model.TextRow

@Suppress("unused")
class AndroidTextRow(capacity: Int) : TextRow(capacity), GetChars {

    constructor() : this(DEFAULT_CAPACITY)

    constructor(charSequence: CharSequence) : this(charSequence.length) {
        append(charSequence)
    }

    @Suppress("OPT_IN_USAGE")
    override fun getChars(srcBegin: Int, srcEnd: Int, dst: CharArray?, dstBegin: Int) {
        checkRangeIndex(srcBegin, srcEnd)
        if (dst == null) {
            throw NullPointerException("The dest CharArray can not be null.")
        }
        val value = getUnsafeValue()
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin)
    }


}