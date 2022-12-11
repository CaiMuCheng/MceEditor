package io.github.mucheng.mce.textmodel.model

import io.github.mucheng.mce.textmodel.annoations.UnsafeApi
import io.github.mucheng.mce.textmodel.exception.IndexOutOfBoundsException
import java.util.*

@Suppress("unused")
open class MutableFloatArray(capacity: Int) {

    companion object Invoker {

        private const val DEFAULT_CAPACITY = 10

        /**
         * Create MutableFloatArray from FloatArray
         * @param floatArray text
         * @return the created MutableFloatArray
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(floatArray: FloatArray): MutableFloatArray {
            val mutableFloatArray = MutableFloatArray(floatArray.size)
            mutableFloatArray.append(floatArray)
            return mutableFloatArray
        }

        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(): MutableFloatArray {
            return MutableFloatArray(DEFAULT_CAPACITY)
        }

    }

    private var value: FloatArray

    val capacity: Int
        get() {
            return value.size
        }

    private var _result: Float

    val result: Float
        get() {
            return _result
        }

    private var _length: Int

    val length: Int
        get() {
            return _length
        }

    init {
        // do init
        this.value = FloatArray(if (capacity < DEFAULT_CAPACITY) DEFAULT_CAPACITY else capacity)
        this._length = 0
        this._result = 0f
    }

    open fun ensureCapacity(capacity: Int) {
        if (value.size < capacity) {
            // copy the value
            val newValue =
                FloatArray(if (value.size * 2 < capacity) capacity + 2 else value.size * 2)
            System.arraycopy(value, 0, newValue, 0, _length)
            value = newValue
        }
    }

    open fun insert(index: Int, float: Float) {
        checkIndex(index, allowEqualsLength = true)
        ensureCapacity(_length + 1)
        if (index < _length) {
            System.arraycopy(value, index, value, 1 + index, _length - index)
        }
        value[index] = float
        _result += float
        ++_length
    }

    open fun append(float: Float) {
        return insert(_length, float)
    }

    open fun insert(
        index: Int,
        floatArray: FloatArray,
        floatArrayStartIndex: Int,
        floatArrayEndIndex: Int
    ) {
        checkIndex(index, allowEqualsLength = true)
        val len = floatArrayEndIndex - floatArrayStartIndex
        ensureCapacity(_length + len)
        System.arraycopy(value, index, value, len + index, _length - index)
        var offset = index
        var charIndex = floatArrayStartIndex
        while (charIndex < len) {
            val float = floatArray[charIndex]
            value[offset++] = float
            _result += float
            ++charIndex
        }
        _length += len
    }

    open fun insert(index: Int, floatArray: FloatArray) {
        return insert(index, floatArray, 0, floatArray.size)
    }

    open fun append(floatArray: FloatArray) {
        return insert(_length, floatArray)
    }

    open fun deleteAt(index: Int) {
        checkIndex(index, allowEqualsLength = false)
        return delete(index, index + 1)
    }

    open fun delete(startIndex: Int, endIndex: Int) {
        checkIndexRange(startIndex, endIndex)
        var workIndex = startIndex
        while (workIndex < endIndex) {
            _result -= value[workIndex]
            ++workIndex
        }
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

    open operator fun get(index: Int): Float {
        checkIndex(index, allowEqualsLength = false)
        return value[index]
    }

    open fun copy(): MutableFloatArray {
        val mutableFloatArray = createMutableFloatArray(_length)
        var index = 0
        while (index < _length) {
            mutableFloatArray.append(value[index])
            ++index
        }
        return mutableFloatArray
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun createMutableFloatArray(capacity: Int): MutableFloatArray {
        return MutableFloatArray(capacity)
    }

    @UnsafeApi
    open fun getUnsafeValue(): FloatArray {
        return this.value
    }

    @UnsafeApi
    open fun setLength(length: Int) {
        this._length = length
    }

    @UnsafeApi
    open fun setResult(result: Float) {
        this._result = result
    }

    open fun clear() {
        this.value = FloatArray(0)
        _length = 0
        _result = 0f
    }

    @Suppress("OPT_IN_USAGE")
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other !is MutableFloatArray) {
            return false
        }
        if (other._result != _result) {
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
     * @param targetIndex 需要检验的索引
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