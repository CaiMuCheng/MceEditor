package io.github.mucheng.mce.textmodel.base

import io.github.mucheng.mce.textmodel.position.CharPosition

interface IIndexer {

    fun getCharPosition(line: Int, column: Int): CharPosition

    fun getCharIndex(line: Int, column: Int): Int

    fun getCharPosition(index: Int): CharPosition

    fun getCharLine(index: Int): Int

    fun getCharColumn(index: Int): Int

}