package io.github.mucheng.mce.textmodel.indexer

import io.github.mucheng.mce.textmodel.model.TextModel
import io.github.mucheng.mce.textmodel.base.IIndexer
import io.github.mucheng.mce.textmodel.event.TextModelEvent
import io.github.mucheng.mce.textmodel.position.CharPosition
import java.util.Collections
import kotlin.math.abs

@Suppress("unused")
open class CachedIndexer(open val textModel: TextModel) : IIndexer, TextModelEvent {

    companion object {
        const val CACHE_CAPACITY = 100
    }

    private val cache: MutableList<CharPosition> = ArrayList(CACHE_CAPACITY + 1)

    private var doCache: Boolean = true

    private val zeroPosition: CharPosition = CharPosition.createZero()

    private var endPosition: CharPosition = zeroPosition.copy()

    private fun attachEndPos() {
        endPosition.line = textModel.lastLine
        endPosition.column = textModel.getTextRowSize(endPosition.line)
        endPosition.index = textModel.length
    }

    open fun setCacheUse(isEnabled: Boolean) {
        this.doCache = isEnabled
    }

    open fun isCacheUse(): Boolean {
        return doCache
    }

    override fun getCharPosition(line: Int, column: Int): CharPosition {
        textModel.checkLineColumn(line, column, allowEqualsLength = true)
        val position = findNearestPositionByColumn(line).copy()
        return if (position.line == line) {
            if (position.column == column) {
                return position
            }
            findPositionInLine(line, column, position)
        } else if (position.line < line) {
            findPositionByLineColumnForward(line, column, position)
        } else {
            findPositionByLineColumnBackward(line, column, position)
        }
    }

    override fun getCharPosition(index: Int): CharPosition {
        textModel.checkIndex(index, allowEqualsLength = true)
        val position = findNearestPositionByIndex(index).copy()
        return if (position.index == index) {
            position
        } else if (position.index < index) {
            findPositionByIndexForward(index, position)
        } else {
            findPositionByIndexBackward(index, position)
        }
    }

    override fun getCharLine(index: Int): Int {
        return getCharPosition(index).line
    }

    override fun getCharColumn(index: Int): Int {
        return getCharPosition(index).column
    }

    override fun getCharIndex(line: Int, column: Int): Int {
        return getCharPosition(line, column).index
    }

    /**
     * 此函数返回离 column 处最近的 ColumnRowPosition
     *
     * @param column 目标列
     * @return ColumnRowPosition 最近的位置
     * */
    private fun findNearestPositionByColumn(
        column: Int
    ): CharPosition {
        synchronized(CachedIndexer::class.java) {
            var targetDistance = column
            // 从缓存池中查找距离最近的 Position
            var targetPos: CharPosition = zeroPosition
            var targetIndex = 0
            var workIndex = 0
            while (workIndex < cache.size) {
                val pos = cache[workIndex]
                val distance = abs(pos.line - column)
                if (distance < targetDistance) {
                    targetDistance = distance
                    targetPos = pos
                    targetIndex = workIndex
                }
                ++workIndex
            }
            // 如果 targetDistance 离 endPosition 较近, 返回 endPosition
            if (endPosition.line - column < targetDistance) {
                targetPos = endPosition
            }

            // 此位置常用, 交换到最前面
            if (targetPos != zeroPosition && targetPos != endPosition) {
                Collections.swap(cache, 0, targetIndex)
            }
            return targetPos
        }
    }

    /**
     * 此函数将返回 column 列 row 行的位置
     *
     * @param line 目标列
     * @param column 目标行
     * @return ColumnRowPosition 目标位置
     * */
    private fun findPositionInLine(
        line: Int,
        column: Int,
        position: CharPosition
    ): CharPosition {
        val targetPos = CharPosition(line, column, position.index - position.column + column)
        push(targetPos)
        return targetPos.copy()
    }

    /**
     * 此函数通过给定的 (column, row) 向前查找位置
     *
     * @param line 目标列
     * @param column 目标行
     * @param position 最近的位置
     * @return ColumnRowPosition 目标位置
     * */
    private fun findPositionByLineColumnForward(
        line: Int,
        column: Int,
        position: CharPosition
    ): CharPosition {
        var workLine: Int = position.line
        var workColumn: Int = position.index

        //Make index to left of line
        workColumn -= position.column

        while (workLine < line) {
            workColumn += textModel.getTextRowSize(workLine) + 1
            workLine++
        }
        val nearestCharPosition = CharPosition(workLine, 0, workColumn)
        return findPositionInLine(line, column, nearestCharPosition)
    }

