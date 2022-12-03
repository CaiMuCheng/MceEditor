package io.github.mucheng.mce.textmodel.model

import io.github.mucheng.mce.textmodel.annoations.UnsafeApi
import io.github.mucheng.mce.textmodel.base.IIndexer
import io.github.mucheng.mce.textmodel.exception.ColumnOutOfBoundsException
import io.github.mucheng.mce.textmodel.exception.IndexOutOfBoundsException
import io.github.mucheng.mce.textmodel.exception.LineOutOfBoundsException
import io.github.mucheng.mce.textmodel.indexer.CachedIndexer
import io.github.mucheng.mce.textmodel.listener.ITextModelListener
import io.github.mucheng.mce.textmodel.util.CharTable
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Suppress("unused", "OPT_IN_USAGE", "LeakingThis")
open class TextModel(capacity: Int) : CharSequence {

    companion object Invoker {

        private const val DEFAULT_ROW_CAPACITY = 50

        /**
         * Create TextModel from charSequence
         * @param charSequence text
         * @return the created TextModel
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(charSequence: CharSequence): TextModel {
            val textModel = TextModel()
            textModel.append(charSequence)
            return textModel
        }

        /**
         * Create TextModel from list of charSequence
         * @param charSequenceList text of lines
         * @return the created TextModel
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(charSequenceList: List<CharSequence>): TextModel {
            val textModel = TextModel(charSequenceList.size)
            val size = charSequenceList.size
            var index = 0
            while (index < size) {
                textModel.append(charSequenceList[index])
                if (index + 1 < size) {
                    textModel.append(CharTable.LF_STRING)
                }
                ++index
            }
            return textModel
        }

        /**
         * Create an empty TextModel
         * @return the created TextModel
         * */
        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(): TextModel {
            return TextModel(DEFAULT_ROW_CAPACITY)
        }

    }

    private var value: ArrayList<TextRow>

    private var _length: Int

    override val length: Int
        get() {
            return _length
        }

    open val lastLine: Int
        get() {
            return value.size
        }

    open val lastIndex: Int
        get() {
            return _length - 1
        }

    private val listeners: MutableList<ITextModelListener>

    @Suppress("MemberVisibilityCanBePrivate")
    protected var lock: ReadWriteLock?

    private var indexer: IIndexer

    init {
        val targetCapacity = if (capacity < DEFAULT_ROW_CAPACITY) DEFAULT_ROW_CAPACITY else capacity
        this.value = ArrayList(targetCapacity + 1)
        this._length = 0
        this.listeners = LinkedList()
        this.lock = ReentrantReadWriteLock()

        this.indexer = CachedIndexer(this)
        this.value.add(createTextRow(0))
    }

    @Suppress("OPT_IN_USAGE")
    open fun ensureCapacity(capacity: Int) {
        return withLock(write = true) {
            ensureCapacityUnsafe(capacity)
        }
    }

    @UnsafeApi
    open fun ensureCapacityUnsafe(capacity: Int) {
        return ensureCapacity(capacity)
    }

    open fun ensureCapacityInternal(capacity: Int) {
        value.ensureCapacity(capacity)
    }

    open fun addListener(listener: ITextModelListener): Boolean {
        return this.listeners.add(listener)
    }

    open fun removeListener(listener: ITextModelListener): Boolean {
        return this.listeners.remove(listener)
    }

    open fun setThreadSafe(isEnabled: Boolean) {
        if (isEnabled && lock == null) {
            lock = ReentrantReadWriteLock()
            return
        }

        if (!isEnabled) {
            lock = null
        }
    }

    open fun isThreadSafe(): Boolean {
        return lock != null
    }

    open fun setIndexer(indexer: IIndexer) {
        this.indexer = indexer
    }

    open fun getIndexer(): IIndexer {
        return this.indexer
    }

    open fun getTextRow(line: Int): TextRow {
        return withLock(write = false) {
            getTextRowUnsafe(line)
        }
    }

    @UnsafeApi
    open fun getTextRowUnsafe(line: Int): TextRow {
        checkLine(line)
        return getTextRowInternal(line)
    }

    private fun getTextRowInternal(line: Int): TextRow {
        return value[lineToIndex(line)]
    }

    open fun getTextRowSize(line: Int): Int {
        return withLock(write = false) {
            getTextRowSizeUnsafe(line)
        }
    }

    @UnsafeApi
    open fun getTextRowSizeUnsafe(line: Int): Int {
        checkLine(line)
        return getTextRowSizeInternal(line)
    }

    private fun getTextRowSizeInternal(line: Int): Int {
        return getTextRowInternal(line).length
    }

    override fun get(index: Int): Char {
        return withLock(write = false) {
            getUnsafe(index)
        }
    }

