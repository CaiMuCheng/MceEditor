package io.github.mucheng.mce.textmodel.model.android

import android.text.GetChars
import io.github.mucheng.mce.textmodel.model.TextRow

open class AndroidTextRow(capacity: Int) : TextRow(capacity), GetChars {

    companion object Invoker {

        private const val DEFAULT_CAPACITY = 10

        /**
         * Create AndroidTextRow from charSequence
         * @param charSequence text
         * @return the created AndroidTextRow
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(charSequence: CharSequence): AndroidTextRow {
            val textRow = AndroidTextRow(charSequence.length)
            textRow.append(charSequence)
            return textRow
        }

        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(): TextRow {
            return AndroidTextRow(DEFAULT_CAPACITY)
        }

    }

    @Suppress("OPT_IN_USAGE")
    override fun getChars(srcBegin: Int, srcEnd: Int, dst: CharArray?, dstBegin: Int) {
        checkIndexRange(srcBegin, srcEnd)
        if (dst == null) {
            throw NullPointerException("The dest CharArray can not be null.")
        }
        val value = getUnsafeValue()
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin)
    }

    override fun createTextRow(capacity: Int): AndroidTextRow {
        return AndroidTextRow(capacity)
    }

}