    /**
     * 此函数通过给定的 (column, row) 向后回溯查找目标位置
     *
     * @param line 目标列
     * @param column 目标行
     * @param position 最近的位置
     * @return ColumnRowPosition 目标位置
     * */
    private fun findPositionByLineColumnBackward(
        line: Int,
        column: Int,
        position: CharPosition
    ): CharPosition {
        var workLine: Int = position.line
        var workIndex: Int = position.index

        //Make index to the left of line
        workIndex -= position.column

        while (workLine > line) {
            workIndex -= textModel.getTextRowSize(workLine - 1) + 1
            workLine--
        }
        val nearestCharPosition = CharPosition(
            workLine, 0, workIndex
        )
        return findPositionInLine(line, column, nearestCharPosition)
    }

    private fun findNearestPositionByIndex(
        index: Int
    ): CharPosition {
        synchronized(CachedIndexer::class.java) {
            var targetDistance = index
            // 从缓存池中查找距离最近的 Position
            var targetPos: CharPosition = zeroPosition
            var targetIndex = 0
            var workIndex = 0
            while (workIndex < cache.size) {
                val pos = cache[workIndex]
                val distance = abs(pos.index - index)
                if (distance < targetDistance) {
                    targetDistance = distance
                    targetPos = pos
                    targetIndex = workIndex
                }
                ++workIndex
            }
            // 如果 targetDistance 离 endPosition 较近, 返回 endPosition
            if (endPosition.index - index < targetDistance) {
                targetPos = endPosition
            }

            // 此位置常用, 交换到最前面
            if (targetPos != zeroPosition && targetPos != endPosition) {
                Collections.swap(cache, 0, targetIndex)
            }
            return targetPos
        }
    }

    /**
     * 此函数通过给定的 index 向前查找位置
     *
     * @param index 目标索引
     * @param position 最近的位置
     * @return ColumnRowPosition 目标位置
     * */
    private fun findPositionByIndexForward(
        index: Int,
        position: CharPosition
    ): CharPosition {
        var workLine = position.line
        var workColumn = position.column
        var workIndex = position.index

        val column = textModel.getTextRowSize(workLine)
        workIndex += column - workColumn
        workColumn = column

        while (workIndex < index) {
            workLine++
            workColumn = textModel.getTextRowSize(workLine)
            workIndex += workColumn + 1
        }
        if (workIndex > index) {
            workColumn -= workIndex - index
        }

        val neededCharPosition = CharPosition(workLine, workColumn, index)
        push(neededCharPosition)
        return neededCharPosition.copy()
    }

    private fun findPositionByIndexBackward(
        index: Int,
        position: CharPosition
    ): CharPosition {
        var workLine = position.line
        var workColumn = position.column
        var workIndex = position.index
        while (workIndex > index) {
            workIndex -= workColumn + 1
            workLine--
            workColumn = if (workLine != -1) {
                textModel.getTextRowSize(workLine)
            } else {
                return findPositionByIndexForward(index, zeroPosition)
            }
        }
        val nextColumn = index - workIndex
        if (nextColumn > 0) {
            workLine++
            workColumn = nextColumn - 1
        }
        val neededCharPosition = CharPosition(workLine, workColumn, index)
        push(neededCharPosition)
        return neededCharPosition
    }

    open fun clearCache() {
        cache.clear()
    }

    protected open fun push(position: CharPosition) {
        synchronized(CachedIndexer::class.java) {
            if (doCache) {
                cache.add(position)
                while (cache.size > CACHE_CAPACITY) {
                    cache.removeFirst()
                }
            }
        }
    }

    override fun afterInsert(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        synchronized(CachedIndexer::class.java) {
            // 大于等于进行位置偏移
            for (position in cache) {
                if (position.line == startLine) {
                    if (position.column >= startColumn) {
                        position.line += endLine - startLine
                        position.column = endColumn - position.column + startColumn
                        position.index += charSequence.length
                    }
                } else if (position.line > startLine) {
                    position.line += endLine - startLine
                    position.index += charSequence.length
                }
            }
            attachEndPos()
        }
    }

    override fun afterDelete(
        startLine: Int,
        startColumn: Int,
        endLine: Int,
        endColumn: Int,
        charSequence: CharSequence
    ) {
        synchronized(CachedIndexer::class.java) {
            // 如果在区间内则进行删除, 否则进行偏移
            val deletedList: MutableList<CharPosition> = ArrayList()
            for (position in cache) {
                if (position.line == startLine) {
                    if (position.column >= startLine) {
                        deletedList.add(position)
                    }
                } else if (position.line > startLine) {
                    if (position.line <= endLine) {
                        deletedList.add(position)
                    } else {
                        position.line -= endLine - startLine
                        position.index -= charSequence.length
                    }
                }
            }
            cache.removeAll(deletedList)
            attachEndPos()
        }
    }

}