    @UnsafeApi
    open fun getUnsafe(index: Int): Char {
        checkIndex(index, allowEqualsLength = false)
        val position = indexer.getCharPosition(index)
        val line = position.line
        val column = position.column
        if (line < value.size) {
            checkLineColumn(line, column, allowEqualsLength = true)
        } else {
            checkLineColumn(line, column, allowEqualsLength = false)
        }
        return getInternal(line, column)
    }

    open fun get(line: Int, column: Int) {
        return withLock(write = false) {
            getUnsafe(line, column)
        }
    }

    open fun getUnsafe(line: Int, column: Int): Char {
        if (line < lastLine) {
            checkLineColumn(line, column, true)
        } else {
            checkLineColumn(line, column, allowEqualsLength = false)
        }
        return getInternal(line, column)
    }

    private fun getInternal(line: Int, column: Int): Char {
        val textRow = value[lineToIndex(line)]
        return if (column == textRow.length) {
            CharTable.LF
        } else {
            textRow[column]
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return withLock(write = false) {
            subSequenceUnsafe(startIndex, endIndex)
        }
    }

    @UnsafeApi
    open fun subSequenceUnsafe(startIndex: Int, endIndex: Int): CharSequence {
        checkIndexRange(startIndex, endIndex)
        val startPosition = indexer.getCharPosition(startIndex)
        val endPosition = indexer.getCharPosition(endIndex)
        return subSequenceInternal(
            startPosition.line,
            startPosition.column,
            endPosition.line,
            endPosition.column
        )
    }

    open fun subSequence(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int
    ): CharSequence {
        return withLock(write = false) {
            subSequenceUnsafe(startLine, startColumn, endLine, endColumn)
        }
    }

    @UnsafeApi
    open fun subSequenceUnsafe(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int
    ): CharSequence {
        checkLineColumnRange(startLine, startColumn, endLine, endColumn)
        return subSequenceInternal(startLine, startColumn, endLine, endColumn)
    }

    private fun subSequenceInternal(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int
    ): CharSequence {
        val builder = StringBuilder()
        if (startLine == endLine) {
            builder.append(getTextRowInternal(startLine).subSequence(startColumn, endColumn))
        } else {
            val startTextRow = getTextRowInternal(startLine)
            val endTextRow = getTextRowInternal(endLine)

            startTextRow.appendToAfter(builder, startColumn)
            builder.append(CharTable.LF)

            var workLine = startLine + 1
            while (workLine < endLine) {
                getTextRowInternal(workLine).appendTo(builder)
                builder.append(CharTable.LF)
                ++workLine
            }

            endTextRow.appendToBefore(builder, endColumn)
        }
        return builder
    }

    open fun insert(index: Int, charSequence: CharSequence) {
        return withLock(write = true) {
            insertUnsafe(index, charSequence)
        }
    }

    @UnsafeApi
    open fun insertUnsafe(index: Int, charSequence: CharSequence) {
        checkIndex(index, allowEqualsLength = true)
        val position = indexer.getCharPosition(index)
        return insertInternal(position.line, position.column, charSequence)
    }

    open fun insert(line: Int, column: Int, charSequence: CharSequence) {
        return insertUnsafe(line, column, charSequence)
    }

    @UnsafeApi
    open fun insertUnsafe(line: Int, column: Int, charSequence: CharSequence) {
        checkLineColumn(line, column, allowEqualsLength = true)
        return insertInternal(line, column, charSequence)
    }

    private fun insertInternal(line: Int, column: Int, charSequence: CharSequence) {
        dispatchBeforeInsert(line, column, charSequence)
        val len = charSequence.length
        var textRow: TextRow = getTextRowInternal(line)
        var workLine = line
        var workColumn = column
        var workIndex = 0
        while (workIndex < len) {
            val char = charSequence[workIndex]

            if (char == CharTable.LF) {
                val text = textRow.subSequenceAfter(workColumn)
                val nextTextRow = createTextRow(text.length)
                nextTextRow.append(text)
                textRow.deleteAfter(workColumn)

                // thisIndex = Converter.lineToIndex(workLine + 1)
                value.add(workLine, nextTextRow)
                textRow = nextTextRow
                ++workLine
                workColumn = 0
            } else {
                textRow.insert(workColumn, char)
                ++workColumn
            }

            ++workIndex
        }
        _length += charSequence.length
        dispatchAfterInsert(line, column, workLine, workColumn, charSequence)
    }

    open fun append(charSequence: CharSequence) {
        return withLock(write = true) {
            appendUnsafe(charSequence)
        }
    }

    @UnsafeApi
    open fun appendUnsafe(charSequence: CharSequence) {
        return appendInternal(charSequence)
    }

    private fun appendInternal(charSequence: CharSequence) {
        return insertInternal(lastLine, getTextRowSizeInternal(lastLine), charSequence)
    }

    open fun delete(startIndex: Int, endIndex: Int) {
        return withLock(write = true) {
            deleteUnsafe(startIndex, endIndex)
        }
    }

    @UnsafeApi
    open fun deleteUnsafe(startIndex: Int, endIndex: Int) {
        checkIndexRange(startIndex, endIndex)
        val startPosition = indexer.getCharPosition(startIndex)
        val endPosition = indexer.getCharPosition(endIndex)
        deleteInternal(
            startPosition.line,
            startPosition.column,
            endPosition.line,
            endPosition.column
        )
    }

    open fun delete(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
        return withLock(write = true) {
            deleteUnsafe(startLine, startColumn, endLine, endColumn)
        }
    }

    @UnsafeApi
    open fun deleteUnsafe(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
        checkLineColumnRange(startLine, startColumn, endLine, endColumn)
        return deleteInternal(startLine, startColumn, endLine, endColumn)
    }

    open fun deleteCharAt(index: Int) {
        return withLock(write = true) {
            deleteCharAtUnsafe(index)
        }
    }

    @UnsafeApi
    open fun deleteCharAtUnsafe(index: Int) {
        checkIndex(index, allowEqualsLength = false)
        val position = indexer.getCharPosition(index)
        return deleteCharAtInternal(position.line, position.column)
    }

    open fun deleteCharAt(line: Int, column: Int) {
        return withLock(write = true) {
            deleteCharAtUnsafe(line, column)
        }
    }

    @UnsafeApi
    open fun deleteCharAtUnsafe(line: Int, column: Int) {
        if (line < lastLine) {
            checkLineColumn(line, column, allowEqualsLength = true)
        } else {
            checkLineColumn(line, column, allowEqualsLength = false)
        }
        return deleteCharAtInternal(line, column)
    }

    private fun deleteCharAtInternal(line: Int, column: Int) {
        return deleteInternal(line, column, line, column + 1)
    }

    private fun deleteInternal(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
        val deleteText = subSequenceInternal(startLine, startColumn, endLine, endColumn)
        dispatchBeforeDelete(startLine, startColumn, endLine, endColumn, deleteText)

        if (startLine == endLine) {
            val textRow = getTextRowInternal(startLine)
            textRow.delete(startColumn, endColumn)
        } else {
            val startTextRow = getTextRowInternal(startLine)
            val endTextRow = getTextRowInternal(endLine)
            val insertedText = endTextRow.subSequenceAfter(endColumn)

            startTextRow.deleteAfter(startColumn)
            endTextRow.deleteBefore(endColumn)

            value.removeAt(lineToIndex(endLine))
            val workLine = startLine + 1
            if (workLine < endLine) {
                var modCount = 0
                val size = endLine - workLine
                while (modCount < size) {
                    value.removeAt(lineToIndex(workLine))
                    ++modCount
                }
            }

            startTextRow.append(insertedText)
        }
        _length -= deleteText.length
        dispatchAfterDelete(startLine, startColumn, endLine, endColumn, deleteText)
    }

    @Suppress("SameParameterValue")
    protected open fun createTextRow(capacity: Int): TextRow {
        return TextRow(capacity)
    }

    override fun toString(): String {
        return withLock(write = false) {
            val builder = StringBuilder(_length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowInternal(workLine))
                    builder.append(CharTable.systemLineSeparator)
                } else {
                    builder.append(getTextRowInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is TextModel) {
            return false
        }
        if (other.length != _length || other.lastLine != lastLine) {
            return false
        }
        if (other === this) {
            return true
        }
        var workLine = 1
        while (workLine < lastLine) {
            val otherTextRow = other.getTextRowInternal(workLine)
            val textRow = getTextRowInternal(workLine)
            if (otherTextRow != textRow) {
                return false
            }
            ++workLine
        }
        return true
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    open fun toCRString(): String {
        return withLock(false) {
            val builder = StringBuilder(_length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowInternal(workLine))
                    builder.append(CharTable.CR)
                } else {
                    builder.append(getTextRowInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    open fun toLFString(): String {
        return withLock(false) {
            val builder = StringBuilder(_length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowInternal(workLine))
                    builder.append(CharTable.LF)
                } else {
                    builder.append(getTextRowInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    open fun toCRLFString(): String {
        return withLock(false) {
            val builder = StringBuilder(_length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowInternal(workLine))
                    builder.append(CharTable.CRLF)
                } else {
                    builder.append(getTextRowInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    open fun toCString() {
        return withLock(false) {
            val builder = StringBuilder(_length + 1)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowInternal(workLine))
                    builder.append(CharTable.systemLineSeparator)
                } else {
                    builder.append(getTextRowInternal(workLine))
                }
                ++workLine
            }
            builder.append(CharTable.NULL)
            builder.toString()
        }
    }

    open fun clear() {
        return withLock(write = true) {
            clearUnsafe()
        }
    }

    open fun clearUnsafe() {
        return clearInternal()
    }

    private fun clearInternal() {
        value.clear()
        value.add(createTextRow(0))
    }

    private inline fun <T> withLock(write: Boolean, block: () -> T): T {
        val currentLock = this.lock ?: return block()
        if (write) currentLock.writeLock().lock() else currentLock.readLock().lock()
        return try {
            block()
        } finally {
            if (write) currentLock.writeLock().unlock() else currentLock.readLock().unlock()
        }
    }

    open fun <T> useLock(write: Boolean, block: () -> T): T {
        return withLock(write, block)
    }

    private fun dispatchBeforeInsert(
        line: Int,
        column: Int,
        charSequence: CharSequence
    ) {
        if (indexer is ITextModelListener) {
            val listener = indexer as ITextModelListener
            listener.beforeInsert(
                line, column, charSequence
            )
        }

        for (listener in listeners) {
            listener.beforeInsert(
                line, column, charSequence
            )
        }
    }

    private fun dispatchBeforeDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        if (indexer is ITextModelListener) {
            val listener = indexer as ITextModelListener
            listener.beforeDelete(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        for (listener in listeners) {
            listener.beforeDelete(startLine, startColumn, endLine, endColumn, charSequence)
        }
    }

    private fun dispatchAfterInsert(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        if (indexer is ITextModelListener) {
            val listener = indexer as ITextModelListener
            listener.afterInsert(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        for (listener in listeners) {
            listener.afterInsert(startLine, startColumn, endLine, endColumn, charSequence)
        }
    }

    private fun dispatchAfterDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        if (indexer is ITextModelListener) {
            val listener = indexer as ITextModelListener
            listener.afterDelete(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        for (listener in listeners) {
            listener.afterDelete(startLine, startColumn, endLine, endColumn, charSequence)
        }
    }

    /**
     * Check the given index out of bounds
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

    /**
     * Check the given index range out of bounds
     *
     * @param startIndex the start index
     * @param endIndex the end index
     * @throws IndexOutOfBoundsException if index range out of bounds
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    fun checkIndexRange(startIndex: Int, endIndex: Int) {
        checkIndex(startIndex, allowEqualsLength = false)
        checkIndex(endIndex, allowEqualsLength = true)

        if (endIndex < startIndex) {
            throw IndexOutOfBoundsException("Start index < end index: ${endIndex - startIndex}")
        }
    }

    /**
     * Check the given line out of bounds
     *
     * @param targetLine the target line
     * @throws LineOutOfBoundsException if line out of bounds
     * */
    @Throws(LineOutOfBoundsException::class)
    @Suppress("MemberVisibilityCanBePrivate")
    fun checkLine(targetLine: Int) {
        if (targetLine < 1) {
            throw LineOutOfBoundsException(targetLine)
        }
        if (targetLine > lastLine) {
            throw LineOutOfBoundsException(targetLine)
        }
    }

    /**
     * Check the given line and column out of bounds
     *
     * @param line the target line
     * @param column the target column of line
     * @throws LineOutOfBoundsException if line out of bounds
     * @throws ColumnOutOfBoundsException if column out of bounds
     * */
    @Suppress("SameParameterValue")
    fun checkLineColumn(line: Int, column: Int, allowEqualsLength: Boolean) {
        checkLine(line)
        if (column < 0) {
            throw ColumnOutOfBoundsException(column)
        }
        val textRow = value[lineToIndex(line)]
        if (allowEqualsLength) {
            if (column > textRow.length) {
                throw ColumnOutOfBoundsException(column)
            }
        } else {
            if (column > textRow.lastIndex) {
                throw ColumnOutOfBoundsException(column)
            }
        }
    }

    /**
     * Check the given line column range out of bounds
     *
     * @param startLine the start line
     * @param startColumn the start column of start line
     * @param endLine the end line
     * @param endColumn the end column of end line
     * @throws LineOutOfBoundsException if line range out of bounds
     * @throws ColumnOutOfBoundsException if column range out of bounds
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    fun checkLineColumnRange(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
        checkLineColumn(startLine, startColumn, allowEqualsLength = true)
        checkLineColumn(endLine, endColumn, allowEqualsLength = true)
        if (startLine > endLine) {
            throw LineOutOfBoundsException("Start line < end line: ${endLine - startLine}")
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun lineToIndex(line: Int): Int {
        return line - 1
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun indexToLine(index: Int): Int {
        return index + 1
    }

}