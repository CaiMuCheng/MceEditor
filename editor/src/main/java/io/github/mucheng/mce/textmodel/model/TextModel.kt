package io.github.mucheng.mce.textmodel.model

import io.github.mucheng.mce.textmodel.base.IIndexer
import io.github.mucheng.mce.textmodel.event.TextModelEvent
import io.github.mucheng.mce.textmodel.exception.IndexOutOfBoundsException
import io.github.mucheng.mce.textmodel.exception.LineOutOfBoundsException
import io.github.mucheng.mce.textmodel.exception.ColumnOutOfBoundsException
import io.github.mucheng.mce.textmodel.indexer.CachedIndexer
import io.github.mucheng.mce.textmodel.iterator.CharIterator
import io.github.mucheng.mce.textmodel.iterator.CharIteratorUnsafe
import io.github.mucheng.mce.textmodel.iterator.TextRowIterator
import io.github.mucheng.mce.textmodel.iterator.TextRowIteratorUnsafe
import io.github.mucheng.mce.annotations.UnsafeApi
import io.github.mucheng.mce.text.ICursor
import io.github.mucheng.mce.text.Cursor
import io.github.mucheng.mce.textmodel.util.CharTable
import io.github.mucheng.mce.textmodel.util.Converter
import java.util.Objects
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Suppress("LeakingThis", "unused")
open class TextModel(
    capacity: Int,
    private var threadSafe: Boolean
) : CharSequence {

    companion object {
        const val DEFAULT_CAPACITY = 50

        @JvmStatic
        private fun equals(a: TextRow, b: TextRow): Boolean {
            if (a.length != b.length) {
                return false
            }
            var index = 0
            while (index < a.length) {
                if (a[index] != b[index]) {
                    return false
                }
                ++index
            }
            return true
        }

    }

    constructor(capacity: Int) : this(capacity, true)

    constructor(threadSafe: Boolean) : this(DEFAULT_CAPACITY, threadSafe)

    constructor() : this(DEFAULT_CAPACITY, true)

    constructor(charSequence: CharSequence) : this(DEFAULT_CAPACITY) {
        appendInternal(charSequence)
    }

    constructor(charSequence: CharSequence, threadSafe: Boolean) : this(
        DEFAULT_CAPACITY,
        threadSafe
    ) {
        appendInternal(charSequence)
    }

    private val value: ArrayList<TextRow>

    private val events: MutableList<TextModelEvent>

    private var _length: Int

    protected var lock: ReadWriteLock?
        private set

    private var indexer: IIndexer

    private var cursor: ICursor

    override val length: Int
        get() {
            return _length
        }

    open val lastIndex: Int
        get() {
            return length - 1
        }

    open val lastLine: Int
        get() {
            return value.size
        }

    open val capacity: Long
        get() {
            return withLock(false) {
                var capacity: Long = 0
                val len = value.size
                var index = 0
                while (index < len) {
                    capacity += value[index].capacity
                    ++index
                }
                capacity
            }
        }

    init {
        value = if (capacity < DEFAULT_CAPACITY) {
            ArrayList(DEFAULT_CAPACITY)
        } else {
            ArrayList(capacity)
        }
        value.add(createTextRow())

        events = ArrayList()
        _length = 0
        lock = if (threadSafe) {
            ReentrantReadWriteLock()
        } else {
            null
        }
        indexer = CachedIndexer(this)
        cursor = Cursor(this)
    }

    /**
     * The text row of creation.
     * */
    protected open fun createTextRow(): TextRow {
        return TextRow()
    }

    open fun setThreadSafe(threadSafe: Boolean) {
        this.threadSafe = threadSafe
        lock = if (threadSafe) {
            ReentrantReadWriteLock()
        } else {
            null
        }
    }

    open fun isThreadSafe(): Boolean {
        return threadSafe
    }

    open fun setIndexer(indexer: IIndexer) {
        this.indexer = indexer
    }

    open fun getIndexer(): IIndexer {
        return indexer
    }

    open fun setCursor(cursor: ICursor) {
        this.cursor = cursor
        this.cursor.set(0) // Move to start
    }

    open fun getCursor(): ICursor {
        return this.cursor
    }

    open fun addEvent(event: TextModelEvent) {
        events.add(event)
    }

    open fun removeEvent(event: TextModelEvent) {
        events.remove(event)
    }

    open fun getTextRow(line: Int): TextRow {
        return withLock(false) {
            checkLine(line)
            getTextRowInternal(line)
        }
    }

    @UnsafeApi
    open fun getTextRowUnsafe(line: Int): TextRow {
        checkLine(line)
        return getTextRowInternal(line)
    }

    private fun getTextRowInternal(line: Int): TextRow {
        return value[Converter.lineToIndex(line)]
    }

    open fun getTextRowSize(line: Int): Int {
        return getTextRow(line).length
    }

    @UnsafeApi
    open fun getTextRowSizeUnsafe(line: Int): Int {
        return getTextRowUnsafe(line).length
    }

    override fun get(index: Int): Char {
        return withLock(false) {
            checkIndex(index, allowEqualsLength = false)
            val position = indexer.getCharPosition(index)
            val line = position.line
            val column = position.column
            if (line < value.size) {
                checkLineColumn(line, column, allowEqualsLength = true)
            } else {
                checkLineColumn(line, column, allowEqualsLength = false)
            }
            getInternal(line, column)
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

    fun get(line: Int, column: Int): Char {
        return withLock(false) {
            if (line < lastLine) {
                checkLineColumn(line, column, true)
            } else {
                checkLineColumn(line, column, allowEqualsLength = false)
            }
            getInternal(line, column)
        }
    }

    @UnsafeApi
    open fun getUnsafe(line: Int, column: Int): Char {
        if (line < lastLine) {
            checkLineColumn(line, column, true)
        } else {
            checkLineColumn(line, column, allowEqualsLength = false)
        }
        return getInternal(line, column)
    }

    private fun getInternal(line: Int, column: Int): Char {
        val textRow = value[Converter.lineToIndex(line)]
        return if (column == textRow.length) {
            CharTable.LF
        } else {
            textRow[column]
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return withLock(false) {
            checkRangeIndex(startIndex, endIndex)
            val startPosition = indexer.getCharPosition(startIndex)
            val endPosition = indexer.getCharPosition(endIndex)
            subSequenceInternal(
                startPosition.line,
                startPosition.column,
                endPosition.line,
                endPosition.column
            )
        }
    }

    @UnsafeApi
    open fun subSequenceUnsafe(startIndex: Int, endIndex: Int): CharSequence {
        checkRangeIndex(startIndex, endIndex)
        val startPosition = indexer.getCharPosition(startIndex)
        val endPosition = indexer.getCharPosition(endIndex)
        return subSequenceInternal(
            startPosition.line,
            startPosition.column,
            endPosition.line,
            endPosition.column
        )
    }

    fun subSequence(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int
    ): CharSequence {
        return withLock(false) {
            checkLine(startLine)
            checkLine(endLine)
            subSequenceInternal(startLine, startColumn, endLine, endColumn)
        }
    }

    @UnsafeApi
    open fun subSequenceUnsafe(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int
    ): CharSequence {
        checkLine(startLine)
        checkLine(endLine)
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

            builder.append(startTextRow.subSequenceAfter(startColumn))
            builder.append(CharTable.LF)

            var workLine = startLine + 1
            while (workLine < endLine) {
                builder.append(getTextRowInternal(workLine))
                builder.append(CharTable.LF)
                ++workLine
            }

            builder.append(endTextRow.subSequenceBefore(endColumn))
        }
        return builder
    }

    fun append(charSequence: CharSequence) {
        withLock(true) {
            appendInternal(charSequence)
        }
    }

    @UnsafeApi
    open fun appendUnsafe(charSequence: CharSequence) {
        appendInternal(charSequence)
    }

    private fun appendInternal(charSequence: CharSequence) {
        val lastLine = lastLine
        insertInternal(lastLine, getTextRowInternal(lastLine).length, charSequence)
    }

    fun insert(index: Int, charSequence: CharSequence) {
        withLock(true) {
            checkIndex(index, allowEqualsLength = true)
            val position = indexer.getCharPosition(index)
            insertInternal(position.line, position.column, charSequence)
        }
    }

    @UnsafeApi
    open fun insertUnsafe(index: Int, charSequence: CharSequence) {
        checkIndex(index, allowEqualsLength = true)
        val position = indexer.getCharPosition(index)
        insertInternal(position.line, position.column, charSequence)
    }

    fun insert(line: Int, column: Int, charSequence: CharSequence) {
        withLock(true) {
            checkLineColumn(line, column, allowEqualsLength = true)
            insertInternal(line, column, charSequence)
        }
    }

    @UnsafeApi
    open fun insertUnsafe(line: Int, column: Int, charSequence: CharSequence) {
        checkLineColumn(line, column, allowEqualsLength = true)
        insertInternal(line, column, charSequence)
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
            if (char == CharTable.CR) {
                if (workIndex + 1 < len && charSequence[workIndex + 1] == CharTable.LF) {
                    val nextTextRow = createTextRow()
                    nextTextRow.append(textRow.subSequenceAfter(workColumn))
                    textRow.deleteAfter(workColumn)

                    // thisIndex = Converter.lineToIndex(workLine + 1)
                    value.add(workLine, nextTextRow)
                    textRow = nextTextRow
                    ++workLine
                    workColumn = 0

                    // 因为 '\r' 和 '\n' 被算做了一个字符, 所以 --_length
                    --_length
                    // 因为提前向下一个 char 扫描了, 别忘记 ++workIndex
                    ++workIndex
                } else {
                    textRow.insert(workColumn, char)
                    ++workColumn
                }
            } else if (char == CharTable.LF) {
                val nextTextRow = createTextRow()
                nextTextRow.append(textRow.subSequenceAfter(workColumn))
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

    fun delete(startIndex: Int, endIndex: Int) {
        withLock(true) {
            checkRangeIndex(startIndex, endIndex)
            val startPosition = indexer.getCharPosition(startIndex)
            val endPosition = indexer.getCharPosition(endIndex)
            deleteInternal(
                startPosition.line,
                startPosition.column,
                endPosition.line,
                endPosition.column
            )
        }
    }

    @UnsafeApi
    open fun deleteUnsafe(startIndex: Int, endIndex: Int) {
        checkRangeIndex(startIndex, endIndex)
        val startPosition = indexer.getCharPosition(startIndex)
        val endPosition = indexer.getCharPosition(endIndex)
        deleteInternal(
            startPosition.line,
            startPosition.column,
            endPosition.line,
            endPosition.column
        )
    }

    fun delete(startLine: Int, startColumn: Int, endColumn: Int, endRow: Int) {
        withLock(true) {
            checkRangeLineRow(startLine, startColumn, endColumn, endRow)
            deleteInternal(startLine, startColumn, endColumn, endRow)
        }
    }

    @UnsafeApi
    open fun deleteUnsafe(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
        checkRangeLineRow(startLine, startColumn, endLine, endColumn)
        return deleteInternal(startLine, startColumn, endLine, endColumn)
    }

    private fun deleteInternal(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
        val deleteText = subSequenceInternal(startLine, startColumn, endLine, endColumn)
        dispatchBeforeDelete(startLine, startColumn, endLine, endColumn, deleteText)

        if (startLine == endLine) {
            val textRow = getTextRowInternal(startLine)
            textRow.delete(startColumn, endColumn)
        } else {
            val startTextRow = getTextRow(startLine)
            val endTextRow = getTextRow(endLine)
            val insertedText = endTextRow.subSequenceAfter(endColumn)

            startTextRow.deleteAfter(startColumn)
            endTextRow.deleteBefore(endColumn)

            value.removeAt(Converter.lineToIndex(endLine))
            val workLine = startLine + 1
            if (workLine < endLine) {
                var modCount = 0
                val size = endLine - workLine
                while (modCount < size) {
                    value.removeAt(Converter.lineToIndex(workLine))
                    ++modCount
                }
            }

            startTextRow.append(insertedText)
        }
        _length -= deleteText.length
        dispatchAfterDelete(startLine, startColumn, endLine, endColumn, deleteText)
    }

    fun deleteCharAt(index: Int) {
        withLock(true) {
            checkIndex(index, allowEqualsLength = false)
            val position = indexer.getCharPosition(index)
            deleteCharAtInternal(position.line, position.column)
        }
    }

    @UnsafeApi
    open fun deleteCharAtUnsafe(index: Int) {
        checkIndex(index, allowEqualsLength = false)
        val position = indexer.getCharPosition(index)
        deleteCharAtInternal(position.line, position.column)
    }

    fun deleteCharAt(line: Int, column: Int) {
        withLock(true) {
            if (line < lastLine) {
                checkLineColumn(line, column, allowEqualsLength = true)
            } else {
                checkLineColumn(line, column, allowEqualsLength = false)
            }
            deleteCharAtInternal(line, column)
        }
    }

    @UnsafeApi
    open fun deleteCharAtUnsafe(line: Int, column: Int) {
        if (line < lastLine) {
            checkLineColumn(line, column, allowEqualsLength = true)
        } else {
            checkLineColumn(line, column, allowEqualsLength = false)
        }
        deleteCharAtInternal(line, column)
    }

    private fun deleteCharAtInternal(line: Int, column: Int) {
        val targetTextRow = value[Converter.lineToIndex(line)]
        val deleteText: CharSequence
        if (column < targetTextRow.length) {
            deleteText = targetTextRow[column].toString()
            targetTextRow.deleteCharAt(column)
        } else {
            val nextTextLineModel = value.removeAt(Converter.lineToIndex(line + 1))
            targetTextRow.append(nextTextLineModel)
            deleteText = CharTable.LF.toString()
        }
        --_length
        dispatchAfterDelete(line, column, line, column + 1, deleteText)
    }

    override fun toString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowInternal(workLine))
                    builder.append(CharTable.CONSTANT_NEW_LINE)
                } else {
                    builder.append(getTextRowInternal(workLine))
                }
                ++workLine
            }
            builder.toString()
        }
    }

    open fun toCRString(): String {
        return withLock(false) {
            val builder = StringBuilder(length)
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
            val builder = StringBuilder(length)
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
            val builder = StringBuilder(length)
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
            val builder = StringBuilder(length + 1)
            var workLine = 1
            while (workLine <= lastLine) {
                if (workLine < lastLine) {
                    builder.append(getTextRowInternal(workLine))
                    builder.append(CharTable.CONSTANT_NEW_LINE)
                } else {
                    builder.append(getTextRowInternal(workLine))
                }
                ++workLine
            }
            builder.append(CharTable.NULL)
            builder.toString()
        }
    }

    open fun ensureTextRowListCapacity(minimumCapacity: Int) {
        val len = value.size
        val targetCapacity: Int = if (minimumCapacity <= len) {
            len + DEFAULT_CAPACITY
        } else {
            minimumCapacity
        }
        value.ensureCapacity(targetCapacity)
    }

    open fun clear() {
        withLock(true) {
            value.clear()
            value.add(createTextRow())
            _length = 0
        }
    }

    @UnsafeApi
    open fun clearUnsafe() {
        value.clear()
        value.add(createTextRow())
        _length = 0
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun dispatchBeforeInsert(
        line: Int,
        column: Int,
        charSequence: CharSequence
    ) {
        if (cursor != null && cursor is TextModelEvent) {
            val event = cursor as TextModelEvent
            event.beforeInsert(
                line, column, charSequence
            )
        }

        if (indexer is TextModelEvent) {
            val event = indexer as TextModelEvent
            event.beforeInsert(
                line, column, charSequence
            )
        }

        for (event in events) {
            event.beforeInsert(
                line, column, charSequence
            )
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun dispatchBeforeDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        if (cursor != null && cursor is TextModelEvent) {
            val event = cursor as TextModelEvent
            event.afterDelete(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        if (indexer is TextModelEvent) {
            val event = indexer as TextModelEvent
            event.afterDelete(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        for (event in events) {
            event.afterDelete(startLine, startColumn, endLine, endColumn, charSequence)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun dispatchAfterInsert(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        if (cursor != null && cursor is TextModelEvent) {
            val event = cursor as TextModelEvent
            event.afterInsert(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        if (indexer is TextModelEvent) {
            val event = indexer as TextModelEvent
            event.afterInsert(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        for (event in events) {
            event.afterInsert(startLine, startColumn, endLine, endColumn, charSequence)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun dispatchAfterDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        if (cursor != null && cursor is TextModelEvent) {
            val event = cursor as TextModelEvent
            event.afterDelete(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        if (indexer is TextModelEvent) {
            val event = indexer as TextModelEvent
            event.afterDelete(
                startLine,
                startColumn,
                endLine,
                endColumn,
                charSequence
            )
        }

        for (event in events) {
            event.afterDelete(startLine, startColumn, endLine, endColumn, charSequence)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        if (other is TextModel) {
            if (other.length != length) {
                return false
            }
            var line = 1
            while (line <= lastLine) {
                if (!equals(getTextRowInternal(line), other.getTextRowInternal(line)))
                    ++line
            }
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(value, length)
    }

    /**
     * 给 block 块加锁
     *
     * @param writeLock 加写锁, 否则加读锁
     * @param block 代码块
     * @return T 目标类型
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    protected inline fun <T> withLock(writeLock: Boolean, block: () -> T): T {
        val currentLock = lock ?: return block()
        if (writeLock) currentLock.writeLock().lock() else currentLock.readLock().lock()
        return try {
            block()
        } finally {
            if (writeLock) currentLock.writeLock().unlock() else currentLock.readLock().unlock()
        }
    }

    /**
     * 检验目标索引是否越界
     *
     * @param targetIndex 需要检验的索引
     * @throws IndexOutOfBoundsException
     * */
    @Throws(IndexOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkIndex(targetIndex: Int, allowEqualsLength: Boolean) {
        if (targetIndex < 0) {
            throw IndexOutOfBoundsException(targetIndex)
        }
        if (allowEqualsLength) {
            if (targetIndex > length) {
                throw IndexOutOfBoundsException(targetIndex)
            }
        } else {
            if (targetIndex > lastIndex) {
                throw IndexOutOfBoundsException(targetIndex)
            }
        }
    }

    /**
     * 检验目标区间是否越界
     *
     * @param startIndex 需要检验的起始索引
     * @param endIndex 需要检验的结束索引
     * @throws IndexOutOfBoundsException
     * */
    @Throws(IndexOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkRangeIndex(startIndex: Int, endIndex: Int) {
        checkIndex(startIndex, allowEqualsLength = true)
        checkIndex(endIndex, allowEqualsLength = true)
        if (startIndex > endIndex) {
            throw IndexOutOfBoundsException(endIndex - startIndex)
        }
    }

    /**
     * 检验目标列是否越界
     *
     * @param targetLine 需要检验的列
     * @throws LineOutOfBoundsException
     * */
    @Throws(LineOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkLine(targetLine: Int) {
        if (targetLine < 1) {
            throw LineOutOfBoundsException(targetLine)
        }
        if (targetLine > lastLine) {
            throw LineOutOfBoundsException(targetLine)
        }
    }

    /**
     * 检验目标列行是否越界
     *
     * @param line 需要检验的列
     * @param column 需要检验的行
     * @throws LineOutOfBoundsException
     * @throws ColumnOutOfBoundsException
     * */
    @Throws(ColumnOutOfBoundsException::class)
    fun checkLineColumn(line: Int, column: Int, allowEqualsLength: Boolean) {
        checkLine(line)
        if (column < 0) {
            throw ColumnOutOfBoundsException(column)
        }
        val textRow = value[Converter.lineToIndex(line)]
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
     * 检验目标行列区间是否越界
     *
     * @param startLine 起始列
     * @param startColumn 起始行
     * @param endLine 结束列
     * @param endColumn 结束行
     * @throws LineOutOfBoundsException
     * @throws ColumnOutOfBoundsException
     * */
    @Throws(ColumnOutOfBoundsException::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun checkRangeLineRow(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
        checkLineColumn(startLine, startColumn, allowEqualsLength = true)
        checkLineColumn(endLine, endColumn, allowEqualsLength = true)
        if (startLine > endLine) {
            throw LineOutOfBoundsException(endLine - startLine)
        }
    }

    fun charIterator(): CharIterator {
        return CharIterator(this)
    }

    @UnsafeApi
    open fun charIteratorUnsafe(): CharIterator {
        return CharIteratorUnsafe(this)
    }

    fun textRowIterator(): Iterator<TextRow> {
        return TextRowIterator(this)
    }

    @UnsafeApi
    open fun textRowIteratorUnsafe(): Iterator<TextRow> {
        return TextRowIteratorUnsafe(this)
    }

    open fun <T> useLock(writeLock: Boolean, block: () -> T): T {
        return withLock(writeLock, block = block)
    }

}