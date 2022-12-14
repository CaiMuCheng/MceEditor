package io.github.mucheng.mce.textmodel.model

import io.github.mucheng.mce.textmodel.annoations.UnsafeApi
import io.github.mucheng.mce.textmodel.exception.IndexOutOfBoundsException
import io.github.mucheng.mce.textmodel.iterator.CharIterator
import java.util.*

@Suppress("unused")
open class TextRow(capacity: Int) : CharSequence {

    companion object Invoker {

        private const val DEFAULT_CAPACITY = 10

        /**
         * Create TextRow from charSequence
         * @param charSequence text
         * @return the created TextRow
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(charSequence: CharSequence): TextRow {
            val textRow = TextRow(charSequence.length)
            textRow.append(charSequence)
            return textRow
        }

        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(): TextRow {
            return TextRow(DEFAULT_CAPACITY)
        }

    }

    private var value: CharArray

    val capacity: Int
        get() {
            return value.size
        }

    private var _length: Int

    override val length: Int
        get() {
            return _length
        }

    init {
        // do init
        this.value = CharArray(if (capacity < DEFAULT_CAPACITY) DEFAULT_CAPACITY else capacity)
        this._length = 0
    }

    open fun ensureCapacity(capacity: Int) {
        if (value.size < capacity) {
            // copy the value
            val newValue = CharArray(if (value.size * 2 < capacity) capacity + 2 else value.size * 2)
            System.arraycopy(value, 0, newValue, 0, _length)
            value = newValue
        }
    }

    open fun insert(index: Int, char: Char) {
        checkIndex(index, allowEqualsLength = true)
        ensureCapacity(_length + 1)
        if (index < _length) {
            System.arraycopy(value, index, value, 1 + index, _length - index)
        }
        value[index] = char
        ++_length
    }

    open fun append(char: Char) {
        return insert(_length, char)
    }

    open fun insert(
        index: Int,
        charSequence: CharSequence,
        charSequenceStartIndex: Int,
        charSequenceEndIndex: Int
    ) {
        checkIndex(index, allowEqualsLength = true)
        val len = charSequenceEndIndex - charSequenceStartIndex
        ensureCapacity(_length + len)
        System.arraycopy(value, index, value, len + index, _length - index)
        var offset = index
        var charIndex = charSequenceStartIndex
        while (charIndex < len) {
            val char = charSequence[charIndex]
            value[offset++] = char
            ++charIndex
        }
        _length += len
    }

    open fun insert(index: Int, charSequence: CharSequence) {
        return insert(index, charSequence, 0, charSequence.length)
    }

    open fun append(charSequence: CharSequence) {
        return insert(_length, charSequence)
    }

    open fun deleteCharAt(index: Int) {
        checkIndex(index, allowEqualsLength = false)
        return delete(index, index + 1)
    }

    open fun delete(startIndex: Int, endIndex: Int) {
        checkIndexRange(startIndex, endIndex)
        val len = endIndex - startIndex
        System.arraycopy(value, len + startIndex, value, startIndex, _length - endIndex)
        _length -= len
    }

    open fun deleteBefore(index: Int) {
        checkIndex(index, allowEqualsLength = true)
        return delete(0, index)
    }

    open fun deleteAfter(index: Int) {
        checkIndex(index, allowEqualsLength = true)
        return delete(index, _length)
    }

    override fun get(index: Int): Char {
        checkIndex(index, allowEqualsLength = false)
        return value[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        checkIndexRange(startIndex, endIndex)
        val len = endIndex - startIndex
        val textRow = TextRow(len)
        var index = startIndex
        while (index < endIndex) {
            textRow.append(value[index])
            ++index
        }
        return textRow
    }

    open fun subSequenceBefore(index: Int): CharSequence {
        return subSequence(0, index)
    }

    open fun subSequenceAfter(index: Int): CharSequence {
        return subSequence(index, _length)
    }

    open fun appendTo(stringBuilder: StringBuilder, fromIndex: Int, toIndex: Int) {
        checkIndexRange(fromIndex, toIndex)
        stringBuilder.ensureCapacity(stringBuilder.capacity() + _length)
        var index = fromIndex
        while (index < toIndex) {
            stringBuilder.append(value[index])
            ++index
        }
    }

    open fun appendToBefore(stringBuilder: StringBuilder, index: Int) {
        return appendTo(stringBuilder, 0, index)
    }

    open fun appendToAfter(stringBuilder: StringBuilder, index: Int) {
        return appendTo(stringBuilder, index, _length)
    }

    open fun appendTo(stringBuilder: StringBuilder) {
        return appendTo(stringBuilder, 0, _length)
    }

    open fun copy(): TextRow {
        val textRow = createTextRow(_length)
        var index = 0
        while (index < _length) {
            textRow.append(value[index])
            ++index
        }
        return textRow
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun createTextRow(capacity: Int): TextRow {
        return TextRow(capacity)
    }

    open fun charIterator(): CharIterator<TextRow> {
        return CharIterator(this)
    }

    @UnsafeApi
    open fun getUnsafeValue(): CharArray {
        return this.value
    }

    open fun clear() {
        this.value = CharArray(0)
        _length = 0
    }

    override fun toString(): String {
        return String(value, 0, _length)
    }

    @Suppress("OPT_IN_USAGE")
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other !is TextRow) {
            return false
        }
        return value.contentEquals(other.getUnsafeValue())
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    /**
     * Check the given index out of bounds.
     *
     * @param targetIndex ?????????????????????
     * @throws IndexOutOfBoundsException if index out of bounds
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    fun checkIndex(targetIndex: Int, allowEqualsLength: Boolean) {
        if (targetIndex < 0) {
            throw IndexOutOfBoundsException(targetIndex)
        }

        if (allowEqualsLength) {
            if (targetIndex > _length) {
                throw IndexOutOfBoundsException(targetIndex)
            }
        } else {
            if (targetIndex >= _length) {
                throw IndexOutOfBoundsException(targetIndex)
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun checkIndexRange(startIndex: Int, endIndex: Int) {
        checkIndex(startIndex, allowEqualsLength = true)
        checkIndex(endIndex, allowEqualsLength = true)
        if (endIndex < startIndex) {
            throw IndexOutOfBoundsException("Start index < end index: ${endIndex - startIndex}")
        }
    }

}