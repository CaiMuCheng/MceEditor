/*
 * An experimental code editor library on Android.
 * https://github.com/CaiMuCheng/MceEditor
 * Copyright (c) 2022 CaiMuCheng - All rights reserved
 *
 * This library is free software. You can redistribute it or
 * modify it under the terms of the Mozilla Public
 * License Version 2.0 by the Mozilla.
 *
 * You can use it for commercial purposes, but you must
 * know the copyright's owner is author and mark the copyright
 * with author in your project.
 *
 * Do not without the author, the license, the repository link.
 */

package io.github.mucheng.mce.text

import io.github.mucheng.mce.textmodel.base.IIndexer
import io.github.mucheng.mce.textmodel.position.CharPosition

/**
 * The cursor interface.
 * */
interface ICursor {

    fun set(line: Int, column: Int)

    fun set(index: Int)

    fun setLeft(line: Int, column: Int)

    fun setLeft(index: Int)

    fun setRight(line: Int, column: Int)

    fun setRight(index: Int)

    fun getLeftLine(): Int

    fun getLeft(): CharPosition

    fun getLeftColumn(): Int

    fun getLeftIndex(): Int

    fun getRight(): CharPosition

    fun getRightLine(): Int

    fun getRightColumn(): Int

    fun getRightIndex(): Int

    fun getIndexer(): IIndexer

    fun isSelected(): Boolean

}